package br.uefs.umleditor.geom;

/**
 * An immutable width and height.
 */
public record Dimension(double width, double height) {

    public static final Dimension ZERO = new Dimension(0, 0);
}
