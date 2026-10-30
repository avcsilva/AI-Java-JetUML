package br.uefs.umleditor.render;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Font family and size used to draw diagram text (RF39).
 */
public record FontSettings(String family, double size) {

    public static final double MIN_SIZE = 8;
    public static final double MAX_SIZE = 36;

    public static FontSettings defaults() {
        Font font = Font.getDefault();
        return new FontSettings(font.getFamily(), Math.round(font.getSize()));
    }

    public Font font(TextStyle style) {
        return Font.font(family, style.bold() ? FontWeight.BOLD : FontWeight.NORMAL,
                style.italic() ? FontPosture.ITALIC : FontPosture.REGULAR, size);
    }
}
