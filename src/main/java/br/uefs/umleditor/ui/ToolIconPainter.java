package br.uefs.umleditor.ui;

import br.uefs.umleditor.model.ToolType;
import br.uefs.umleditor.render.Theme;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Draws the icon of each tool, so that no image resources are needed.
 */
public final class ToolIconPainter {

    public static final double SIZE = 24;

    private static final double LOW = 4;
    private static final double HIGH = 20;
    private static final double[] DASHES = {3, 2};

    private enum Head { NONE, OPEN, TRIANGLE, FILLED, DIAMOND, FILLED_DIAMOND }

    private ToolIconPainter() {
    }

    public static Canvas createIcon(ToolType tool, Theme theme) {
        Canvas canvas = new Canvas(SIZE, SIZE);
        paint(canvas, tool, theme);
        return canvas;
    }

    public static void paint(Canvas canvas, ToolType tool, Theme theme) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, SIZE, SIZE);
        gc.setStroke(theme.getStroke());
        gc.setFill(theme.getFill());
        gc.setLineWidth(1.2);
        gc.setLineDashes();
        switch (tool) {
            case SELECT -> paintSelect(gc, theme);
            case CLASS -> paintCompartments(gc, 2);
            case INTERFACE -> paintInterface(gc, theme);
            case PACKAGE -> paintPackage(gc);
            case PACKAGE_DESCRIPTION -> paintPackageDescription(gc);
            case NOTE -> paintNote(gc);
            case DEPENDENCY -> paintEdge(gc, true, Head.NONE, Head.OPEN, theme);
            case INHERITANCE, GENERALIZATION -> paintEdge(gc, false, Head.NONE, Head.TRIANGLE, theme);
            case REALIZATION -> paintEdge(gc, true, Head.NONE, Head.TRIANGLE, theme);
            case CLASS_ASSOCIATION, USE_CASE_ASSOCIATION, COLLABORATION ->
                    paintEdge(gc, false, Head.NONE, Head.NONE, theme);
            case AGGREGATION -> paintEdge(gc, false, Head.DIAMOND, Head.NONE, theme);
            case COMPOSITION -> paintEdge(gc, false, Head.FILLED_DIAMOND, Head.NONE, theme);
            case NOTE_CONNECTOR -> paintEdge(gc, true, Head.NONE, Head.NONE, theme);
            case TO_CLIPBOARD -> paintClipboard(gc, theme);
            case IMPLICIT_PARAMETER -> paintImplicitParameter(gc);
            case CALL -> paintHorizontalEdge(gc, false, theme);
            case RETURN -> paintHorizontalEdge(gc, true, theme);
            case STATE -> paintState(gc);
            case INITIAL_STATE -> paintInitialState(gc, theme);
            case FINAL_STATE -> paintFinalState(gc, theme);
            case TRANSITION -> paintEdge(gc, false, Head.NONE, Head.OPEN, theme);
            case OBJECT -> paintObject(gc);
            case FIELD -> paintField(gc, theme);
            case REFERENCE -> paintReference(gc, theme);
            case ACTOR -> paintActor(gc);
            case USE_CASE -> paintUseCase(gc);
            case INCLUDE -> paintStereotypedEdge(gc, "i", theme);
            case EXTEND -> paintStereotypedEdge(gc, "e", theme);
        }
    }

    private static void paintSelect(GraphicsContext gc, Theme theme) {
        double[] xs = {7, 7, 11, 14, 16, 13, 18};
        double[] ys = {3, 19, 15, 21, 20, 14, 14};
        gc.setFill(theme.getStroke());
        gc.fillPolygon(xs, ys, xs.length);
    }

    private static void paintCompartments(GraphicsContext gc, int separators) {
        gc.fillRect(3, 4, 18, 16);
        gc.strokeRect(3, 4, 18, 16);
        double step = 16.0 / (separators + 1);
        for (int i = 1; i <= separators; i++) {
            double y = 4 + Math.min(step * i, 16);
            gc.strokeLine(3, y, 21, y);
        }
    }

    private static void paintInterface(GraphicsContext gc, Theme theme) {
        gc.fillRect(3, 4, 18, 16);
        gc.strokeRect(3, 4, 18, 16);
        gc.strokeLine(3, 13, 21, 13);
        paintLabel(gc, "«I»", 12, 8.5, 7, theme);
    }

    private static void paintPackage(GraphicsContext gc) {
        gc.fillRect(3, 4, 8, 4);
        gc.strokeRect(3, 4, 8, 4);
        gc.fillRect(3, 8, 18, 12);
        gc.strokeRect(3, 8, 18, 12);
    }

    private static void paintPackageDescription(GraphicsContext gc) {
        paintPackage(gc);
        gc.strokeLine(6, 12, 18, 12);
        gc.strokeLine(6, 15, 18, 15);
        gc.strokeLine(6, 18, 14, 18);
    }

    private static void paintNote(GraphicsContext gc) {
        double[] xs = {4, 15, 20, 20, 4};
        double[] ys = {3, 3, 8, 21, 21};
        gc.fillPolygon(xs, ys, xs.length);
        gc.strokePolygon(xs, ys, xs.length);
        gc.strokeLine(15, 3, 15, 8);
        gc.strokeLine(15, 8, 20, 8);
    }

    private static void paintClipboard(GraphicsContext gc, Theme theme) {
        gc.fillRect(5, 4, 14, 18);
        gc.strokeRect(5, 4, 14, 18);
        gc.setFill(theme.getStroke());
        gc.fillRect(9, 2, 6, 4);
        gc.strokeLine(8, 10, 16, 10);
        gc.strokeLine(8, 14, 16, 14);
        gc.strokeLine(8, 18, 13, 18);
    }

    private static void paintImplicitParameter(GraphicsContext gc) {
        gc.fillRect(4, 2, 16, 7);
        gc.strokeRect(4, 2, 16, 7);
        gc.setLineDashes(DASHES);
        gc.strokeLine(12, 9, 12, 22);
        gc.setLineDashes();
    }

    private static void paintHorizontalEdge(GraphicsContext gc, boolean isReturn, Theme theme) {
        if (isReturn) {
            gc.setLineDashes(DASHES);
            gc.strokeLine(HIGH, 12, LOW, 12);
            gc.setLineDashes();
            paintHead(gc, HIGH, 12, LOW, 12, Head.OPEN, theme);
        } else {
            gc.strokeLine(LOW, 12, HIGH, 12);
            paintHead(gc, LOW, 12, HIGH, 12, Head.FILLED, theme);
        }
    }

    private static void paintState(GraphicsContext gc) {
        gc.fillRoundRect(3, 6, 18, 12, 8, 8);
        gc.strokeRoundRect(3, 6, 18, 12, 8, 8);
    }

    private static void paintInitialState(GraphicsContext gc, Theme theme) {
        gc.setFill(theme.getStroke());
        gc.fillOval(6, 6, 12, 12);
    }

    private static void paintFinalState(GraphicsContext gc, Theme theme) {
        gc.strokeOval(4, 4, 16, 16);
        gc.setFill(theme.getStroke());
        gc.fillOval(8, 8, 8, 8);
    }

    private static void paintObject(GraphicsContext gc) {
        gc.fillRect(3, 4, 18, 16);
        gc.strokeRect(3, 4, 18, 16);
        gc.strokeLine(3, 11, 21, 11);
        gc.strokeLine(8, 8.5, 16, 8.5);
        gc.strokeLine(6, 15.5, 18, 15.5);
    }

    private static void paintField(GraphicsContext gc, Theme theme) {
        gc.fillRect(2, 7, 20, 10);
        gc.strokeRect(2, 7, 20, 10);
        gc.strokeLine(12, 7, 12, 17);
        paintLabel(gc, "=", 12, 12, 9, theme);
    }

    private static void paintReference(GraphicsContext gc, Theme theme) {
        gc.setFill(theme.getStroke());
        gc.fillOval(LOW - 2, HIGH - 2, 4, 4);
        gc.strokeLine(LOW, HIGH, HIGH, LOW);
        paintHead(gc, LOW, HIGH, HIGH, LOW, Head.FILLED, theme);
    }

    private static void paintActor(GraphicsContext gc) {
        gc.strokeOval(9, 2, 6, 6);
        gc.strokeLine(12, 8, 12, 15);
        gc.strokeLine(6, 11, 18, 11);
        gc.strokeLine(12, 15, 7, 22);
        gc.strokeLine(12, 15, 17, 22);
    }

    private static void paintUseCase(GraphicsContext gc) {
        gc.fillOval(2, 6, 20, 12);
        gc.strokeOval(2, 6, 20, 12);
    }

    private static void paintStereotypedEdge(GraphicsContext gc, String letter, Theme theme) {
        paintEdge(gc, true, Head.NONE, Head.OPEN, theme);
        paintLabel(gc, letter, 7, 8, 9, theme);
    }

    private static void paintEdge(GraphicsContext gc, boolean dashed, Head startHead, Head endHead, Theme theme) {
        if (dashed) {
            gc.setLineDashes(DASHES);
        }
        gc.strokeLine(LOW, HIGH, HIGH, LOW);
        gc.setLineDashes();
        paintHead(gc, HIGH, LOW, LOW, HIGH, startHead, theme);
        paintHead(gc, LOW, HIGH, HIGH, LOW, endHead, theme);
    }

    /**
     * Paints an arrow head at (toX, toY) for a line coming from (fromX, fromY).
     */
    private static void paintHead(GraphicsContext gc, double fromX, double fromY, double toX, double toY,
                                  Head head, Theme theme) {
        if (head == Head.NONE) {
            return;
        }
        double angle = Math.atan2(toY - fromY, toX - fromX);
        double length = 7;
        double spread = Math.PI / 7;
        double leftX = toX - length * Math.cos(angle - spread);
        double leftY = toY - length * Math.sin(angle - spread);
        double rightX = toX - length * Math.cos(angle + spread);
        double rightY = toY - length * Math.sin(angle + spread);
        switch (head) {
            case OPEN -> {
                gc.strokeLine(toX, toY, leftX, leftY);
                gc.strokeLine(toX, toY, rightX, rightY);
            }
            case TRIANGLE, FILLED -> {
                double[] xs = {toX, leftX, rightX};
                double[] ys = {toY, leftY, rightY};
                gc.setFill(head == Head.FILLED ? theme.getStroke() : theme.getFill());
                gc.fillPolygon(xs, ys, 3);
                gc.strokePolygon(xs, ys, 3);
            }
            case DIAMOND, FILLED_DIAMOND -> {
                double backX = toX - 2 * length * Math.cos(angle) * Math.cos(spread);
                double backY = toY - 2 * length * Math.sin(angle) * Math.cos(spread);
                double[] xs = {toX, leftX, backX, rightX};
                double[] ys = {toY, leftY, backY, rightY};
                gc.setFill(head == Head.FILLED_DIAMOND ? theme.getStroke() : theme.getFill());
                gc.fillPolygon(xs, ys, 4);
                gc.strokePolygon(xs, ys, 4);
            }
            default -> {
            }
        }
    }

    private static void paintLabel(GraphicsContext gc, String text, double x, double y, double size, Theme theme) {
        gc.setFill(theme.getText());
        gc.setFont(Font.font(size));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, x, y);
    }
}
