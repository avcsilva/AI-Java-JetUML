package br.uefs.umleditor.model.edges;

import java.util.List;

import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Property;

/**
 * A dependency of a class diagram, unidirectional or bidirectional.
 */
public class DependencyEdge extends Edge {

    private Directionality directionality = Directionality.UNIDIRECTIONAL;

    public Directionality getDirectionality() {
        return directionality;
    }

    public void setDirectionality(Directionality directionality) {
        this.directionality = directionality;
    }

    @Override
    public List<Property> getProperties() {
        return List.of(Property.choice("Directionality",
                List.of(Directionality.UNIDIRECTIONAL, Directionality.BIDIRECTIONAL), this::getDirectionality,
                this::setDirectionality));
    }
}
