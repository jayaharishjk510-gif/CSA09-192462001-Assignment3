package Q4;

/**
 * Scanner station implementation extending java.lang.Thread.
 */
public class ScannerThread extends Thread {
    private final int stationId;
    private final int batchSize;
    private int scannedCount = 0;

    public ScannerThread(int stationId, int batchSize) {
        super("ScannerStation-Thread-" + stationId);
        this.stationId = stationId;
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        System.out.printf("[%s | State: RUNNABLE] Started scanning batch of %d scripts on Thread ID %d.%n",
                getName(), batchSize, Thread.currentThread().threadId());
        try {
            for (int i = 1; i <= batchSize; i++) {
                // Simulate variable scan time per script (TIMED_WAITING state)
                Thread.sleep(15 + (int)(Math.random() * 20));
                scannedCount++;
            }
            System.out.printf("[%s | State: TERMINATING] Finished batch! Total scanned: %d.%n",
                    getName(), scannedCount);
        } catch (InterruptedException e) {
            System.err.printf("[%s] Scan interrupted!%n", getName());
            Thread.currentThread().interrupt();
        }
    }

    public int getScannedCount() {
        return scannedCount;
    }
}

