package br.uefs.umleditor.layout;

import br.uefs.umleditor.geom.Point;

/**
 * Aligns positions to the grid. Elements snap to the grid whether or not
 * it is visible (RF36).
 */
public final class GridSnap {

    public static final int GRID_SIZE = 10;

    private GridSnap() {
    }

    public static double snap(double value) {
        return Math.round(value / GRID_SIZE) * (double) GRID_SIZE;
    }

    public static Point snap(Point point) {
        return new Point(snap(point.x()), snap(point.y()));
    }

    /**
     * @return the largest grid position that is not greater than the value
     */
    public static double snapDown(double value) {
        return Math.floor(value / GRID_SIZE) * GRID_SIZE;
    }
}
