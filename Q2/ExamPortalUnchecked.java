package Q2;

/**
 * Alternative Concept for Q2:
 * Demonstrating Unchecked Exception Hierarchy (inheriting RuntimeException).
 * Shows that methods do not declare 'throws' in signatures, but callers risk
 * runtime termination if unhandled.
 */
class UncheckedExamException extends RuntimeException {
    public UncheckedExamException(String message) { super(message); }
    public UncheckedExamException(String message, Throwable cause) { super(message, cause); }
}

class UncheckedStorageException extends UncheckedExamException {
    public UncheckedStorageException(String message, Throwable cause) { super(message, cause); }
}

public class ExamPortalUnchecked {
    // Notice: No 'throws' clause needed on method signature
    public static void saveUnchecked(String fileName, long sizeKb) {
        if (sizeKb > 500) {
            // Chaining an internal exception into an unchecked exception
            throw new UncheckedStorageException("Storage write failed for " + fileName, 
                    new java.io.IOException("Unchecked: Disk Full"));
        }
        System.out.println("Unchecked Saved: " + fileName);
    }

    public static void main(String[] args) {
        try {
            saveUnchecked("test1.pdf", 200);
            saveUnchecked("test2.pdf", 800);
        } catch (UncheckedExamException uee) {
            System.out.println("Caught unchecked: " + uee.getMessage());
            System.out.println("Root cause: " + uee.getCause());
        }
    }
}

