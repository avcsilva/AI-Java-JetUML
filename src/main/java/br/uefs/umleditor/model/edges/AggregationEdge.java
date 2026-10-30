package br.uefs.umleditor.model.edges;

/**
 * A whole-part relationship, from the whole (start) to the part (end):
 * aggregation or composition.
 */
public class AggregationEdge extends ClassRelationshipEdge {

    /**
     * Kind of whole-part relationship (RF20).
     */
    public enum Type {
        AGGREGATION("Aggregation"),
        COMPOSITION("Composition");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Type type;

    public AggregationEdge(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
