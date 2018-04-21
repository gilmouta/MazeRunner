public class Metric {
    // Request parameters
    private String xStart;
    private String xFinal;
    private String yStart;
    private String yFinal;
    private String velocity;
    private String strategy;
    //Maze maze;

    // Collected metrics
    private int iCount;

    public Metric(String xStart, String xFinal, String yStart, String yFinal, String velocity, String strategy){
        this.xStart = xStart;
        this.xFinal = xFinal;
        this.yStart = yStart;
        this.yFinal = yFinal;
        this.velocity = velocity;
        this.strategy = strategy;
        //this.maze = maze;

        this.iCount = 0;
    }

    public void incrementICount(int incr) {
        this.iCount += incr;
    }

    public String toString() {
        return "[Metric] icount: " + iCount;
    }

    public String getxStart() {
        return xStart;
    }

    public String getxFinal() {
        return xFinal;
    }

    public String getyStart() {
        return yStart;
    }

    public String getyFinal() {
        return yFinal;
    }

    public String getVelocity() {
        return velocity;
    }

    public String getStrategy() {
        return strategy;
    }

    public int getICount() {
        return iCount;
    }
}
