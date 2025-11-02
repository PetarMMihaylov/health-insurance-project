package app.transaction.model;

public enum TransactionStatus {
    COMPLETED("Completed"), FAILED("Failed");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
