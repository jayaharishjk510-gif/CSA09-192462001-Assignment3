package Q4;

/**
 * Scanner station implementation implementing java.lang.Runnable.
 * Separates task logic from thread execution mechanism.
 */
public class ScannerRunnable implements Runnable {
    private final int stationId;
    private final int batchSize;
    private int scannedCount = 0;

    public ScannerRunnable(int stationId, int batchSize) {
        this.stationId = stationId;
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        long threadId = Thread.currentThread().threadId();
        System.out.printf("[Runnable Station-%d running on '%s' (ID %d)] Began scanning %d scripts.%n",
                stationId, threadName, threadId, batchSize);
        try {
            for (int i = 1; i <= batchSize; i++) {
                Thread.sleep(15 + (int)(Math.random() * 20));
                scannedCount++;
            }
            System.out.printf("[Runnable Station-%d running on '%s'] COMPLETED scanning %d scripts.%n",
                    stationId, threadName, scannedCount);
        } catch (InterruptedException e) {
            System.err.printf("[Runnable Station-%d] Interrupted!%n", stationId);
            Thread.currentThread().interrupt();
        }
    }

    public int getScannedCount() {
        return scannedCount;
    }
}

