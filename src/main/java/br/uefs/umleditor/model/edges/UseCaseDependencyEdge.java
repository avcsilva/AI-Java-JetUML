package br.uefs.umleditor.model.edges;

import java.util.List;

import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Property;

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

        @Override
        public String toString() {
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

    @Override
    public List<Property> getProperties() {
        return List.of(Property.choice("Dependency Type", List.of(Type.values()), this::getType, this::setType));
    }
}
