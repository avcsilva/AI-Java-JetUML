package br.uefs.umleditor.render;

import javafx.scene.paint.Color;

/**
 * Colors used to paint diagrams and tool icons in light and dark mode (RF40).
 */
public enum Theme {
    LIGHT(Color.WHITE, Color.rgb(220, 220, 220), Color.BLACK, Color.WHITE, Color.BLACK,
            Color.rgb(255, 249, 196), Color.rgb(30, 120, 220)),
    DARK(Color.rgb(40, 42, 46), Color.rgb(64, 67, 72), Color.rgb(220, 220, 220), Color.rgb(54, 57, 62),
            Color.rgb(220, 220, 220), Color.rgb(92, 88, 52), Color.rgb(90, 160, 240));

    private final Color background;
    private final Color grid;
    private final Color stroke;
    private final Color fill;
    private final Color text;
    private final Color noteFill;
    private final Color selection;

    Theme(Color background, Color grid, Color stroke, Color fill, Color text, Color noteFill, Color selection) {
        this.background = background;
        this.grid = grid;
        this.stroke = stroke;
        this.fill = fill;
        this.text = text;
        this.noteFill = noteFill;
        this.selection = selection;
    }

    public static Theme of(boolean darkMode) {
        return darkMode ? DARK : LIGHT;
    }

    public Color getBackground() {
        return background;
    }

    public Color getGrid() {
        return grid;
    }

    public Color getStroke() {
        return stroke;
    }

    public Color getFill() {
        return fill;
    }

    public Color getText() {
        return text;
    }

    public Color getNoteFill() {
        return noteFill;
    }

    public Color getSelection() {
        return selection;
    }
}
