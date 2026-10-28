package service;

import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import model.Account;
import model.BankInfo;
import model.Command;
import model.CurrentAccount;
import model.FixedDepositAccount;
import model.MenuOption;
import model.SavingsAccount;
import model.WithdrawRule;
import util.AnnotationValidator;
import util.AuditLog;
import util.CommandParser;
import util.ReportGenerator;
import util.StatementFormatter;
import util.StatePersister;
import util.TransactionLog;
import util.Validator;

import static util.Validator.isValidEmail;

public class MiniBank {

    private static final BankInfo BANK = new BankInfo("MiniBank", "Main Branch");

    public static void main(String[] args) throws InterruptedException {
        try (Scanner scanner = new Scanner(System.in);
                AuditLog auditLog = new AuditLog("MiniBank session")) {
            printHeader();
            auditLog.record("Session started.");

            Account[] accounts = {
                new SavingsAccount("Asha Verma", 5000, 1000),
                new CurrentAccount("Rahul Mehta", 0, 5000),
                new FixedDepositAccount("Priya Nair", 12000)
            };

            System.out.println();
            System.out.println("Transaction processing:");
            try {
                accounts[0].deposit(2500);
                accounts[1].deposit(1000);
                accounts[1].withdraw(4000);
                accounts[2].deposit(3000);
            } catch (InvalidAmountException e) {
                System.out.println("  Invalid amount: " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.out.println("  Withdrawal failed: " + e.getMessage()
                        + " (shortfall: Rs. " + e.getShortfall() + ")");
            } finally {
                System.out.println("  Transaction batch finished.");
            }

            try {
                accounts[2].withdraw(5000);
            } catch (InsufficientFundsException e) {
                System.out.println("  Locked account withdrawal failed: " + e.getMessage()
                        + " (shortfall: Rs. " + e.getShortfall() + ")");
            } catch (InvalidAmountException e) {
                System.out.println("  Invalid withdrawal: " + e.getMessage());
            }

            try {
                accounts[1].deposit(0);
            } catch (InvalidAmountException e) {
                System.out.println("  Invalid deposit rejected: " + e.getMessage());
            }

            try {
                accounts[0].transfer(accounts[1], 1000);
                System.out.println("  Transfer successful.");
            } catch (BankException e) {
                System.out.println("  Transfer failed: " + e.getMessage());
            }

            System.out.println();
            System.out.println("Account summary:");
            for (Account account : accounts) {
                System.out.println("  " + account);
            }

            System.out.println();
            System.out.println("Interest rates:");
            for (Account account : accounts) {
                System.out.printf("  %s (%s): %.1f%% (yearly interest: Rs. %.2f)%n",
                        account.getAccountNumber(),
                        account.getClass().getSimpleName(),
                        account.interestRate(),
                        account.yearlyInterest());
                if (account instanceof FixedDepositAccount fixedDeposit) {
                    System.out.println("    " + fixedDeposit.getAccountNumber()
                            + " is locked; withdrawals are not allowed.");
                }
            }

            System.out.println();
            System.out.println("Withdraw rules:");
            WithdrawRule noOverdraft = new WithdrawRule() {
                @Override
                public boolean allow(Account account, long amount) {
                    return account.getBalance() - amount >= 0;
                }
            };
            System.out.println("  anonymous rule allows Rs. 500 from AC0001: "
                    + noOverdraft.allow(accounts[0], 500));
            WithdrawRule accountRule = (account, amount) -> account.canWithdraw(amount);
            System.out.println("  lambda rule allows Rs. 100 from AC0003: "
                    + accountRule.allow(accounts[2], 100));

            System.out.println();
            System.out.println("Equality check:");
            System.out.println("  accounts[0].equals(accounts[0]) = "
                    + accounts[0].equals(accounts[0]));
            System.out.println("  accounts[0].equals(accounts[1]) = "
                    + accounts[0].equals(accounts[1]));

            System.out.println();
            System.out.println("Type check:");
            Object candidate = accounts[2];
            if (candidate instanceof Account account) {
                System.out.println("  " + account + " is an Account.");
            } else {
                System.out.println("  The object is not an Account.");
            }

            System.out.println();
            System.out.println("Validation check:");
            System.out.println("  mobile 9876543210 -> " + Validator.isValidMobile("9876543210"));
            System.out.println("  mobile 12345 -> " + Validator.isValidMobile("12345"));
            System.out.println("  email asha@example.com -> "
                    + isValidEmail("asha@example.com"));
            System.out.println("  email asha.example.com -> "
                    + isValidEmail("asha.example.com"));
            System.out.println("  PAN ABCDE1234F -> " + Validator.isValidPan("ABCDE1234F"));
            System.out.println("  PAN ABC1234F -> " + Validator.isValidPan("ABC1234F"));
            System.out.println("  IFSC SBIN0001234 -> " + Validator.isValidIfsc("SBIN0001234"));
            System.out.println("  IFSC SBIN1001234 -> " + Validator.isValidIfsc("SBIN1001234"));

            System.out.println();
            System.out.println("Command parsing:");
            Command command = CommandParser.parse("DEPOSIT AC0001 500");
            System.out.println("  Type: " + command.type());
            System.out.println("  Account: " + command.accountNumber());
            System.out.println("  Amount: " + command.amount());

            System.out.println();
            System.out.println(StatementFormatter.buildStatement(accounts[0]));

            System.out.println();
            System.out.println("Annotation validation:");
            Account invalidAccount = new SavingsAccount("Negative Balance", -500, 0);
            String[] annotationErrors = AnnotationValidator.validate(invalidAccount);
            for (String error : annotationErrors) {
                System.out.println("  " + error);
            }

            System.out.println();
            System.out.println("Thread safety demo:");
            Account sharedAccount = new SavingsAccount("Shared Account", 0, 0);
            Thread[] workers = new Thread[10];
            for (int i = 0; i < workers.length; i++) {
                workers[i] = new Thread(
                        new AccountWorker(sharedAccount, 10_000, 1),
                        "Worker-" + (i + 1));
                workers[i].start();
            }
            for (Thread worker : workers) {
                worker.join();
            }
            System.out.println("  Expected balance: " + (10 * 10_000));
            System.out.println("  Final balance: " + sharedAccount.getBalance());

            ConcurrencyDemo.runTransactionPoolDemo();
            ConcurrencyDemo.runProducerConsumerDemo();
            ConcurrencyDemo.runTransferOrderingDemo();

            System.out.println();
            System.out.println("Persistence and reporting:");
            Path dataDirectory = Paths.get("data");
            Path accountsFile = dataDirectory.resolve("accounts.dat");
            Path logsDirectory = dataDirectory.resolve("logs");
            Path reportFile = dataDirectory.resolve("report").resolve("eod-report.txt");
            Path logFile = logsDirectory.resolve("transactions.log");
            try {
                Files.createDirectories(logsDirectory);
                TransactionLog.append(logFile,
                        "DEPOSIT " + accounts[0].getAccountNumber() + " 2500");
                TransactionLog.append(logFile,
                        "DEPOSIT " + accounts[1].getAccountNumber() + " 1000");
                TransactionLog.append(logFile,
                        "WITHDRAW " + accounts[1].getAccountNumber() + " 4000");
                TransactionLog.append(logFile,
                        "DEPOSIT " + accounts[2].getAccountNumber() + " 3000");

                StatePersister.save(accounts, accountsFile);
                Account[] restoredAccounts = StatePersister.load(accountsFile);

                System.out.println("  Restored accounts:");
                for (Account account : restoredAccounts) {
                    System.out.println("    " + account);
                }

                String report = ReportGenerator.generate(logsDirectory, reportFile);
                System.out.println();
                System.out.println(report);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("  Persistence error: " + e.getMessage());
            }

            printMenu();

            boolean running = true;
            while (running) {
                int choice = readChoice(scanner);
                running = handleChoice(choice);
                if (running) {
                    printMenu();
                }
            }
            auditLog.record("Session completed.");
        }
    }

    private static void printHeader() {
        System.out.println("========================================");
        System.out.println("  " + BANK.name() + " - " + BANK.branch());
        System.out.println("========================================");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("Please choose an option:");
        for (MenuOption option : MenuOption.values()) {
            System.out.printf("  %d. %s%n", option.ordinal() + 1, option.label());
        }
        System.out.print("Enter choice: ");
    }

    private static int readChoice(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print("Invalid input. Enter a number: ");
        }
        return scanner.nextInt();
    }

    private static boolean handleChoice(int choice) {
        MenuOption option = switch (choice) {
            case 1 -> MenuOption.OPEN_ACCOUNT;
            case 2 -> MenuOption.DEPOSIT;
            case 3 -> MenuOption.WITHDRAW;
            case 4 -> MenuOption.TRANSFER;
            case 5 -> MenuOption.EXIT;
            default -> null;
        };

        if (option == null) {
            System.out.println("Invalid option. Please choose between 1 and 5.");
            return true;
        }

        String message = switch (option) {
            case OPEN_ACCOUNT -> "Open Account - to be implemented in a later lab.";
            case DEPOSIT -> "Deposit - to be implemented in a later lab.";
            case WITHDRAW -> "Withdraw - to be implemented in a later lab.";
            case TRANSFER -> "Transfer - to be implemented in a later lab.";
            case EXIT -> "Thank you for banking with " + BANK.name() + ". Goodbye!";
        };

        System.out.println(message);
        return option != MenuOption.EXIT;
    }
}
