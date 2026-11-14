package Q4;

import java.util.*;

/**
 * Main simulation runner for Q4 demonstrating:
 * 1. Life cycle methods: start(), sleep(), join(), isAlive()
 * 2. start() vs run() verification with thread IDs
 * 3. 10 consecutive execution runs to observe non-deterministic thread scheduling
 * 4. Thread Priority experiment
 * 5. Daemon Thread behavior
 */
public class ScannerStationSimulation {

    public static void runLifecycleDemo() throws InterruptedException {
        System.out.println("===============================================================");
        System.out.println("1. DEMONSTRATING THREAD LIFECYCLE: start(), sleep(), join(), isAlive()");
        System.out.println("===============================================================");

        ScannerThread station1 = new ScannerThread(1, 10);
        System.out.println("Before start() -> station1.isAlive(): " + station1.isAlive() + " | State: " + station1.getState());

        station1.start();
        System.out.println("After start() -> station1.isAlive(): " + station1.isAlive() + " | State: " + station1.getState());

        // Wait slightly and inspect state while it sleeps
        Thread.sleep(25);
        System.out.println("During execution -> station1.isAlive(): " + station1.isAlive() + " | State: " + station1.getState());

        // Using join() to guarantee main waits for completion
        System.out.println("Main calling station1.join()...");
        station1.join();

        System.out.println("After join() -> station1.isAlive(): " + station1.isAlive() + " | State: " + station1.getState());
        System.out.println("Station 1 total scanned: " + station1.getScannedCount());
    }

    public static void demonstrateStartVsRun() {
        System.out.println("\n===============================================================");
        System.out.println("2. DEMONSTRATING start() vs run() EXECUTION CONTEXT");
        System.out.println("===============================================================");

        System.out.printf("Current Main Thread: '%s' [ID: %d]%n", Thread.currentThread().getName(), Thread.currentThread().threadId());

        ScannerRunnable taskDirectRun = new ScannerRunnable(99, 3);
        System.out.println("\n--- Invoking taskDirectRun.run() directly (NO NEW THREAD) ---");
        taskDirectRun.run(); // Synchronous call on main thread

        ScannerRunnable taskThreadStart = new ScannerRunnable(100, 3);
        Thread workerThread = new Thread(taskThreadStart, "Worker-Thread-100");
        System.out.println("\n--- Invoking workerThread.start() (CREATES ASYNCHRONOUS THREAD) ---");
        workerThread.start();
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void runTenSchedulingTrials() throws InterruptedException {
        System.out.println("\n===============================================================");
        System.out.println("3. TEN CONSECUTIVE RUNS: OBSERVING FINISH ORDERS");
        System.out.println("===============================================================");

        for (int run = 1; run <= 10; run++) {
            List<String> finishOrder = Collections.synchronizedList(new ArrayList<>());
            List<Thread> threads = new ArrayList<>();

            for (int s = 1; s <= 4; s++) {
                final int stationId = s;
                Thread t = new Thread(() -> {
                    try {
                        // Batch of 5 items with small random sleep
                        for (int i = 0; i < 5; i++) {
                            Thread.sleep((int)(Math.random() * 8));
                        }
                        finishOrder.add("Station-" + stationId);
                    } catch (InterruptedException ignored) {}
                }, "Station-" + stationId);
                threads.add(t);
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) t.join();

            System.out.printf("Run #%2d Finish Order: %s%n", run, String.join(" -> ", finishOrder));
        }
    }

    public static void runPriorityExperiment() throws InterruptedException {
        System.out.println("\n===============================================================");
        System.out.println("4. THREAD PRIORITY EXPERIMENT (MIN, NORM, MAX)");
        System.out.println("===============================================================");

        for (int exp = 1; exp <= 3; exp++) {
            List<String> finishOrder = Collections.synchronizedList(new ArrayList<>());
            Thread tMin = new Thread(() -> {
                burnCpu();
                finishOrder.add("P_MIN(1)");
            });
            tMin.setPriority(Thread.MIN_PRIORITY);

            Thread tNorm = new Thread(() -> {
                burnCpu();
                finishOrder.add("P_NORM(5)");
            });
            tNorm.setPriority(Thread.NORM_PRIORITY);

            Thread tMax = new Thread(() -> {
                burnCpu();
                finishOrder.add("P_MAX(10)");
            });
            tMax.setPriority(Thread.MAX_PRIORITY);

            tMin.start();
            tNorm.start();
            tMax.start();

            tMin.join();
            tNorm.join();
            tMax.join();

            System.out.printf("Priority Experiment #%d Finish Order: %s%n", exp, String.join(" -> ", finishOrder));
        }
    }

    private static void burnCpu() {
        long sum = 0;
        for (int i = 0; i < 20_000_000; i++) {
            sum += (i ^ 3);
        }
    }

    public static void runDaemonExperiment() throws InterruptedException {
        System.out.println("\n===============================================================");
        System.out.println("5. DAEMON THREAD DEMONSTRATION");
        System.out.println("===============================================================");

        Thread daemonStation = new Thread(() -> {
            try {
                System.out.println("[Daemon Station] Running background cleanup tasks...");
                while (true) {
                    Thread.sleep(50);
                    System.out.println("[Daemon Station] Heartbeat ping (Still alive)...");
                }
            } catch (InterruptedException e) {
                System.out.println("[Daemon Station] Interrupted.");
            } finally {
                System.out.println("[Daemon Station] Finally block executed.");
            }
        }, "Background-Daemon-Station");

        daemonStation.setDaemon(true);
        daemonStation.start();

        System.out.println("[Main] User thread doing 120ms of work...");
        Thread.sleep(120);
        System.out.println("[Main] Main user thread finishing now. JVM should terminate daemon immediately!");
    }

    public static void main(String[] args) throws InterruptedException {
        runLifecycleDemo();
        demonstrateStartVsRun();
        runTenSchedulingTrials();
        runPriorityExperiment();
        runDaemonExperiment();
    }
}
