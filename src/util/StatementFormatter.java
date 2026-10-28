package util;

import model.Account;

public class StatementFormatter {

    private StatementFormatter() {
    }

    public static String buildStatement(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account is required.");
        }

        String newLine = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        builder.append("========================================").append(newLine);
        builder.append("           MINIBANK STATEMENT").append(newLine);
        builder.append("========================================").append(newLine);
        builder.append("Account Number : ").append(account.getAccountNumber()).append(newLine);
        builder.append("Owner Name     : ").append(account.getOwnerName()).append(newLine);
        builder.append("Balance        : Rs. ").append(account.getBalance()).append(newLine);
        builder.append("Status         : ")
                .append(account.isActive() ? "Active" : "Inactive")
                .append(newLine);
        builder.append("========================================");
        return builder.toString();
    }
}
