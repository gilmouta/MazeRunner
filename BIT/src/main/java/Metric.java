import java.util.HashMap;
import java.util.Map;

public class Metric {
    // Request parameters
    private Integer id;
    private String xStart;
    private String xFinal;
    private String yStart;
    private String yFinal;
    private String velocity;
    private String strategy;
    //Maze maze;

    // Collected metrics
    private int callDepth;
    private Map<Integer, Integer> loopCount;  // Don't store in DB
    private int loopDepth;

    public Metric(String xStart, String xFinal, String yStart, String yFinal, String velocity, String strategy){
        this.xStart = xStart;
        this.xFinal = xFinal;
        this.yStart = yStart;
        this.yFinal = yFinal;
        this.velocity = velocity;
        this.strategy = strategy;
        //this.maze = maze;
        this.callDepth = 0;
        this.loopCount = new HashMap<>();
        this.loopDepth = 0;
    }
    public String toString() {
        return "[Metric] loopdepth: " + loopDepth + " | calldepth: " + callDepth;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) {this.id = id; }

    public String getxStart() {
        return xStart;
    }
    public void setxStart(String xStart) {
        this.xStart = xStart;
    }

    public String getxFinal() {
        return xFinal;
    }
    public void setxFinal(String xFinal) {
        this.xFinal = xFinal;
    }

    public String getyStart() {
        return yStart;
    }
    public void setyStart(String yStart) {
        this.yStart = yStart;
    }

    public String getyFinal() {
        return yFinal;
    }
    public void setyFinal(String yFinal) {
        this.yFinal = yFinal;
    }

    public String getVelocity() {
        return velocity;
    }
    public void setVelocity(String velocity) {
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

    public int getCallDepth() {
        return callDepth;
    }
    public void setCallDepth(int callDepth) {
        this.callDepth = callDepth;
    }

    public void updateCallDepth(int callDepth) {
        if (callDepth > this.callDepth) {
            this.callDepth = callDepth;
        }
    }

    public void calculateLoopDepth() {
        for (Integer depth: loopCount.values()){
            if (depth > loopDepth){
                loopDepth = depth;
            }
        }
    }
}
