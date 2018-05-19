import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

public class LoadBalancer {
    static AmazonEC2 ec2;

    private static void init() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion("eu-west-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/test", new RunnerHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        // FIXME _autoScaler = new AutoScaler();
        _autoScaler.init();
        System.out.println("Starting Load Balancer.");
        server.start();
    }

    private static boolean isAlive(String ip) {
        final int RETRY_INTERVAL = 1000;
        final int TIMEOUT = 2000;
        HttpURLConnection connection = null;

        for(int retries=3; retries > 0; retries--) {
            try {
                connection = (HttpURLConnection) new URL("http", ip, 8000, "/test").openConnection();
                connection.setConnectTimeout(TIMEOUT);

                if(connection.getResponseCode() == HTTP_OK) {
                    connection.disconnect();
                    return false;
                }

                Thread.sleep(RETRY_INTERVAL);

            } catch (IOException e) {
                if(connection != null)
                    connection.disconnect();

            } catch (InterruptedException e) {
                if(connection != null)
                    connection.disconnect();

                return false;
            }
        }

        if(connection != null)
            connection.disconnect();

        _autoScaler.instanceFailure(ip);
        return true;
    }

    public static Job getArgs(String qs){
        Map<String, String> query = WebServer.parseQueryString(qs);
        int xStart, yStart, xFinal, yFinal, velocity;
        try {
            xStart = Integer.parseInt(query.get("x0"));
            yStart = Integer.parseInt(query.get("y0"));
            xFinal = Integer.parseInt(query.get("x1"));
            yFinal = Integer.parseInt(query.get("y1"));
            velocity = Integer.parseInt(query.get("v"));
            String mazeFile = query.get("m");
            if (velocity < 1 || velocity > 100) {
                return null;
            }

            return new Job(xStart, yStart, xFinal, yFinal, velocity, mazeFile);
        }catch (Exception e){
            return null;
        }
    }
    static class RunnerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Job job = getArgs(t.getRequestURI().getQuery());

            String ipForJob = null;
            do {
                try {
                    ipForJob = _autoScaler.scheduleJob(job);

                    if (ipForJob.equals(_autoScaler.QUEUED)) {
                        ipForJob = _autoScaler.waitForBootAndSchedule();
                    }
                } catch(InterruptedException e) {
                    // print error and retry
                    System.out.println("failed to schedule job: " + e.getMessage());
                }

            }
            while (isAlive(ipForJob)); // run until job is finished


            String charset = java.nio.charset.StandardCharsets.UTF_8.name();
            String query = t.getRequestURI().getQuery();

            HttpURLConnection connection = (HttpURLConnection) new URL("http", ipForJob, 8000, "/mzrun.html"+"?"+query).openConnection();
            connection.setRequestProperty("Accept-Charset", charset);

            try {
                job.setDesiredIP(ipForJob);

                InputStream response = connection.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                System.out.print("received " + job.toString());
                int data;
                while ((data = response.read()) > -1) {
                    outputStream.write(data);
                }

                t.sendResponseHeaders(200, outputStream.size());
                OutputStream os = t.getResponseBody();
                outputStream.writeTo(os);
                os.close();

            } catch(IOException e) {
                e.printStackTrace();
                String response = connection.getResponseCode() + "\n";
                System.out.println(response);
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (Exception e) {
                t.sendResponseHeaders(400, e.getMessage().length());
                OutputStream os = t.getResponseBody();
                os.write(e.getMessage().getBytes());
                os.close();
            }
            // Job finished. Update scheduler.
            _autoScaler.finishJob(job, ipForJob);
        }
    }
}
