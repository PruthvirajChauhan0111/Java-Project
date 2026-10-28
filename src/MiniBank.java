import java.util.Scanner;

public class MiniBank {

    private static final BankInfo BANK = new BankInfo("MiniBank", "Main Branch");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            printHeader();
            printMenu();

            boolean running = true;
            while (running) {
                int choice = readChoice(scanner);
                running = handleChoice(choice);
                if (running) {
                    printMenu();
                }
            }
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
