package app.policy.model;

public enum PolicyType {
    COMFORT("Comfort"), STANDARD("Standard"), LUX("Lux");

    private final String displayName;

    PolicyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}



