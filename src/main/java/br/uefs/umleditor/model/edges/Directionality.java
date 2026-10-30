package br.uefs.umleditor.model.edges;

/**
 * Navigability of an association, aggregation or dependency (RF20).
 */
public enum Directionality {
    UNSPECIFIED("Unspecified"),
    UNIDIRECTIONAL("Unidirectional"),
    BIDIRECTIONAL("Bidirectional");

    private final String displayName;

    Directionality(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
