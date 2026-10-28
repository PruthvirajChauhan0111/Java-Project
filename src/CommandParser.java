import java.util.Locale;

public class CommandParser {

    private CommandParser() {
    }

    public static Command parse(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Command line cannot be empty.");
        }

        String[] parts = line.trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Expected format: <TYPE> <ACCOUNT_NUMBER> <AMOUNT>");
        }

        TransactionType type;
        try {
            type = TransactionType.valueOf(parts[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown transaction type: " + parts[0], e);
        }

        long amount;
        try {
            amount = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: " + parts[2], e);
        }

        return new Command(type, parts[1], amount);
    }
}
