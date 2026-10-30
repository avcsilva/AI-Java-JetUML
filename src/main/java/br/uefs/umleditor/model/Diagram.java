package br.uefs.umleditor.model;

import java.util.Objects;

/**
 * A UML diagram of a given type with a fixed size in pixels (RF38).
 */
public class Diagram {

    public static final int MIN_SIZE = 250;
    public static final int MAX_SIZE = 4000;

    private final DiagramType type;
    private int width;
    private int height;

    public Diagram(DiagramType type, int width, int height) {
        this.type = Objects.requireNonNull(type);
        setSize(width, height);
    }

    public DiagramType getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public final void setSize(int width, int height) {
        this.width = clampSize(width);
        this.height = clampSize(height);
    }

    /**
     * Limits a diagram dimension to the allowed range of 250 to 4000 pixels (RF38).
     */
    public static int clampSize(int value) {
        return Math.max(MIN_SIZE, Math.min(MAX_SIZE, value));
    }
}
