package br.uefs.umleditor.render;

import br.uefs.umleditor.geom.Rectangle;
import javafx.scene.paint.Color;

/**
 * Drawing primitives used by the renderers. Keeping drawing behind this
 * interface lets the same renderers paint on screen and in exported images.
 * A null fill or stroke means that part is not drawn.
 */
public interface GraphicsAdapter {

    void drawLine(double x1, double y1, double x2, double y2, Color color, LineStyle style);

    void drawRectangle(Rectangle rectangle, Color fill, Color stroke);

    void drawRoundedRectangle(Rectangle rectangle, double arc, Color fill, Color stroke);

    void drawOval(Rectangle rectangle, Color fill, Color stroke);

    void drawPolygon(double[] xs, double[] ys, Color fill, Color stroke);

    /**
     * Draws a single line of text whose top-left corner is at (x, y).
     */
    void drawText(String text, double x, double y, TextStyle style, Color color);
}
