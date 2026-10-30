package br.uefs.umleditor.render;

import br.uefs.umleditor.geom.Rectangle;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Draws on a JavaFX canvas.
 */
public class FxGraphicsAdapter implements GraphicsAdapter {

    private static final double LINE_WIDTH = 1;

    private final GraphicsContext gc;
    private final RenderContext context;

    public FxGraphicsAdapter(GraphicsContext gc, RenderContext context) {
        this.gc = gc;
        this.context = context;
        gc.setLineWidth(LINE_WIDTH);
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2, Color color, LineStyle style) {
        gc.setStroke(color);
        gc.setLineDashes(style.getDashes());
        gc.strokeLine(x1, y1, x2, y2);
        gc.setLineDashes();
    }

    @Override
    public void drawRectangle(Rectangle r, Color fill, Color stroke) {
        if (fill != null) {
            gc.setFill(fill);
            gc.fillRect(r.x(), r.y(), r.width(), r.height());
        }
        if (stroke != null) {
            gc.setStroke(stroke);
            gc.strokeRect(r.x(), r.y(), r.width(), r.height());
        }
    }

    @Override
    public void drawRoundedRectangle(Rectangle r, double arc, Color fill, Color stroke) {
        if (fill != null) {
            gc.setFill(fill);
            gc.fillRoundRect(r.x(), r.y(), r.width(), r.height(), arc, arc);
        }
        if (stroke != null) {
            gc.setStroke(stroke);
            gc.strokeRoundRect(r.x(), r.y(), r.width(), r.height(), arc, arc);
        }
    }

    @Override
    public void drawOval(Rectangle r, Color fill, Color stroke) {
        if (fill != null) {
            gc.setFill(fill);
            gc.fillOval(r.x(), r.y(), r.width(), r.height());
        }
        if (stroke != null) {
            gc.setStroke(stroke);
            gc.strokeOval(r.x(), r.y(), r.width(), r.height());
        }
    }

    @Override
    public void drawPolygon(double[] xs, double[] ys, Color fill, Color stroke) {
        if (fill != null) {
            gc.setFill(fill);
            gc.fillPolygon(xs, ys, xs.length);
        }
        if (stroke != null) {
            gc.setStroke(stroke);
            gc.strokePolygon(xs, ys, xs.length);
        }
    }

    @Override
    public void drawText(String text, double x, double y, TextStyle style, Color color) {
        gc.setFont(context.getFontSettings().font(style));
        gc.setFill(color);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(text, x, y);
        if (style.underline() && !text.isEmpty()) {
            double width = context.measure(text, style).width();
            double underlineY = y + context.lineHeight(style) - 2;
            gc.setStroke(color);
            gc.strokeLine(x, underlineY, x + width, underlineY);
        }
    }
}
