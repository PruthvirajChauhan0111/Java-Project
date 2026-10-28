public enum MenuOption {
    OPEN_ACCOUNT("Open Account"),
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw"),
    TRANSFER("Transfer"),
    EXIT("Exit");

    private final String label;

    MenuOption(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
