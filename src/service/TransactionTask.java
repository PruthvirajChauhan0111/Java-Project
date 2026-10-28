package service;

import exception.BankException;

public class TransactionTask implements Runnable {

    private final TransactionRequest request;

    public TransactionTask(TransactionRequest request) {
        this.request = request;
    }

    @Override
    public void run() {
        try {
            request.execute();
            System.out.println(Thread.currentThread().getName()
                    + " completed " + request.type() + " of Rs. " + request.amount());
        } catch (BankException e) {
            System.out.println(Thread.currentThread().getName()
                    + " failed: " + e.getMessage());
        }
    }
}
