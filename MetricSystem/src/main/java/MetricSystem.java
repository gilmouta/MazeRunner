import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MetricSystem {
    private static MetricSystem ourInstance = new MetricSystem();
    private static DynamoDBMapper mapper;

    public static MetricSystem getInstance() {
        return ourInstance;
    }

    private MetricSystem() {
        // TODO: Access/Create dynamoDB tables here
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
        mapper = new DynamoDBMapper(client);
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
        mapper.save(metric);
    }
}
