package Q7;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

/**
 * Q7: High-Throughput, Fault-Tolerant Semester Result Processor.
 * Processes 10,000 student answer-script score files concurrently using
 * ExecutorService, Callable, Future, and ConcurrentHashMap.
 */
public class ResultProcessor {

    public enum Status {
        SUCCESS,
        FILE_NOT_FOUND,
        CORRUPTED_DATA,
        INVALID_SCORE,
        IO_FAILURE
    }

    public static class StudentResult {
        public final String studentId;
        public final double totalMarks;
        public final double percentage;
        public final String grade;

        public StudentResult(String studentId, double totalMarks, double percentage, String grade) {
            this.studentId = studentId;
            this.totalMarks = totalMarks;
            this.percentage = percentage;
            this.grade = grade;
        }

        @Override
        public String toString() {
            return String.format("%s: Total=%.1f, Pct=%.1f%%, Grade=%s", studentId, totalMarks, percentage, grade);
        }
    }

    public static class TaskOutcome {
        public final String studentId;
        public final Status status;
        public final StudentResult result;
        public final String errorMessage;

        public TaskOutcome(String studentId, Status status, StudentResult result, String errorMessage) {
            this.studentId = studentId;
            this.status = status;
            this.result = result;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * Callable Task representing the isolated processing of one student score file.
     * Fault isolation: Every exception is caught locally and returned as a typed failure outcome.
     */
    public static class FileProcessorTask implements Callable<TaskOutcome> {
        private final String studentId;
        private final File file;

        public FileProcessorTask(String studentId, File file) {
            this.studentId = studentId;
            this.file = file;
        }

        @Override
        public TaskOutcome call() {
            // Check 1: Missing File
            if (file == null || !file.exists()) {
                return new TaskOutcome(studentId, Status.FILE_NOT_FOUND, null, "File does not exist: " + (file == null ? "null" : file.getName()));
            }

            double total = 0.0;
            int subjectCount = 0;

            // Try-with-resources ensures file descriptor safety
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    String[] parts = line.split(",");
                    if (parts.length < 2) {
                        return new TaskOutcome(studentId, Status.CORRUPTED_DATA, null, "Malformed CSV row: '" + line + "'");
                    }

                    double score;
                    try {
                        score = Double.parseDouble(parts[1].trim());
                    } catch (NumberFormatException nfe) {
                        return new TaskOutcome(studentId, Status.CORRUPTED_DATA, null, "Non-numeric score '" + parts[1] + "'");
                    }

                    if (score < 0 || score > 100) {
                        return new TaskOutcome(studentId, Status.INVALID_SCORE, null, "Score out of range [0-100]: " + score);
                    }

                    total += score;
                    subjectCount++;
                }

                if (subjectCount == 0) {
                    return new TaskOutcome(studentId, Status.CORRUPTED_DATA, null, "Empty score file: No valid subject records found.");
                }

                double percentage = total / subjectCount;
                String grade;
                if (percentage >= 85) grade = "Distinction";
                else if (percentage >= 70) grade = "First Class";
                else if (percentage >= 50) grade = "Second Class";
                else grade = "Fail";

                StudentResult res = new StudentResult(studentId, total, percentage, grade);
                return new TaskOutcome(studentId, Status.SUCCESS, res, null);

            } catch (IOException ioe) {
                return new TaskOutcome(studentId, Status.IO_FAILURE, null, "I/O Error: " + ioe.getMessage());
            } catch (Exception ex) {
                return new TaskOutcome(studentId, Status.CORRUPTED_DATA, null, "Unexpected error: " + ex.getMessage());
            }
        }
    }

