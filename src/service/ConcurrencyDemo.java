package service;

import exception.BankException;
import model.Account;
import model.SavingsAccount;
import model.TransactionType;

public class ConcurrencyDemo {

    private ConcurrencyDemo() {
    }

    public static void runTransactionPoolDemo() {
        System.out.println();
        System.out.println("Transaction pool demo:");
        Account account = new SavingsAccount("Pool Account", 1000, 0);
        TransactionProcessor processor = new TransactionProcessor();

        for (int i = 0; i < 20; i++) {
            TransactionType type = (i % 2 == 0)
                    ? TransactionType.DEPOSIT
                    : TransactionType.WITHDRAW;
            long amount = (type == TransactionType.DEPOSIT) ? 100 : 50;
            processor.submit(new TransactionTask(new TransactionRequest(account, type, amount)));
        }

        processor.stop();
        System.out.println("  Pool account final balance: " + account.getBalance());
    }

    public static void runProducerConsumerDemo() {
        System.out.println();
        System.out.println("Producer-consumer demo:");
        Account account = new SavingsAccount("Buffer Account", 500, 0);
        TransactionBuffer buffer = new TransactionBuffer(5);
        int transactionCount = 10;

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < transactionCount; i++) {
                    TransactionType type = (i % 2 == 0)
                            ? TransactionType.DEPOSIT
                            : TransactionType.WITHDRAW;
                    buffer.produce(new TransactionRequest(account, type, 100));
                }
                System.out.println("  Producer finished.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            for (int i = 0; i < transactionCount; i++) {
                try {
                    TransactionRequest request = buffer.consume();
                    request.execute();
                    System.out.println("  Consumer processed " + request.type()
                            + " of Rs. " + request.amount());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (BankException e) {
                    System.out.println("  Consumer failed: " + e.getMessage());
                }
            }
        }, "Consumer");

        producer.start();
        consumer.start();
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("  Buffer account final balance: " + account.getBalance());
    }

    public static void runTransferOrderingDemo() {
        System.out.println();
        System.out.println("Transfer lock-ordering demo:");
        Account first = new SavingsAccount("Transfer A", 5000, 0);
        Account second = new SavingsAccount("Transfer B", 5000, 0);

        Thread firstToSecond = new Thread(
                () -> transferRepeatedly(first, second), "A-to-B");
        Thread secondToFirst = new Thread(
                () -> transferRepeatedly(second, first), "B-to-A");

        firstToSecond.start();
        secondToFirst.start();
        try {
            firstToSecond.join();
            secondToFirst.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("  Balances: A=" + first.getBalance()
                + ", B=" + second.getBalance()
                + ", total=" + (first.getBalance() + second.getBalance()));
    }

    private static void transferRepeatedly(Account from, Account to) {
        for (int i = 0; i < 5; i++) {
            try {
                from.transfer(to, 10);
            } catch (BankException e) {
                System.out.println("  " + Thread.currentThread().getName()
                        + " transfer failed: " + e.getMessage());
                return;
            }
        }
    }
}
