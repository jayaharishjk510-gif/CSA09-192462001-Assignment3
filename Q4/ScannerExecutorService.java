package Q4;

import java.util.concurrent.*;
import java.util.*;

/**
 * Alternative Concept for Q4:
 * Using an ExecutorService with a fixed thread pool instead of raw Thread objects.
 */
public class ScannerExecutorService {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("=== Scanner Stations using ExecutorService (FixedThreadPool = 4) ===");
        
        int totalBatches = 12;
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int b = 1; b <= totalBatches; b++) {
            final int batchId = b;
            Callable<Integer> scanTask = () -> {
                String threadName = Thread.currentThread().getName();
                System.out.printf("[%s] Processing Scan Batch #%d%n", threadName, batchId);
                Thread.sleep(20 + (int)(Math.random() * 20));
                return 25; // 25 scripts scanned per batch
            };
            futures.add(pool.submit(scanTask));
        }

        int totalScriptsScanned = 0;
        for (Future<Integer> f : futures) {
            totalScriptsScanned += f.get();
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("ExecutorService Complete. Total Scripts Scanned: " + totalScriptsScanned);
    }
}

