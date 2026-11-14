package Q5;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SeatBookingBenchmark {

    private static final int SEATS = 100;
    private static final int THREADS = 10_000;

    // 1. Synchronized Method
    static class SyncMethodCinema {
        private final boolean[] booked = new boolean[SEATS];
        private int count = 0;
        public synchronized boolean book(int seat) {
            if (!booked[seat]) {
                booked[seat] = true;
                count++;
                return true;
            }
            return false;
        }
    }

    // 2. Synchronized Block on per-seat Object lock
    static class SyncBlockCinema {
        private final boolean[] booked = new boolean[SEATS];
        private final Object[] seatLocks = new Object[SEATS];
        private final AtomicInteger count = new AtomicInteger(0);

        SyncBlockCinema() {
            for (int i = 0; i < SEATS; i++) seatLocks[i] = new Object();
        }

        public boolean book(int seat) {
            synchronized (seatLocks[seat]) {
                if (!booked[seat]) {
                    booked[seat] = true;
                    count.incrementAndGet();
                    return true;
                }
                return false;
            }
        }
    }

    // 3. AtomicBoolean[] (Lock-free compareAndSet)
    static class AtomicCinema {
        private final AtomicBoolean[] booked = new AtomicBoolean[SEATS];
        private final AtomicInteger count = new AtomicInteger(0);

        AtomicCinema() {
            for (int i = 0; i < SEATS; i++) booked[i] = new AtomicBoolean(false);
        }

        public boolean book(int seat) {
            if (booked[seat].compareAndSet(false, true)) {
                count.incrementAndGet();
                return true;
            }
            return false;
        }
    }

    // 4. ReentrantLock array
    static class ReentrantLockCinema {
        private final boolean[] booked = new boolean[SEATS];
        private final ReentrantLock[] locks = new ReentrantLock[SEATS];
        private final AtomicInteger count = new AtomicInteger(0);

        ReentrantLockCinema() {
            for (int i = 0; i < SEATS; i++) locks[i] = new ReentrantLock();
        }

        public boolean book(int seat) {
            locks[seat].lock();
            try {
                if (!booked[seat]) {
                    booked[seat] = true;
                    count.incrementAndGet();
                    return true;
                }
                return false;
            } finally {
                locks[seat].unlock();
            }
        }
    }

    // 5. ConcurrentHashMap
    static class ConcurrentMapCinema {
        private final ConcurrentHashMap<Integer, String> bookings = new ConcurrentHashMap<>();

        public boolean book(int seat, String userId) {
            return bookings.putIfAbsent(seat, userId) == null;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== BENCHMARK: 10,000 CONCURRENT BOOKINGS ACROSS STRATEGIES ===");

        // Test 1: Synchronized Method
        SyncMethodCinema sm = new SyncMethodCinema();
        long start = System.nanoTime();
        ExecutorService pool1 = Executors.newFixedThreadPool(16);
        for (int i = 0; i < THREADS; i++) {
            final int s = i % SEATS;
            pool1.submit(() -> sm.book(s));
        }
        pool1.shutdown();
        pool1.awaitTermination(10, TimeUnit.SECONDS);
        long durSm = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("1. Synchronized Method      : %4d ms (Seats sold: %d)%n", durSm, sm.count);

        // Test 2: Synchronized Block (Fine-grained per seat lock)
        SyncBlockCinema sb = new SyncBlockCinema();
        start = System.nanoTime();
        ExecutorService pool2 = Executors.newFixedThreadPool(16);
        for (int i = 0; i < THREADS; i++) {
            final int s = i % SEATS;
            pool2.submit(() -> sb.book(s));
        }
        pool2.shutdown();
        pool2.awaitTermination(10, TimeUnit.SECONDS);
        long durSb = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("2. Synchronized Block (Seat): %4d ms (Seats sold: %d)%n", durSb, sb.count.get());

        // Test 3: AtomicBoolean[] (CAS)
        AtomicCinema ac = new AtomicCinema();
        start = System.nanoTime();
        ExecutorService pool3 = Executors.newFixedThreadPool(16);
        for (int i = 0; i < THREADS; i++) {
            final int s = i % SEATS;
            pool3.submit(() -> ac.book(s));
        }
        pool3.shutdown();
        pool3.awaitTermination(10, TimeUnit.SECONDS);
        long durAc = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("3. AtomicBoolean[] (CAS)    : %4d ms (Seats sold: %d)%n", durAc, ac.count.get());

        // Test 4: ReentrantLock[]
        ReentrantLockCinema rlc = new ReentrantLockCinema();
        start = System.nanoTime();
        ExecutorService pool4 = Executors.newFixedThreadPool(16);
        for (int i = 0; i < THREADS; i++) {
            final int s = i % SEATS;
            pool4.submit(() -> rlc.book(s));
        }
        pool4.shutdown();
        pool4.awaitTermination(10, TimeUnit.SECONDS);
        long durRlc = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("4. ReentrantLock Array      : %4d ms (Seats sold: %d)%n", durRlc, rlc.count.get());

        // Test 5: ConcurrentHashMap
        ConcurrentMapCinema cmc = new ConcurrentMapCinema();
        start = System.nanoTime();
        ExecutorService pool5 = Executors.newFixedThreadPool(16);
        for (int i = 0; i < THREADS; i++) {
            final int s = i % SEATS;
            final int uid = i;
            pool5.submit(() -> cmc.book(s, "User-" + uid));
        }
        pool5.shutdown();
        pool5.awaitTermination(10, TimeUnit.SECONDS);
        long durCmc = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("5. ConcurrentHashMap        : %4d ms (Seats sold: %d)%n", durCmc, cmc.bookings.size());
    }
}

