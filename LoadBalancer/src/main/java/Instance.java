public class Instance {
    private com.amazonaws.services.ec2.model.Instance ec2Instance;
    private long work;

    public Instance(com.amazonaws.services.ec2.model.Instance instance) {
        this.ec2Instance = instance;
        this.work = 0;
    }

    public com.amazonaws.services.ec2.model.Instance getEc2Instance() {
        return ec2Instance;
    }

    public long getWork() {
        return work;
    }

    public void setWork(long work) {
        this.work = work;
    }

    public void addWork(long work) {
        this.work += work;
    }

    public void removeWork(long work) {
        this.work -= work;
    }
}
