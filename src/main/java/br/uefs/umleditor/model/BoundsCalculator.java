package br.uefs.umleditor.model;

import br.uefs.umleditor.geom.Rectangle;

/**
 * Computes the bounds of nodes, which depend on how they are drawn.
 */
@FunctionalInterface
public interface BoundsCalculator {

    Rectangle getBounds(Node node);
}