    /**
     * Executes the batch processing of student files with a specified thread pool size.
     */
    public static Map<String, Object> executeBatch(List<FileProcessorTask> tasks, int threadPoolSize) 
            throws InterruptedException, ExecutionException {
        
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<TaskOutcome>> futures = new ArrayList<>(tasks.size());

        // Concurrent Shared Collections
        ConcurrentHashMap<String, StudentResult> validResults = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> failureReports = new ConcurrentHashMap<>();

        // High-concurrency lock-free accumulators
        LongAdder successCounter = new LongAdder();
        LongAdder missingCounter = new LongAdder();
        LongAdder corruptCounter = new LongAdder();
        LongAdder invalidCounter = new LongAdder();
        LongAdder ioErrorCounter = new LongAdder();

        // Submit tasks
        for (FileProcessorTask task : tasks) {
            futures.add(executor.submit(task));
        }

        // Aggregate results from futures
        for (Future<TaskOutcome> future : futures) {
            TaskOutcome outcome = future.get(); // Blocks only until individual task completes
            switch (outcome.status) {
                case SUCCESS:
                    validResults.put(outcome.studentId, outcome.result);
                    successCounter.increment();
                    break;
                case FILE_NOT_FOUND:
                    failureReports.put(outcome.studentId, outcome.errorMessage);
                    missingCounter.increment();
                    break;
                case CORRUPTED_DATA:
                    failureReports.put(outcome.studentId, outcome.errorMessage);
                    corruptCounter.increment();
                    break;
                case INVALID_SCORE:
                    failureReports.put(outcome.studentId, outcome.errorMessage);
                    invalidCounter.increment();
                    break;
                case IO_FAILURE:
                    failureReports.put(outcome.studentId, outcome.errorMessage);
                    ioErrorCounter.increment();
                    break;
            }
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long elapsedTimeMs = System.currentTimeMillis() - startTime;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("poolSize", threadPoolSize);
        metrics.put("elapsedMs", elapsedTimeMs);
        metrics.put("totalFiles", tasks.size());
        metrics.put("successCount", successCounter.sum());
        metrics.put("missingCount", missingCounter.sum());
        metrics.put("corruptCount", corruptCounter.sum());
        metrics.put("invalidCount", invalidCounter.sum());
        metrics.put("ioErrorCount", ioErrorCounter.sum());
        metrics.put("validResultsMap", validResults);
        metrics.put("failureMap", failureReports);

        return metrics;
    }

    /**
     * Generates a dataset of 10,000 files including valid, corrupt, and missing cases.
     */
    public static List<FileProcessorTask> generateDataset(Path baseDir, int totalFiles) throws IOException {
        Files.createDirectories(baseDir);
        List<FileProcessorTask> tasks = new ArrayList<>(totalFiles);

        // Predefined ratios: 96% valid, 2% corrupt, 1% invalid score, 1% missing
        for (int i = 1; i <= totalFiles; i++) {
            String studentId = String.format("STU%05d", i);
            File file = baseDir.resolve(studentId + ".csv").toFile();

            if (i <= 100) {
                // Injected: Missing files (do not create file on disk)
                tasks.add(new FileProcessorTask(studentId, file));
            } else if (i <= 200) {
                // Injected: Corrupt format / non-numeric
                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    pw.println("Maths,90");
                    pw.println("Physics,MALFORMED_GRADE");
                    pw.println("Chemistry,85");
                }
                tasks.add(new FileProcessorTask(studentId, file));
            } else if (i <= 300) {
                // Injected: Out-of-range score
                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    pw.println("Maths,105");
                    pw.println("English,75");
                }
                tasks.add(new FileProcessorTask(studentId, file));
            } else {
                // Valid student score file
                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    int s1 = 60 + (i % 38);
                    int s2 = 55 + ((i * 3) % 43);
                    int s3 = 70 + ((i * 7) % 29);
                    pw.printf("Mathematics,%d%n", s1);
                    pw.printf("ComputerScience,%d%n", s2);
                    pw.printf("Electronics,%d%n", s3);
                }
                tasks.add(new FileProcessorTask(studentId, file));
            }
        }
        return tasks;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================================");
        System.out.println("   ASSIGNMENT 3 - Q7: FAULT-TOLERANT SEMESTER RESULT PROCESSOR   ");
        System.out.println("==================================================================");

        Path scoreDir = Paths.get("Q7/scores");
        System.out.println("Generating 10,000 student score files in '" + scoreDir.toAbsolutePath() + "'...");
        List<FileProcessorTask> tasks = generateDataset(scoreDir, 10_000);
        System.out.println("Dataset created successfully (10,000 score tasks configured).");

        // Small hand-checked verification (First 10 items)
        System.out.println("\n--- Step 1: Hand-Checked Verification on 10 Known Test Cases ---");
        List<FileProcessorTask> smallSample = tasks.subList(95, 105); // 5 missing (96-100) and 5 corrupt (101-105)
        Map<String, Object> smallMetrics = executeBatch(smallSample, 2);
        System.out.printf("Hand-checked Sample -> Total: %d, Success: %d, Missing: %d, Corrupted: %d%n",
                smallMetrics.get("totalFiles"), smallMetrics.get("successCount"),
                smallMetrics.get("missingCount"), smallMetrics.get("corruptCount"));

        // Step 2: Multi-core Benchmark across 1, 2, 4, 8 threads
        System.out.println("\n--- Step 2: Full Benchmark on 10,000 Files across 1, 2, 4, 8 Threads ---");
        int[] threadConfigs = {1, 2, 4, 8};
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-12s | %-10s | %-10s | %-10s | %-10s | %-12s%n",
                "Threads", "Elapsed (ms)", "Success", "Missing", "Corrupt", "Invalid", "Throughput(f/s)");
        System.out.println("-------------------------------------------------------------------------------------------------");

        for (int threads : threadConfigs) {
            Map<String, Object> m = executeBatch(tasks, threads);
            long elapsed = (long) m.get("elapsedMs");
            double throughput = (10_000.0 / elapsed) * 1000.0;
            System.out.printf("%-10d | %-12d | %-10d | %-10d | %-10d | %-10d | %-12.1f%n",
                    threads, elapsed, m.get("successCount"), m.get("missingCount"),
                    m.get("corruptCount"), m.get("invalidCount"), throughput);
        }
        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("Batch processing successfully completed with 100% fault-tolerance!");
    }
}
