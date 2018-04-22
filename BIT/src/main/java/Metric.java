import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="Metrics")
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
    private int iCount;
    private int callDepth;

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
        return "[Metric] icount: " + iCount + " | calldepth: " + callDepth;
    }

    @DynamoDBHashKey(attributeName="Id")
    public Integer getId() { return id; }
    public void setId(Integer id) {this.id = id; }

    @DynamoDBAttribute(attributeName="xStart")
    public String getxStart() {
        return xStart;
    }
    public void setxStart(String xStart) {
        this.xStart = xStart;
    }

    @DynamoDBAttribute(attributeName="xFinal")
    public String getxFinal() {
        return xFinal;
    }
    public void setxFinal(String xFinal) {
        this.xFinal = xFinal;
    }

    @DynamoDBAttribute(attributeName="yStart")
    public String getyStart() {
        return yStart;
    }
    public void setyStart(String yStart) {
        this.yStart = yStart;
    }

    @DynamoDBAttribute(attributeName="yFinal")
    public String getyFinal() {
        return yFinal;
    }
    public void setyFinal(String yFinal) {
        this.yFinal = yFinal;
    }

    @DynamoDBAttribute(attributeName="velocity")
    public String getVelocity() {
        return velocity;
    }
    public void setVelocity(String velocity) {
        this.velocity = velocity;
    }

    @DynamoDBAttribute(attributeName="strategy")
    public String getStrategy() {
        return strategy;
    }
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    @DynamoDBAttribute(attributeName="iCount")
    public int getiCount() {
        return iCount;
    }
    public void setiCount(int iCount) {
        this.iCount = iCount;
    }

    @DynamoDBAttribute(attributeName="callDepth")
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
}
