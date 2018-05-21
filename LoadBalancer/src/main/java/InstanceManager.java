import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

public class InstanceManager {
    private static InstanceManager instanceManager = new InstanceManager();
    private AmazonEC2 ec2;
    private List<Instance> instances;

    public static InstanceManager getInstanceManager() {
        return instanceManager;
    }

    private InstanceManager() {
        init();
    }

    private void init() {
        System.out.println("Initializing Instance Manager");
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion("eu-west-2").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        instances = new LinkedList<>();
        // Can't manage at existing instances because it'll manage itself
        /*DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        for (Reservation reservation : reservations) {
            for (com.amazonaws.services.ec2.model.Instance i: reservation.getInstances()){
                if (i.getState().getCode() == 16) {
                    instances.add(new Instance(i, false));
                }
            }
        }*/
        if (instances.size() < 1) {
            startInstance();
        }

        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(new InstanceCleaner() , 5, 5, TimeUnit.MINUTES);
    }

    public Instance startInstance(){
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        runInstancesRequest.withImageId("ami-d749a4b0")
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName("CNV-MazeRunner")
                .withSecurityGroups("CNV-MazeRunner");
        RunInstancesResult runInstancesResult =
                ec2.runInstances(runInstancesRequest);
        Instance instance = new Instance(runInstancesResult.getReservation().getInstances().get(0), true);
        instances.add(instance);
        return instance;
    }

    public void stopInstance(Instance i){
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(i.getEc2Instance().getInstanceId());
        ec2.terminateInstances(termInstanceReq);
        instances.remove(i);
    }

    public Instance distributeJob(Job job) {
        Instance bestInstance = null;
        do {
            synchronized (this) {
                if (instances.size() == 0) {
                    System.out.println("No instances found somehow, starting instance.");
                    startInstance();
                    continue;
                }

                long leastWork = instances.get(0).getWork();
                for (Instance i : instances) {
                    if (i.isToRemove()) {
                        continue;
                    }
                    if (i.getWork() <= leastWork) {
                        leastWork = i.getWork();
                        bestInstance = i;
                    }
                }

                if (leastWork >= 100000) {
                    // If every instance has a good load already
                    startInstance(); // Start a new instance
                }

                long cost = job.getEstimatedCost();

                if (leastWork + cost >= 200000) {
                    // If the resulting work is too high, we will assign this job to the new instance
                    // This means the job will have to wait for the instance to get ready,
                    // but it should be better than waiting for the present load
                    bestInstance = null;
                    continue;
                }

                bestInstance.addJob(cost);
            }

            int state = getInstanceState(bestInstance);
            while (state == 0) { // While pending
                try {
                    Thread.sleep(10000);
                    state = getInstanceState(bestInstance);
                    if (state != 0){
                        // Give it time to initialize
                        Thread.sleep(20000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (state != 16) {
                synchronized (this) {
                    System.out.println("Instance with lowest workload isn't alive. Removing it from instance list");
                    instances.remove(bestInstance);
                    bestInstance = null;
                }
            }

        } while (bestInstance == null);

        System.out.println("Assigned to " + bestInstance.getEc2Instance().getPublicIpAddress());
        return bestInstance;
    }

    public synchronized void endJob(Job job, Instance instance) {
        instance.finishJob(job.getEstimatedCost());
    }

    public int getInstanceState(Instance instance) {
        DescribeInstancesResult result = ec2.describeInstances(
                new DescribeInstancesRequest().withInstanceIds(instance.getEc2Instance().getInstanceId()));
        List<Reservation> reservations = result.getReservations();
        for (Reservation reservation : reservations) {
            for (com.amazonaws.services.ec2.model.Instance i: reservation.getInstances()) {
                if (i.getInstanceId().equals(instance.getEc2Instance().getInstanceId())){
                    instance.setEc2Instance(i);
                    return i.getState().getCode();
                }
            }
        }
        return -1;
    }

    class InstanceCleaner implements Runnable {

        @Override
        public void run() {
            System.out.println("Running cleanup");
            long totalWork = 0;
            int totalJobs = 0;
            int totalInstances = instances.size();
            if (totalInstances == 0) {
                // Always keep 1 instance running
                startInstance();
                return;
            }
            if (totalInstances == 1) {
                return;
            }

            long leastWork = instances.get(0).getWork();
            Instance bestCandidate = null;

            for (Instance instance:instances) {
                totalWork += instance.getWork();
                totalJobs += instance.getJobs();
                if (instance.getWork() <= leastWork) {
                    bestCandidate = instance;
                    leastWork = instance.getWork();
                }
            }

            boolean remove = false;
            if (totalJobs < totalInstances-1){
                // If there's 2 idle instances, one can be shut down
                remove = true;
            }
            if (leastWork == 0 && totalWork/totalInstances <= 100000) {
                // If there's one idle instance, and the total work per instance is less than 100000, one can be shut down
                remove = true;
            }
            if (totalWork/totalInstances <= 10000) {
                // If there's less than 10000 work per instance, one can be shut down
                remove = true;
            }

            System.out.println("Decided to remove " + bestCandidate.getEc2Instance().getPublicIpAddress());
            bestCandidate.setToRemove(true);
            while (bestCandidate.getJobs() != 0) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stopInstance(bestCandidate);
            System.out.println("Removed.");
        }
    }
}
