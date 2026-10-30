package br.uefs.umleditor.render;

/**
 * Stroke pattern of a line.
 */
public enum LineStyle {
    SOLID(new double[0]),
    DASHED(new double[] {5, 4});

    private final double[] dashes;

    LineStyle(double[] dashes) {
        this.dashes = dashes;
    }

    public double[] getDashes() {
        return dashes.clone();
    }
}
