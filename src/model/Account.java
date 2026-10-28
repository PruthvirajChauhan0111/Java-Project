package model;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.annotation.Id;
import model.annotation.Positive;

public abstract class Account implements Transactable, InterestBearing {

    private static long accountCounter = 0;

    @Id
    private final String accountNumber;
    private String ownerName;
    @Positive
    private long balance;
    private boolean active;

    public Account(String ownerName, long openingBalance) {
        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.balance = openingBalance;
        this.active = true;
    }

    public Account(String ownerName) {
        this(ownerName, 0);
    }

    private static String generateAccountNumber() {
        accountCounter++;
        return String.format("AC%04d", accountCounter);
    }

    @Override
    public void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero.");
        }
        balance += amount;
    }

    @Override
    public boolean withdraw(long amount)
            throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }
        if (!canWithdraw(amount)) {
            long shortfall = Math.max(0, amount - availableBalance());
            throw new InsufficientFundsException(
                    "Insufficient funds for this withdrawal.", shortfall);
        }
        balance -= amount;
        return true;
    }

    public void transfer(Account to, long amount) throws BankException {
        try {
            if (to == null) {
                throw new AccountNotFoundException("Destination account not found.");
            }
            withdraw(amount);
            to.deposit(amount);
        } catch (InsufficientFundsException e) {
            throw new BankException("Transfer failed: " + e.getMessage(), e);
        } catch (InvalidAmountException e) {
            throw new BankException("Transfer failed: " + e.getMessage(), e);
        } finally {
            System.out.println("  Transfer attempt of Rs. " + amount + " finished.");
        }
    }

    protected long availableBalance() {
        return balance;
    }

    @Override
    public abstract double interestRate();

    public abstract boolean canWithdraw(long amount);

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return String.format("Account[number=%s, owner=%s, balance=%d]",
                accountNumber, ownerName, balance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account other)) {
            return false;
        }
        return accountNumber.equals(other.accountNumber);
    }

    @Override
    public int hashCode() {
        return accountNumber.hashCode();
    }
}
