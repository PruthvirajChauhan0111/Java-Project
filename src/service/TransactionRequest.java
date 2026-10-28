package service;

import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.Account;
import model.TransactionType;

public record TransactionRequest(Account account, TransactionType type, long amount) {

    public void execute() throws InsufficientFundsException, InvalidAmountException {
        if (type == TransactionType.DEPOSIT) {
            account.deposit(amount);
        } else if (type == TransactionType.WITHDRAW) {
            account.withdraw(amount);
        } else {
            throw new InvalidAmountException("Unsupported transaction type: " + type);
        }
    }
}
