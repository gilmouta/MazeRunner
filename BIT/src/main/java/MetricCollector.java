import BIT.highBIT.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("Duplicates")
public class MetricCollector {
    //private static PrintStream out = null;
    private static ThreadLocal<Metric> threadMetric = new ThreadLocal<>();
    private static ThreadLocal<Integer> callDepth = new ThreadLocal<>();
    
    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        String infilenames[];
        String inPath;
        String outPath;
        if (argv.length == 0) {
            System.out.println("Instrumenting strategy files");
            inPath = "MazeRunner/target/classes/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/strategies";
            try {
                // Dirty fix to ease IntelliJ use
                String here = new File(".").getCanonicalPath();
                if (here.endsWith("BIT")) {
                    inPath = "../" + inPath;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            outPath = inPath;
            infilenames = new String[]{"AStarStrategy.class", "BreadthFirstSearchStrategy.class", "DepthFirstSearchStrategy.class"};
        } else {
            inPath = argv[0];
            outPath = argv[1];
            File file_in = new File(argv[0]);
            infilenames = file_in.list();
        }
        
        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith(".class")) {
				// create class info object
				ClassInfo ci = new ClassInfo(inPath + System.getProperty("file.separator") + infilename);
				
                // loop through all the routines
                // see java.util.Enumeration for more information on Enumeration class
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    routine.addBefore("MetricCollector", "mstart", new Integer(1));
                    routine.addAfter("MetricCollector", "mend", new Integer(1));

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("MetricCollector", "iCount", new Integer(bb.size()));
                    }
                }
                //ci.addAfter("MetricCollector", "printICount1", ci.getClassName());
                ci.write(outPath + System.getProperty("file.separator") + infilename);
            }
        }
    }

    public static synchronized void setMetric(Metric metric) {
        System.out.println("Setting metric for thread " + Thread.currentThread().getId());
        threadMetric.set(metric);
    }
    
    public static synchronized Metric getMetric() {
        return threadMetric.get();
    }

    public static synchronized void deleteMetric() {
        threadMetric.remove();
        callDepth.remove();
    }

    public static synchronized void iCount(int incr) {
        threadMetric.get().incrementICount(incr);
    }

    public static synchronized void mstart(int m) {
        if (callDepth.get() == null) {
            callDepth.set(0);
        }
        callDepth.set(callDepth.get()+1);
        threadMetric.get().updateCallDepth(callDepth.get());
    }

    public static synchronized void mend(int m) {
        callDepth.set(callDepth.get()-1);
    }
}

