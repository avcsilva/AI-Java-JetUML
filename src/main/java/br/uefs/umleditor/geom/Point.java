package br.uefs.umleditor.geom;

/**
 * An immutable point in diagram coordinates.
 */
public record Point(double x, double y) {

    public Point translated(double dx, double dy) {
        return new Point(x + dx, y + dy);
    }
}
