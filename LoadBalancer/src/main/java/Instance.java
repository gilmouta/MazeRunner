import com.amazonaws.services.ec2.model.InstanceState;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

public class Instance {
    private com.amazonaws.services.ec2.model.Instance ec2Instance;
    private long work;
    private int jobs;
    private boolean pending;
    private boolean toRemove;

    public Instance(com.amazonaws.services.ec2.model.Instance instance, boolean pending) {
        this.ec2Instance = instance;
        this.work = 0;
        this.jobs = 0;
        this.pending = pending;
        this.toRemove = false;
    }

    public com.amazonaws.services.ec2.model.Instance getEc2Instance() {
        return ec2Instance;
    }

    public void setEc2Instance(com.amazonaws.services.ec2.model.Instance ec2Instance) {
        this.ec2Instance = ec2Instance;
    }

    public long getWork() {
        return work;
    }

    public void addJob(long work) {
        this.work += work;
        this.jobs += 1;
    }

    public void finishJob(long work) {
        this.work -= work;
        this.jobs -= 1;
    }

    public boolean isPending() {
        return pending;
    }

    public boolean isAlive() {
        final int RETRY_INTERVAL = 5000;
        final int TIMEOUT = 2000;
        HttpURLConnection connection = null;

        int retries = 3;
        if (pending) {
            retries = 10;
        }
        System.out.println("Checking if " + ec2Instance.getPublicIpAddress() + " is alive");

        for(; retries > 0; retries--) {
            try {
                connection = (HttpURLConnection) new URL("http", ec2Instance.getPublicIpAddress(), 8000, "/test").openConnection();
                connection.setConnectTimeout(TIMEOUT);

                if(connection.getResponseCode() == HTTP_OK) {
                    connection.disconnect();
                    pending = false;
                    return true;
                }

                System.out.println("Server " + ec2Instance.getPublicIpAddress() + " didn't answer. Retrying...");
                Thread.sleep(RETRY_INTERVAL);

            } catch (IOException e) {
                if(connection != null)
                    connection.disconnect();
                try {
                    Thread.sleep(RETRY_INTERVAL);
                } catch (InterruptedException ex){
                    ex.printStackTrace();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(ec2Instance.getPublicIpAddress() + " is dead!");
        if(connection != null)
            connection.disconnect();

        return false;
    }

    public int getJobs() {
        return jobs;
    }

    public boolean isToRemove() {
        return toRemove;
    }

    public void setToRemove(boolean toRemove) {
        this.toRemove = toRemove;
    }
}
