package app.user.model;

public enum CompanyName {

    LOCAL_GROUP_LTD("Local Group Ltd."),
    NEURO_NEST("Neuro Nest"),
    SOLAR_BLOOM("Solar Bloom"),

    HEALTH_INSURANCE_INC("Health Insurance Inc.");

    private final String displayName;

    CompanyName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

