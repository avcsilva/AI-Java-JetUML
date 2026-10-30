package br.uefs.umleditor.model.edges;

import java.util.List;

import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Property;

/**
 * A generalization of a class diagram: inheritance or realization of an
 * interface (implementation).
 */
public class GeneralizationEdge extends Edge {

    /**
     * Kind of generalization (RF20).
     */
    public enum Type {
        INHERITANCE("Inheritance"),
        IMPLEMENTATION("Implementation");

        private final String displayName;

        Type(String displayName) {
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

    private Type type;

    public GeneralizationEdge(Type type) {
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
        return List.of(Property.choice("Generalization Type", List.of(Type.values()), this::getType, this::setType));
    }
}
