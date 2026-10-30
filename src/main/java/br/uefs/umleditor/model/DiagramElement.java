package br.uefs.umleditor.model;

import java.util.List;

/**
 * An element of a diagram that the user can select: a node or an edge.
 */
public interface DiagramElement {

    /**
     * @return the properties edited in the properties panel (RF22), in the
     *         order they are shown; empty if the element has none
     */
    default List<Property> getProperties() {
        return List.of();
    }
}
