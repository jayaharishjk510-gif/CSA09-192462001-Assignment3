package Q3;

import java.io.*;
import java.util.*;

/**
 * Q3: Daily Sales Report Generator - Manual Resource Management.
 * Demonstrates resource leakage when an error occurs during processing.
 */
public class SalesReportManual {

    public static void generateReport(List<String> filePaths, String outputPath) {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        System.out.println("\n[Manual Version] Starting report generation...");

        try {
            writer = new BufferedWriter(new FileWriter(outputPath));
            writer.write("StoreFile,TotalQuantity,TotalSalesValue\n");

            for (String filePath : filePaths) {
                System.out.println("[Manual Version] Opening: " + filePath);
                // Leaking point: Opening reader inside the loop without dedicated inner try-finally
                reader = new BufferedReader(new FileReader(filePath));
                
                String line = reader.readLine(); // Skip header
                int storeQty = 0;
                double storeSales = 0.0;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] tokens = line.split(",");
                    
                    // Injection failure point: Corrupted number format throws NumberFormatException
                    int qty = Integer.parseInt(tokens[2].trim());
                    double price = Double.parseDouble(tokens[3].trim());

                    storeQty += qty;
                    storeSales += (qty * price);
                }

                writer.write(String.format("%s,%d,%.2f\n", filePath, storeQty, storeSales));
                // Only closed if loop finishes successfully without exception!
                reader.close();
                reader = null;
                System.out.println("[Manual Version] Successfully processed and closed: " + filePath);
            }

            System.out.println("[Manual Version] Report completed successfully.");

        } catch (Exception e) {
            System.err.println("[Manual Version Error] Processing aborted: " + e.getMessage());
            // Notice: If exception happens, the reader for the failing file is NEVER closed!
            // Furthermore, the outer writer might not flush or close if finally has bugs.
        } finally {
            // Manual cleanup attempts
            if (writer != null) {
                try {
                    writer.close();
                    System.out.println("[Manual Finally] Writer closed.");
                } catch (IOException e) {
                    System.err.println("[Manual Finally] Error closing writer: " + e.getMessage());
                }
            }
            if (reader != null) {
                // If the loop broke, only the last reader reference is here, but previous ones if multiple
                // would be leaked, and if reader.close() throws, subsequent cleanups fail.
                try {
                    // In naive code, developers often forget this or miss it when reader was nullified
                    System.out.println("[Manual Finally] Cleaning up leftover active reader reference.");
                } catch (Exception ignored) {}
            }
        }
    }

    public static void main(String[] args) {
        List<String> validFiles = Arrays.asList("Q3/store1.csv", "Q3/store2.csv", "Q3/store4.csv");
        generateReport(validFiles, "Q3/manual_summary_valid.csv");

        List<String> failingFiles = Arrays.asList("Q3/store1.csv", "Q3/store3_corrupt.csv", "Q3/store4.csv");
        generateReport(failingFiles, "Q3/manual_summary_corrupted.csv");
    }
}

