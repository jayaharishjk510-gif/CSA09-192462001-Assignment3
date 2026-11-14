package Q1;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Q1: ATM Withdrawal Simulation
 * Demonstrates exception handling with checked/unchecked exceptions,
 * multi-catch blocks (with disjoint sibling exceptions), and guaranteed cleanup via finally.
 */
public class ATMWithdrawal {

    private static final String VALID_ACCOUNT = "ACC1001";
    private static double balance = 25000.00;

    /**
     * Simulates reading physical card reader configuration file.
     * @throws FileNotFoundException if hardware card-reader definition file is missing (Checked Exception)
     */
    public static void verifyCardReaderHardware(String cardReaderPath) throws FileNotFoundException {
        File cardFile = new File(cardReaderPath);
        if (!cardFile.exists()) {
            throw new FileNotFoundException("Card reader hardware config missing: " + cardReaderPath);
        }
    }

    /**
     * Executes the withdrawal process given input parameters.
     * Catches and classifies distinct failures.
     */
    public static void processWithdrawal(String cardReaderConfigFile, String accountInput, String amountInput) {
        System.out.println("\n=======================================================");
        System.out.println("Processing Withdrawal Request:");
        System.out.println("Card Config: " + cardReaderConfigFile + " | Account: " + accountInput + " | Amount: " + amountInput);
        System.out.println("-------------------------------------------------------");

        try {
            // Step 1: Verify Hardware / Card Reader File (Checked Exception)
            verifyCardReaderHardware(cardReaderConfigFile);
            System.out.println("[Step 1 Passed] Card reader hardware initialized.");

            // Step 2: Validate Account Number Format and Existence
            if (accountInput == null || accountInput.trim().isEmpty()) {
                throw new IllegalArgumentException("Account number cannot be empty.");
            }
            if (!accountInput.matches("[a-zA-Z0-9]+")) {
                throw new IllegalArgumentException("Account number contains invalid characters: " + accountInput);
            }
            if (!accountInput.equals(VALID_ACCOUNT)) {
                throw new SecurityException("Account not found or access denied: " + accountInput);
            }
            System.out.println("[Step 2 Passed] Account authenticated: " + accountInput);

            // Step 3: Parse and Validate Withdrawal Amount
            double amount;
            try {
                amount = Double.parseDouble(amountInput);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Non-numeric withdrawal amount entered: '" + amountInput + "'", nfe);
            }

            // Step 4: Validate Amount Range
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be strictly positive (> 0). Entered: " + amount);
            }
            if (amount % 100 != 0) {
                throw new IllegalArgumentException("Amount must be a multiple of 100 for ATM cash dispenser: " + amount);
            }

            // Step 5: Check Account Balance (ArithmeticException)
            if (amount > balance) {
                throw new ArithmeticException("Insufficient funds! Requested: " + amount + ", Available: " + balance);
            }

            // Step 6: Dispense Cash
            balance -= amount;
            System.out.println("[Step 3 Passed] Cash Dispensed: Rs. " + amount);
            System.out.println("Remaining Balance: Rs. " + balance);

        } catch (FileNotFoundException fnfe) {
            // Checked Hardware Error
            System.err.println("[CATCH 1 - Checked Hardware Error]: " + fnfe.getMessage());
            System.err.println("Action: ATM switching to Out-of-Service mode.");

        } catch (IllegalArgumentException | SecurityException e) {
            // Disjoint sibling unchecked exceptions demonstrating Java 7+ multi-catch
            System.err.println("[CATCH 2 - Multi-Catch: Input Validation / Security Failure]: " + e.getMessage());
            System.err.println("Action: Transaction rejected. Please re-check account/amount details.");

        } catch (ArithmeticException ae) {
            // Business Logic Rule Violation (Unchecked)
            System.err.println("[CATCH 3 - Balance Rule Violation]: " + ae.getMessage());
            System.err.println("Action: Transaction cancelled. Please enter a lower amount.");

        } catch (Exception e) {
            // Fallback for any unanticipated checked/unchecked exception
            System.err.println("[CATCH 4 - General Fallback]: Unexpected error: " + e.getMessage());

        } finally {
            // Guaranteed Cleanup
            System.out.println("[FINALLY BLOCK] Guarantee: Ejecting card, clearing secure PIN buffer, logging audit trail.");
            System.out.println("Session closed cleanly. Active Session = false.");
        }
    }

    public static void main(String[] args) {
        // Create dummy card reader file
        try {
            File dummyCard = new File("card_reader.cfg");
            if (!dummyCard.exists()) {
                dummyCard.createNewFile();
            }
        } catch (Exception ignored) {}

        System.out.println("==================================================================");
        System.out.println("   ASSIGNMENT 3 - Q1: BANK ATM SIMULATION TEST SUITE EXECUTION   ");
        System.out.println("==================================================================");

        // Test Case 1: Valid Transaction
        processWithdrawal("card_reader.cfg", "ACC1001", "2000");

        // Test Case 2: Letter input instead of number
        processWithdrawal("card_reader.cfg", "ACC1001", "five_thousand");

        // Test Case 3: Negative amount input
        processWithdrawal("card_reader.cfg", "ACC1001", "-500");

        // Test Case 4: Overdraft / Insufficient Funds
        processWithdrawal("card_reader.cfg", "ACC1001", "90000");

        // Test Case 5: Missing Card Reader Hardware File
        processWithdrawal("missing_hardware_reader.cfg", "ACC1001", "1000");
    }
}
