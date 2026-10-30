package br.uefs.umleditor.model.edges;

import br.uefs.umleditor.model.Edge;

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
}
