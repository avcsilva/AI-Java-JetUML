package br.uefs.umleditor.model;

import br.uefs.umleditor.geom.Rectangle;

/**
 * Computes the bounds of nodes, which depend on how they are drawn.
 */
@FunctionalInterface
public interface BoundsCalculator {

    Rectangle getBounds(Node node);

    /**
     * @return the part of the node where edges that need a specific starting
     *         area begin, such as the value part of a field (RF26)
     */
    default Rectangle getConnectionBounds(Node node) {
        return getBounds(node);
    }
}
