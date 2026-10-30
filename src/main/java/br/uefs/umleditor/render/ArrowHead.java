package br.uefs.umleditor.render;

import br.uefs.umleditor.geom.Point;
import javafx.scene.paint.Color;

/**
 * Decorations drawn at the ends of edges.
 */
public enum ArrowHead {
    NONE,
    OPEN,
    TRIANGLE,
    FILLED_TRIANGLE,
    DIAMOND,
    FILLED_DIAMOND;

    private static final double LENGTH = 10;
    private static final double SPREAD = Math.PI / 7;

    /**
     * Draws the head at the tip, for a line arriving from the given point.
     *
     * @param fill the background color used by hollow heads
     */
    public void draw(GraphicsAdapter g, Point from, Point tip, Color stroke, Color fill) {
        if (this == NONE) {
            return;
        }
        double angle = Math.atan2(tip.y() - from.y(), tip.x() - from.x());
        double leftX = tip.x() - LENGTH * Math.cos(angle - SPREAD);
        double leftY = tip.y() - LENGTH * Math.sin(angle - SPREAD);
        double rightX = tip.x() - LENGTH * Math.cos(angle + SPREAD);
        double rightY = tip.y() - LENGTH * Math.sin(angle + SPREAD);
        switch (this) {
            case OPEN -> {
                g.drawLine(tip.x(), tip.y(), leftX, leftY, stroke, LineStyle.SOLID);
                g.drawLine(tip.x(), tip.y(), rightX, rightY, stroke, LineStyle.SOLID);
            }
            case TRIANGLE, FILLED_TRIANGLE -> g.drawPolygon(new double[] {tip.x(), leftX, rightX},
                    new double[] {tip.y(), leftY, rightY}, this == FILLED_TRIANGLE ? stroke : fill, stroke);
            case DIAMOND, FILLED_DIAMOND -> {
                double backX = tip.x() - 2 * LENGTH * Math.cos(SPREAD) * Math.cos(angle);
                double backY = tip.y() - 2 * LENGTH * Math.cos(SPREAD) * Math.sin(angle);
                g.drawPolygon(new double[] {tip.x(), leftX, backX, rightX},
                        new double[] {tip.y(), leftY, backY, rightY}, this == FILLED_DIAMOND ? stroke : fill, stroke);
            }
            default -> {
            }
        }
    }
}
