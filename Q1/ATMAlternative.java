package Q1;

import java.io.File;
import java.util.Scanner;

/**
 * Q1 Alternative Implementation:
 * Avoids exceptions by checking every condition in advance using Scanner/pre-conditions
 * and returning integer status codes.
 */
public class ATMAlternative {

    public static final int SUCCESS = 0;
    public static final int ERR_CARD_READER_MISSING = -1;
    public static final int ERR_INVALID_ACCOUNT = -2;
    public static final int ERR_NON_NUMERIC_AMOUNT = -3;
    public static final int ERR_NEGATIVE_OR_ZERO_AMOUNT = -4;
    public static final int ERR_INVALID_DENOMINATION = -5;
    public static final int ERR_INSUFFICIENT_FUNDS = -6;

    private static final String VALID_ACCOUNT = "ACC1001";
    private static double balance = 25000.00;

    /**
     * Attempts withdrawal using explicit conditionals and error codes.
     */
    public static int processWithdrawalWithCodes(String cardConfigFile, String accountInput, String amountInput) {
        // Check 1: File existence check
        File cardFile = new File(cardConfigFile);
        if (!cardFile.exists()) {
            return ERR_CARD_READER_MISSING;
        }

        // Check 2: Account input validation
        if (accountInput == null || !accountInput.equals(VALID_ACCOUNT)) {
            return ERR_INVALID_ACCOUNT;
        }

        // Check 3: Scanner inspection without throwing exceptions
        Scanner amountScanner = new Scanner(amountInput);
        if (!amountScanner.hasNextDouble()) {
            amountScanner.close();
            return ERR_NON_NUMERIC_AMOUNT;
        }

        double amount = amountScanner.nextDouble();
        amountScanner.close();

        // Check 4: Positive amount check
        if (amount <= 0) {
            return ERR_NEGATIVE_OR_ZERO_AMOUNT;
        }

        // Check 5: Denomination check
        if (amount % 100 != 0) {
            return ERR_INVALID_DENOMINATION;
        }

        // Check 6: Balance check
        if (amount > balance) {
            return ERR_INSUFFICIENT_FUNDS;
        }

        // State update
        balance -= amount;
        return SUCCESS;
    }

    public static void main(String[] args) {
        System.out.println("=== ATM Alternative Implementation (Error Codes & Pre-checks) ===");
        
        String[] testAmounts = {"2000", "invalid_amt", "-500", "90000"};
        for (String amt : testAmounts) {
            int code = processWithdrawalWithCodes("card_reader.cfg", "ACC1001", amt);
            System.out.printf("Withdrawal for '%s' -> Result Code: %d (%s)%n", 
                amt, code, explainCode(code));
        }
    }

    private static String explainCode(int code) {
        return switch (code) {
            case SUCCESS -> "SUCCESS: Cash dispensed.";
            case ERR_CARD_READER_MISSING -> "ERROR: Card reader missing.";
            case ERR_INVALID_ACCOUNT -> "ERROR: Invalid account.";
            case ERR_NON_NUMERIC_AMOUNT -> "ERROR: Amount is non-numeric.";
            case ERR_NEGATIVE_OR_ZERO_AMOUNT -> "ERROR: Amount <= 0.";
            case ERR_INVALID_DENOMINATION -> "ERROR: Not multiple of 100.";
            case ERR_INSUFFICIENT_FUNDS -> "ERROR: Insufficient funds.";
            default -> "UNKNOWN CODE";
        };
    }
}

