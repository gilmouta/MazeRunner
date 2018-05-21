import java.util.LinkedList;
import java.util.List;

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
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        for (Reservation reservation : reservations) {
            for (com.amazonaws.services.ec2.model.Instance i: reservation.getInstances()){
                if (i.getState().getCode() == 16) {
                    instances.add(new Instance(i));
                }
            }
        }
        if (instances.size() < 1) {
            startInstance();
        }
    }

    public Instance startInstance(){
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        runInstancesRequest.withImageId("ami-547e9333")
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName("CNV-MazeRunner")
                .withSecurityGroups("CNV-MazeRunner");
        RunInstancesResult runInstancesResult =
                ec2.runInstances(runInstancesRequest);
        Instance instance = new Instance(runInstancesResult.getReservation().getInstances().get(0));
        instances.add(instance);
        return instance;
    }

    public void stopInstance(){
        Instance toStop = instances.get(instances.size()-1);
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(toStop.getEc2Instance().getInstanceId());
        ec2.terminateInstances(termInstanceReq);
    }

    public synchronized Instance distributeJob(Job job) {
        long leastWork = instances.get(0).getWork();
        Instance bestInstance = null;
        for (Instance i: instances){
            if (i.getWork() <= leastWork){
                leastWork = i.getWork();
                bestInstance = i;
            }
        }

        bestInstance.addWork(job.getEstimatedCost());

        return bestInstance;
    }

    public synchronized void endJob(Job job, Instance instance) {
        instance.removeWork(job.getEstimatedCost());
    }
}
