package exception;

public class InsufficientFundsException extends BankException {

    private final long shortfall;

    public InsufficientFundsException(String message, long shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public InsufficientFundsException(long shortfall) {
        this("Insufficient funds for this withdrawal.", shortfall);
    }

    public long getShortfall() {
        return shortfall;
    }
}
