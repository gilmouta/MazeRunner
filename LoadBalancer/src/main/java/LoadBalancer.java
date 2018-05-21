import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class LoadBalancer {
    private static InstanceManager instanceManager;

    public static void main(String[] args) throws Exception {
        instanceManager = InstanceManager.getInstanceManager();

        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/mzrun.html", new MazeRunnerHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        System.out.println("Starting Load Balancer.");
        server.start();
    }

    public static Job createJob(String qs){
        Map<String, String> query = parseQueryString(qs);

        int xStart, yStart, xFinal, yFinal, velocity;
        try {
            xStart = Integer.parseInt(query.get("x0"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Arg %d: xStart argument must be a number", 0));
        }
        try {
            yStart = Integer.parseInt(query.get("y0"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Arg %d: yStart argument must be a number", 1));
        }
        try {
            xFinal = Integer.parseInt(query.get("x1"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Arg %d: xFinal argument must be a number", 2));
        }
        try {
            yFinal = Integer.parseInt(query.get("y1"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Arg %d: yFinal argument must be a number", 3));
        }
        try {
            velocity = Integer.parseInt(query.get("v"));
            if (velocity < 1 || velocity > 100) {
                throw new IllegalArgumentException(String.format("Arg %d: velocity argument must be between 1 and 100", 4));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Arg %d: velocity argument must be a number", 4));
        }
        String strategy = query.get("s");

        return new Job(xStart, yStart, xFinal, yFinal, velocity, strategy);
    }
    static class MazeRunnerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("Received request");
            Job job = createJob(t.getRequestURI().getQuery());

            Instance instanceForJob = null;
            String ipForJob = null;
            try {
                instanceForJob = instanceManager.distributeJob(job);
                ipForJob = instanceForJob.getEc2Instance().getPublicIpAddress();
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }

            String charset = java.nio.charset.StandardCharsets.UTF_8.name();
            String query = t.getRequestURI().getQuery();
            System.out.println(query);

            HttpURLConnection connection = (HttpURLConnection) new URL("http", ipForJob, 8000, "/mzrun.html?"+query).openConnection();
            connection.setRequestProperty("Accept-Charset", charset);

            try {
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

            } catch(Exception e) {
                e.printStackTrace();
                String response = connection.getResponseCode() + "\n";
                System.out.println(response);
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } finally {
                instanceManager.endJob(job, instanceForJob);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    public static Map<String, String> parseQueryString(String qs) {
        Map<String, String> result = new HashMap<>();
        if (qs == null)
            return result;

        int last = 0, next, l = qs.length();
        while (last < l) {
            next = qs.indexOf('&', last);
            if (next == -1)
                next = l;

            if (next > last) {
                int eqPos = qs.indexOf('=', last);
                try {
                    if (eqPos < 0 || eqPos > next)
                        result.put(URLDecoder.decode(qs.substring(last, next), "utf-8"), "");
                    else
                        result.put(URLDecoder.decode(qs.substring(last, eqPos), "utf-8"), URLDecoder.decode(qs.substring(eqPos + 1, next), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e); // will never happen, utf-8 support is mandatory for java
                }
            }
            last = next + 1;
        }
        return result;
    }
}
