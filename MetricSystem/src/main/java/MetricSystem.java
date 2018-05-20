import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MetricSystem {
    private static MetricSystem ourInstance = new MetricSystem();
    private AmazonDynamoDB dynamoDB;
    private String tableName;

    public static MetricSystem getInstance() {
        return ourInstance;
    }

    private MetricSystem() {
        init();
    }

    public void startRequest(Metric m) {
        MetricCollector.setMetric(m);
    }

    public void finishRequest() {
        Metric m = MetricCollector.getMetric();
        String result = "Thread " + java.lang.Thread.currentThread().getId() + " got metric: " + m.toString();
        System.out.println(result);
        saveMetric(m);
        MetricCollector.deleteMetric();
    }

    public void abortRequest() {
        MetricCollector.deleteMetric();
    }

    public void saveMetric(Metric metric) {
        try {

            // Add an item
            Map<String, AttributeValue> item = newItem(metric.getDistance(), metric.getVelocity(),
                    metric.getStrategy(), metric.getCallDepth(), metric.getLoopDepth());
            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
            dynamoDB.putItem(putItemRequest);

        } catch (AmazonServiceException ase) {
            printAmazonServiceException(ase);
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    private void init() {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("eu-west-2")
                .build();

        try{
            tableName = "metrics-CNV";

            // Create a table with a primary hash key named 'id', which holds a random String
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                    .withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(dynamoDB, tableName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (AmazonServiceException ase) {
            printAmazonServiceException(ase);
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }

    private Map<String, AttributeValue> newItem(int distance, int velocity, String strategy, int callDepth, int loopDepth) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        String id = UUID.randomUUID().toString();
        item.put("id", new AttributeValue(id));
        item.put("strategy", new AttributeValue(strategy));
        item.put("distance", new AttributeValue().withN(Integer.toString(distance)));
        item.put("velocity", new AttributeValue().withN(Integer.toString(velocity)));
        item.put("callDepth", new AttributeValue().withN(Integer.toString(callDepth)));
        item.put("loopDepth", new AttributeValue().withN(Integer.toString(loopDepth)));

        return item;
    }

    private static void printAmazonServiceException(AmazonServiceException ase) {
        System.out.println("Caught an AmazonServiceException, which means your request made it "
                + "to AWS, but was rejected with an error response for some reason.");
        System.out.println("Error Message:    " + ase.getMessage());
        System.out.println("HTTP Status Code: " + ase.getStatusCode());
        System.out.println("AWS Error Code:   " + ase.getErrorCode());
        System.out.println("Error Type:       " + ase.getErrorType());
        System.out.println("Request ID:       " + ase.getRequestId());
    }
}
