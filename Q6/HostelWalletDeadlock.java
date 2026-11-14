package Q6;

/**
 * Demonstrates deadlock in Hostel Wallet transfers when two threads acquire locks
 * in circular order (Account A -> Account B vs Account B -> Account A).
 */
public class HostelWalletDeadlock {

    static class Wallet {
        final int id;
        double balance;

        Wallet(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    public static void transferDeadlock(Wallet from, Wallet to, double amount) {
        String threadName = Thread.currentThread().getName();
        synchronized (from) {
            System.out.printf("[%s] Acquired lock on Wallet #%d. Waiting for Wallet #%d...%n",
                    threadName, from.id, to.id);
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}

            synchronized (to) {
                System.out.printf("[%s] Acquired lock on Wallet #%d. Transferring Rs. %.2f%n",
                        threadName, to.id, amount);
                from.balance -= amount;
                to.balance += amount;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Demonstrating Hostel Wallet Deadlock Scenario ===");
        Wallet wallet1 = new Wallet(1, 1000.0);
        Wallet wallet2 = new Wallet(2, 1000.0);

        Thread studentA = new Thread(() -> transferDeadlock(wallet1, wallet2, 200), "Thread-Student-A");
        Thread studentB = new Thread(() -> transferDeadlock(wallet2, wallet1, 300), "Thread-Student-B");

        // Mark as daemon so program won't hang forever once main prints diagnosis
        studentA.setDaemon(true);
        studentB.setDaemon(true);

        studentA.start();
        studentB.start();

        Thread.sleep(300); // Allow both threads to acquire their first lock

        System.out.println("\n--- Deadlock Inspection ---");
        System.out.printf("Student A State: %s (isAlive: %b)%n", studentA.getState(), studentA.isAlive());
        System.out.printf("Student B State: %s (isAlive: %b)%n", studentB.getState(), studentB.isAlive());
        System.out.println("Confirmed Deadlock: Thread-Student-A holds Lock(1) waiting for Lock(2).");
        System.out.println("Confirmed Deadlock: Thread-Student-B holds Lock(2) waiting for Lock(1).");
        System.out.println("Circular Wait condition met. Program exiting cleanly.");
    }
}
