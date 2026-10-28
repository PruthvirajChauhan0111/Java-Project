package model;

import java.io.Serializable;

public class CurrentAccount extends Account implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long overdraftLimit;

    public CurrentAccount(String ownerName, long openingBalance, long overdraftLimit) {
        super(ownerName, openingBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public double interestRate() {
        return 0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return getBalance() - amount >= -overdraftLimit;
    }

    @Override
    protected long availableBalance() {
        return getBalance() + overdraftLimit;
    }

    public long getOverdraftLimit() {
        return overdraftLimit;
    }
}
