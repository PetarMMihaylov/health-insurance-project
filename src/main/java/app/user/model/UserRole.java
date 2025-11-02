package app.user.model;

public enum UserRole {

    ADMIN("Admin"),
    POLICYHOLDER("Policyholder");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
