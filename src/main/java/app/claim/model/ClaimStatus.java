package app.claim.model;

public enum ClaimStatus {
    OPEN("Open"), FOR_REVIEW("For Review"), APPROVED("Approved"), REJECTED("Rejected");

    private final String displayName;

    ClaimStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

