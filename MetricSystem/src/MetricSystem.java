import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MetricSystem {
    private static MetricSystem ourInstance = new MetricSystem();

    public static MetricSystem getInstance() {
        return ourInstance;
    }

    private MetricSystem() {
        // TODO: Access/Create dynamoDB tables here
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
        // TODO: Code to save a metric (to file/db) here
        List<String> lines = Arrays.asList(metric.toString());
        Path file = Paths.get("metrics.txt");
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
