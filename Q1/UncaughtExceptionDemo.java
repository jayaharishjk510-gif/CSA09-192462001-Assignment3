package Q1;

/**
 * Demonstrates an uncaught exception propagating out of main,
 * printing a standard JVM stack trace for line-by-line analysis.
 */
public class UncaughtExceptionDemo {

    public static void dispenseCash(double amount) {
        // Line 9: Deliberately throws unchecked exception without catch
        if (amount > 10000) {
            throw new ArithmeticException("Daily cash limit exceeded: Rs. " + amount);
        }
        System.out.println("Cash dispensed: " + amount);
    }

    public static void executeTransaction(String amtStr) {
        // Line 17: Converts and delegates
        double amt = Double.parseDouble(amtStr);
        dispenseCash(amt);
    }

    public static void main(String[] args) {
        // Line 23: Entry point
        System.out.println("Starting ATM transaction without outer try-catch...");
        executeTransaction("50000"); // Will trigger uncaught ArithmeticException
    }
}
