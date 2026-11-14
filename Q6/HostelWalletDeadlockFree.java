package Q6;

/**
 * Deadlock-Free Hostel Wallet Transfer using Global Consistent Lock Ordering.
 * Breaks the Circular Wait condition by always acquiring locks in ascending account ID order.
 */
public class HostelWalletDeadlockFree {

    static class Wallet {
        final int id;
        double balance;

        Wallet(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    public static void transferSafe(Wallet from, Wallet to, double amount) {
        String threadName = Thread.currentThread().getName();

        // Consistent Lock Ordering: Always lock lower ID wallet first
        Wallet firstLock = from.id < to.id ? from : to;
        Wallet secondLock = from.id < to.id ? to : from;

        synchronized (firstLock) {
            System.out.printf("[%s] Acquired 1st Lock on Wallet #%d%n", threadName, firstLock.id);
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}

            synchronized (secondLock) {
                System.out.printf("[%s] Acquired 2nd Lock on Wallet #%d. Transferring Rs. %.2f from #%d to #%d%n",
                        threadName, secondLock.id, amount, from.id, to.id);
                if (from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;
                    System.out.printf("[%s] Transfer Complete! Wallet #%d=%.2f, Wallet #%d=%.2f%n",
                            threadName, from.id, from.balance, to.id, to.balance);
                } else {
                    System.err.printf("[%s] Insufficient funds in Wallet #%d%n", threadName, from.id);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Demonstrating Deadlock-Free Transfer (Lock Ordering) ===");
        Wallet wallet1 = new Wallet(1, 1000.0);
        Wallet wallet2 = new Wallet(2, 1000.0);

        // Even with opposing transfer directions, both threads acquire Wallet 1 then Wallet 2
        Thread studentA = new Thread(() -> transferSafe(wallet1, wallet2, 200), "Thread-Student-A");
        Thread studentB = new Thread(() -> transferSafe(wallet2, wallet1, 300), "Thread-Student-B");

        studentA.start();
        studentB.start();

        studentA.join();
        studentB.join();

        System.out.println("\nAll transfers completed successfully without deadlock!");
        System.out.printf("Final Balances -> Wallet #1: Rs. %.2f | Wallet #2: Rs. %.2f%n",
                wallet1.balance, wallet2.balance);
    }
}

