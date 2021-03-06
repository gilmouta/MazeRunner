import java.util.HashMap;
import java.util.Map;

public class Metric {
    // Request parameters
    private int distance;
    private int velocity;
    private String strategy;

    // Collected metrics
    //private int callDepth;
    private Map<Integer, Integer> loopCount;  // Don't store in DB
    private int loopDepth;

    public Metric(String xStart, String xFinal, String yStart, String yFinal, String velocity, String strategy){
        int x0 = Integer.parseInt(xStart);
        int x1 = Integer.parseInt(xFinal);
        int y0 = Integer.parseInt(yStart);
        int y1 = Integer.parseInt(yFinal);
        distance = (int) Math.round(Math.sqrt(Math.pow(x1-x0, 2) + Math.pow(y1-y0, 2)));
        this.velocity = Integer.parseInt(velocity);
        this.strategy = strategy;
        //this.maze = maze;
        //this.callDepth = 0;
        this.loopCount = new HashMap<>();
        this.loopDepth = 0;
    }
    public String toString() {
        return "[Metric] for distance: "+ distance +" we got loopdepth: " + loopDepth;
    }

    public int getDistance(){
        return distance;
    }

    public int getVelocity() {
        return velocity;
    }
    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public String getStrategy() {
        return strategy;
    }
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public int getLoopDepth() {
        return loopDepth;
    }
    public void setLoopDepth(int loopDepth) {
        this.loopDepth = loopDepth;
    }

    public Map<Integer, Integer> getLoopCount() {
        return loopCount;
    }
    public void setLoopCount(Map<Integer, Integer> loopCount) {
        this.loopCount = loopCount;
    }

    /*public int getCallDepth() {
        return callDepth;
    }
    public void setCallDepth(int callDepth) {
        this.callDepth = callDepth;
    }

    public void updateCallDepth(int callDepth) {
        if (callDepth > this.callDepth) {
            this.callDepth = callDepth;
        }
    }*/

    public void calculateLoopDepth() {
        for (Integer depth: loopCount.values()){
            if (depth > loopDepth){
                loopDepth = depth;
            }
        }
    }
}
