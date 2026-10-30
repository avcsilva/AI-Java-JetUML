package br.uefs.umleditor.model.edges;

import br.uefs.umleditor.model.Edge;

/**
 * A dependency between use cases, which may be an include or an extend.
 */
public class UseCaseDependencyEdge extends Edge {

    /**
     * Kind of use case dependency (RF20).
     */
    public enum Type {
        NONE("None", ""),
        INCLUDE("Include", "«include»"),
        EXTEND("Extend", "«extend»");

        private final String displayName;
        private final String stereotype;

        Type(String displayName, String stereotype) {
            this.displayName = displayName;
            this.stereotype = stereotype;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getStereotype() {
            return stereotype;
        }
    }

    private Type type;

    public UseCaseDependencyEdge(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
