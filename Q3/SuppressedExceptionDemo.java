package Q3;

/**
 * Demonstrates how try-with-resources preserves primary exceptions
 * and captures exceptions occurring during close() as 'suppressed' exceptions.
 */
public class SuppressedExceptionDemo {

    static class FaultyResource implements AutoCloseable {
        private final String name;

        FaultyResource(String name) {
            this.name = name;
        }

        public void doWork() throws Exception {
            System.out.println("Executing work on " + name);
            throw new RuntimeException("Primary failure during data calculation in " + name);
        }

        @Override
        public void close() throws Exception {
            System.out.println("Attempting to close " + name);
            throw new RuntimeException("Secondary failure while closing " + name);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Demonstrating Suppressed Exceptions in Try-With-Resources ===");
        try (FaultyResource res = new FaultyResource("SalesDatabaseConnection")) {
            res.doWork();
        } catch (Exception e) {
            System.err.println("Main Caught Exception: " + e.getMessage());
            Throwable[] suppressed = e.getSuppressed();
            System.err.println("Number of Suppressed Exceptions: " + suppressed.length);
            for (Throwable s : suppressed) {
                System.err.println(" -> Suppressed Exception: " + s.getMessage());
            }
        }
    }
}

