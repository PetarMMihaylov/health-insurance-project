package app.claim.model;

public enum ClaimType {
    MEDICATION_EXPENSES("Medication Expenses"), HOSPITAL_TREATMENT_EXPENSES("Hospital Treatment Expenses"), SURGERY_EXPENSES("Surgery Expenses"), DENTAL_SERVICE_EXPENSES("Dental Service Expenses");

    private final String displayName;

    ClaimType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
