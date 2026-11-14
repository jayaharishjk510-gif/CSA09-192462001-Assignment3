package Q2;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// Base Custom Exception (Checked)
class PortalException extends Exception {
    public PortalException(String message) { super(message); }
    public PortalException(String message, Throwable cause) { super(message, cause); }
}

// Subclasses for specific domain failures
class ExamClosedException extends PortalException {
    public ExamClosedException(String message) { super(message); }
}

class StudentNotRegisteredException extends PortalException {
    public StudentNotRegisteredException(String message) { super(message); }
}

class SubmissionStorageFailureException extends PortalException {
    public SubmissionStorageFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Storage Layer: Interacts with physical disk / storage system.
 */
class FileStorageService {
    private static final long MAX_FILE_SIZE_KB = 500;

    public void storeFile(String studentId, String courseCode, String fileName, long sizeKb) 
            throws SubmissionStorageFailureException {
        try {
            // Low-level hardware/OS check
            if (sizeKb > MAX_FILE_SIZE_KB) {
                throw new IOException("Operating System Disk Quota Exceeded. Requested: " + sizeKb + "KB, Max: " + MAX_FILE_SIZE_KB + "KB");
            }
            // Simulate saving
            System.out.printf("[Storage Layer] File '%s' (%d KB) safely written to server disk.%n", fileName, sizeKb);
        } catch (IOException ioException) {
            // Exception Chaining: Wrap low-level IOException in domain-specific SubmissionStorageFailureException
            throw new SubmissionStorageFailureException("Failed to persist answer script for " + studentId, ioException);
        }
    }
}

/**
 * Service Layer: Encapsulates business validation rules.
 */
class ExamSubmissionService {
    private final FileStorageService storageService = new FileStorageService();
    private final Set<String> registeredStudents = new HashSet<>();
    private final LocalDateTime examEndTime;

    public ExamSubmissionService(LocalDateTime examEndTime) {
        this.examEndTime = examEndTime;
        registeredStudents.add("STU202401");
        registeredStudents.add("STU202402");
    }

    public void processSubmission(String studentId, String courseCode, String fileName, long sizeKb, LocalDateTime submissionTime)
            throws ExamClosedException, StudentNotRegisteredException, SubmissionStorageFailureException {
        
        // Rule 1: Check exam window
        if (submissionTime.isAfter(examEndTime)) {
            throw new ExamClosedException("Submission rejected: Examination window closed at " + examEndTime);
        }

        // Rule 2: Check course registration
        if (!registeredStudents.contains(studentId)) {
            throw new StudentNotRegisteredException("Student " + studentId + " is not registered for course " + courseCode);
        }

        // Rule 3: Delegate to storage layer
        storageService.storeFile(studentId, courseCode, fileName, sizeKb);
    }
}

/**
 * Controller Layer: HTTP/UI request boundary that catches business exceptions and maps them to user-friendly messages.
 */
public class ExamPortalSystem {
    private final ExamSubmissionService service;

    public ExamPortalSystem(LocalDateTime examEndTime) {
        this.service = new ExamSubmissionService(examEndTime);
    }

    public void handleStudentSubmission(String studentId, String courseCode, String fileName, long sizeKb, LocalDateTime time) {
        System.out.println("\n-------------------------------------------------------------");
        System.out.printf("Incoming Submission: Student=%s, Course=%s, File=%s (%d KB)%n", studentId, courseCode, fileName, sizeKb);
        try {
            service.processSubmission(studentId, courseCode, fileName, sizeKb, time);
            System.out.println(">>> HTTP 200 OK: Submission successfully accepted and receipt generated.");
        } catch (ExamClosedException e) {
            System.err.println(">>> HTTP 403 Forbidden: " + e.getMessage());
            System.err.println("    Student Notice: The portal has closed. Please contact the Controller of Examinations.");
        } catch (StudentNotRegisteredException e) {
            System.err.println(">>> HTTP 401 Unauthorized: " + e.getMessage());
            System.err.println("    Student Notice: You are not enrolled in this subject. Please verify your enrollment status.");
        } catch (SubmissionStorageFailureException e) {
            System.err.println(">>> HTTP 500 Internal Server Error: " + e.getMessage());
            System.err.println("    Root Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "None"));
            System.err.println("    Student Notice: Storage failure occurred. Please retry or contact technical helpdesk.");
            System.err.println("    [Internal Log] Chained Stack Trace:");
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        LocalDateTime examEnd = LocalDateTime.of(2026, 11, 14, 15, 0, 0);
        ExamPortalSystem portal = new ExamPortalSystem(examEnd);

        // Scenario 1: Valid submission
        portal.handleStudentSubmission("STU202401", "CS301", "answersheet_stu1.pdf", 320, 
                LocalDateTime.of(2026, 11, 14, 14, 30, 0));

        // Scenario 2: Student not registered
        portal.handleStudentSubmission("STU999999", "CS301", "answersheet_unreg.pdf", 250, 
                LocalDateTime.of(2026, 11, 14, 14, 45, 0));

        // Scenario 3: Exam window closed
        portal.handleStudentSubmission("STU202402", "CS301", "answersheet_late.pdf", 200, 
                LocalDateTime.of(2026, 11, 14, 15, 15, 0));

        // Scenario 4: Storage failure (quota exceeded) demonstrating chaining
        portal.handleStudentSubmission("STU202402", "CS301", "huge_scanned_sheet.pdf", 950, 
                LocalDateTime.of(2026, 11, 14, 14, 50, 0));
    }
}
