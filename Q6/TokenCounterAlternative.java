package Q6;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Alternative Concept for Q6:
 * 1. ArrayBlockingQueue for Producer-Consumer (no manual wait/notify)
 * 2. tryLock(timeout) for Wallet Transfer (prevents deadlock via timeout/retry)
 */
public class TokenCounterAlternative {

    // 1. ArrayBlockingQueue Producer-Consumer
    public static void testBlockingQueue() throws InterruptedException {
        System.out.println("--- 1. Producer-Consumer using ArrayBlockingQueue(5) ---");
        ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

        Thread cook = new Thread(() -> {
            try {
                for (int t = 1; t <= 10; t++) {
                    queue.put(t);
                    System.out.println("[Cook] Put token: " + t + " (Queue size: " + queue.size() + ")");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread student = new Thread(() -> {
            try {
                for (int k = 1; k <= 10; k++) {
                    int token = queue.take();
                    System.out.println("[Student] Collected token: " + token + " (Queue size: " + queue.size() + ")");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        cook.start();
        student.start();
        cook.join();
        student.join();
    }

    // 2. Deadlock avoidance using ReentrantLock.tryLock() with backoff
    static class LockWallet {
        final int id;
        final ReentrantLock lock = new ReentrantLock();
        double balance;

        LockWallet(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }

        public static boolean transferWithTryLock(LockWallet from, LockWallet to, double amount, long timeoutMs) 
                throws InterruptedException {
            long stopTime = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < stopTime) {
                if (from.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                    try {
                        if (to.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                            try {
                                if (from.balance >= amount) {
                                    from.balance -= amount;
                                    to.balance += amount;
                                    return true;
                                }
                                return false;
                            } finally {
                                to.lock.unlock();
                            }
                        }
                    } finally {
                        from.lock.unlock();
                    }
                }
                // Backoff jitter to prevent livelock
                Thread.sleep((int)(Math.random() * 10));
            }
            return false; // Timed out without deadlock!
        }
    }

    public static void main(String[] args) throws InterruptedException {
        testBlockingQueue();

        System.out.println("\n--- 2. Deadlock Prevention via ReentrantLock.tryLock() ---");
        LockWallet w1 = new LockWallet(1, 1000);
        LockWallet w2 = new LockWallet(2, 1000);

        Thread t1 = new Thread(() -> {
            try {
                boolean ok = LockWallet.transferWithTryLock(w1, w2, 200, 1000);
                System.out.println("[T1 Transfer w1->w2]: " + (ok ? "SUCCESS" : "TIMEOUT/FAIL"));
            } catch (InterruptedException ignored) {}
        });

        Thread t2 = new Thread(() -> {
            try {
                boolean ok = LockWallet.transferWithTryLock(w2, w1, 300, 1000);
                System.out.println("[T2 Transfer w2->w1]: " + (ok ? "SUCCESS" : "TIMEOUT/FAIL"));
            } catch (InterruptedException ignored) {}
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.printf("Final Balances with tryLock: W1=%.2f, W2=%.2f%n", w1.balance, w2.balance);
    }
}
