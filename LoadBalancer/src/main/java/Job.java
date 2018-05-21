public class Job {
    private String strategy;
    private int xStart, yStart, xFinal, yFinal, velocity;
    private int distance;

    private long estimatedCost = 0;

    public Job(int xStart,int  yStart, int xFinal, int yFinal, int velocity, String strategy) {
        this.xStart = xStart;
        this.yStart = yStart;
        this.xFinal = xFinal;
        this.yFinal = yFinal;
        this.velocity = velocity;
        this.strategy = strategy;
        distance = (int) Math.round(Math.sqrt(Math.pow(xFinal-xStart,2) + Math.pow(yFinal-yStart,2)));
        estimateCost();
    }

    public long getEstimatedCost() {
        return estimatedCost;
    }

    public void estimateCost() {
        System.out.println("Estimating cost...");
        // Get cost from similar requests
        double estimatedCost = MetricSystem.getInstance().estimateCost(this.distance, this.strategy);

        if (estimatedCost == -1) {
            System.out.println("...using linear function...");
            // MetricSystem didn't have close enough metrics, so we need to estimate one
            // Linear function: c = 557.43d - 112791
            // Based on BFS results, overestimation at worse
            // Since the estimation is bad at lower values, we need to add a bound
            estimatedCost = 557.43*distance - 112791;
            if (estimatedCost < 10000) {
                estimatedCost = 10000;
            }
        }

        // Velocity doesn't affect our metric. It does affect time taken (cost) in basically a 1:1 ratio
        // So double the velocity = half the cost
        // Our cost estimation is based on a velocity = 100
        estimatedCost = estimatedCost * (100/velocity);

        // DFS, being recursive, seems to always get a lower "loopDepth" metric then it's actual time cost
        // By analysis however, difference is a constant, so we just adjust it
        if (strategy.equals("dfs")) {
            estimatedCost *= 2.17;
        }

        System.out.println("...done: " + estimatedCost);
        this.estimatedCost = Math.round(estimatedCost);
    }

    public String toString() {
        return strategy + ", " + xStart + ", " + yStart + ", " + xFinal + ", " + yFinal + ", " + velocity;
    }

}
