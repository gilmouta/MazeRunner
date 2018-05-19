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
        // TODO: Save metric to DB here
    }
}
