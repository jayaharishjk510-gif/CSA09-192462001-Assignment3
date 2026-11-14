package Q3;

import java.io.*;
import java.util.*;

/**
 * Q3: Daily Sales Report Generator - Automated Resource Safety with Try-With-Resources.
 * Guarantees resource closure even under failure, supports suppressed exceptions,
 * and maintains continuous logging.
 */
public class SalesReportAutoClose {

    public static void generateReport(List<String> filePaths, String outputPath) {
        System.out.println("\n[AutoClose Version] Starting report generation with try-with-resources...");

        // Resource 1: Summary Writer managed by try-with-resources
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("StoreFile,TotalQuantity,TotalSalesValue,Status\n");

            for (String filePath : filePaths) {
                System.out.println("[AutoClose Version] Processing: " + filePath);
                
                // Resource 2: File reader per store managed automatically by try-with-resources
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line = reader.readLine(); // Header
                    int storeQty = 0;
                    double storeSales = 0.0;

                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] tokens = line.split(",");
                        int qty = Integer.parseInt(tokens[2].trim());
                        double price = Double.parseDouble(tokens[3].trim());

                        storeQty += qty;
                        storeSales += (qty * price);
                    }

                    writer.write(String.format("%s,%d,%.2f,SUCCESS\n", filePath, storeQty, storeSales));
                    System.out.println("[AutoClose Version] Cleanly processed and closed: " + filePath);

                } catch (FileNotFoundException fnfe) {
                    System.err.printf("[LOG ERROR] Missing file '%s': %s%n", filePath, fnfe.getMessage());
                    writer.write(String.format("%s,0,0.00,MISSING_FILE\n", filePath));
                } catch (NumberFormatException nfe) {
                    System.err.printf("[LOG ERROR] Corrupted data in '%s': %s%n", filePath, nfe.getMessage());
                    writer.write(String.format("%s,0,0.00,DATA_CORRUPT\n", filePath));
                } catch (IOException ioe) {
                    System.err.printf("[LOG ERROR] I/O failure in '%s': %s%n", filePath, ioe.getMessage());
                    writer.write(String.format("%s,0,0.00,IO_ERROR\n", filePath));
                }
                // BufferedReader is GUARANTEED to be closed here by AutoCloseable contract
            }

            System.out.println("[AutoClose Version] Report successfully finalized and writer safely flushed.");

        } catch (IOException e) {
            System.err.println("[AutoClose Version Fatal] Could not initialize summary output file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        List<String> validFiles = Arrays.asList("Q3/store1.csv", "Q3/store2.csv", "Q3/store4.csv");
        generateReport(validFiles, "Q3/autoclose_summary_valid.csv");

        List<String> mixedFiles = Arrays.asList("Q3/store1.csv", "Q3/store3_corrupt.csv", "Q3/store4.csv");
        generateReport(mixedFiles, "Q3/autoclose_summary_mixed.csv");
    }
}
