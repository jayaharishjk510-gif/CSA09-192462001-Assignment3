package Q7;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Alternative Concept for Q7:
 * Compares ExecutorService approach with:
 * 1. files.parallelStream() (ForkJoinPool.commonPool())
 * 2. Creating one thread per file (Thread-per-task anti-pattern)
 */
public class ResultProcessorAlternative {

    public static void testParallelStream(List<ResultProcessor.FileProcessorTask> tasks) {
        System.out.println("\n--- Testing Alternative 1: parallelStream() ---");
        long start = System.currentTimeMillis();

        LongAdder success = new LongAdder();
        LongAdder failures = new LongAdder();

        // Using common ForkJoinPool
        tasks.parallelStream().forEach(task -> {
            ResultProcessor.TaskOutcome outcome = task.call();
            if (outcome.status == ResultProcessor.Status.SUCCESS) {
                success.increment();
            } else {
                failures.increment();
            }
        });

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("parallelStream() completed in %d ms | Success: %d, Failures: %d%n",
                elapsed, success.sum(), failures.sum());
    }

    public static void testThreadPerFileSample(List<ResultProcessor.FileProcessorTask> tasks) throws InterruptedException {
        System.out.println("\n--- Testing Alternative 2: Thread-per-file on small batch (500 files) ---");
        // We limit to 500 files to avoid OutOfMemoryError / OS thread exhaustion
        List<ResultProcessor.FileProcessorTask> sample = tasks.subList(0, Math.min(500, tasks.size()));
        long start = System.currentTimeMillis();

        List<Thread> threads = new ArrayList<>(sample.size());
        LongAdder success = new LongAdder();

        for (ResultProcessor.FileProcessorTask task : sample) {
            Thread t = new Thread(() -> {
                ResultProcessor.TaskOutcome o = task.call();
                if (o.status == ResultProcessor.Status.SUCCESS) success.increment();
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) t.join();
        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("Thread-per-file (500 threads) completed in %d ms | Success: %d%n", elapsed, success.sum());
    }

    public static void main(String[] args) throws Exception {
        Path scoreDir = Paths.get("Q7/scores");
        List<ResultProcessor.FileProcessorTask> tasks = ResultProcessor.generateDataset(scoreDir, 10_000);
        testParallelStream(tasks);
        testThreadPerFileSample(tasks);
    }
}
