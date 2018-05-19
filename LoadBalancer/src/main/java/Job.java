import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.UUID;

public class Job {
    private String maze;
    private int xStart, yStart, xFinal, yFinal, velocity;


    private String _jobId;

    private double _estimatedCost = 0;
    private boolean _jobIsDone;

    private String _desiredIP;

    public String getDesiredIP() { return _desiredIP; }
    public void setDesiredIP(String ip) { _desiredIP = ip; }
    public Job(int xStart,int  yStart, int xFinal, int yFinal, int velocity, String maze) {
        _jobId = UUID.randomUUID().toString();
        this.xStart = xStart;
        this.yStart = yStart;
        this.xFinal = xFinal;
        this.yFinal = yFinal;
        this.velocity = velocity;
        this.maze = maze;
        _jobIsDone = false;
    }
    public double getEstimatedCost() {
        return _estimatedCost;
    }
    public double estimateCost(MetricSystem metricSystem) {
        //TODO
        return 0;
    }

    public void finish() {
        _jobIsDone = true;
    }

    public String toString() {
        return maze + ", " + xStart + ", " + yStart + ", " + xFinal + ", " + yFinal + ", " + velocity;
    }

}
