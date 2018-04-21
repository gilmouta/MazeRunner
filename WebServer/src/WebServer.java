import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.Maze;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.exceptions.CantReadMazeInputFileException;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.render.RenderMaze;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.render.RenderMazeHTMLClientCanvas;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies.FactoryMazeRunningStrategies;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.maze.strategies.MazeRunningStrategy;

public class WebServer {
    private static MetricSystem metricSystem;


    public static void main(String[] args) throws Exception {
        metricSystem = MetricSystem.getInstance();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new TestHandler());
        server.createContext("/mzrun.html", new MazeRunnerHandler());
        //server.setExecutor(null); // creates a default executor
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        System.out.println("Starting server.");
        server.start();
    }

    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This was the query:" + t.getRequestURI().getQuery() 
                               + "##";
            System.out.println(response);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    @SuppressWarnings("Duplicates") // FIXME
    static class MazeRunnerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                System.out.println("Doing maze request in thread " + java.lang.Thread.currentThread().getId());
                Map<String, String> query = parseQueryString(t.getRequestURI().getQuery());

                metricSystem.startRequest(new Metric(query.get("x0"), query.get("x1"), query.get("y0"), query.get("y1"), query.get("v"), query.get("s")));

                if (query.size() < 7) {
                    throw new IllegalArgumentException("InsuficientArguments - The maze runners do not have enough information to solve the maze");
                }

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
                MazeRunningStrategy strategy = null;
                strategy = FactoryMazeRunningStrategies.CreateMazeRunningStrategy(query.get("s"));

                String mazeFile = query.get("m");
                Maze maze = null;

                // Read the maze from the file
                try {
                    FileInputStream fileIn = new FileInputStream(mazeFile);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    maze = (Maze) in.readObject();
                    in.close();
                    fileIn.close();
                } catch (IOException i) {
                    i.printStackTrace();
                    throw new CantReadMazeInputFileException("Problems reading " + mazeFile + " input file!");
                } catch (ClassNotFoundException c) {
                    System.out.println("Maze class not found -> Dark stuff is happening...");
                    c.printStackTrace();
                    return;
                }

                // Solve the maze.
                strategy.solve(maze, xStart, yStart, xFinal, yFinal, velocity);


                // Choose the way to render the maze and rendered it
                RenderMaze renderMaze = new RenderMazeHTMLClientCanvas();
                String mazeRendered = renderMaze.render(maze, velocity);

                t.sendResponseHeaders(200, mazeRendered.length());
                OutputStream os = t.getResponseBody();
                os.write(mazeRendered.getBytes());
                os.close();
                metricSystem.finishRequest();
            } catch (Exception e) {
                t.sendResponseHeaders(400, e.getMessage().length());
                OutputStream os = t.getResponseBody();
                os.write(e.getMessage().getBytes());
                os.close();
                metricSystem.abortRequest();
            }
        }
    }


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
