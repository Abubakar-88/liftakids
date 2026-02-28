package org.liftakids.entity.enm;

public enum InstitutionStatus {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    SUSPENDED("Suspended"),
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String displayName;

    InstitutionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
