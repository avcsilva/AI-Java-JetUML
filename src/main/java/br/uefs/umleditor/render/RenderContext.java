package br.uefs.umleditor.render;

import br.uefs.umleditor.geom.Dimension;
import javafx.scene.text.Text;

/**
 * Appearance settings shared by the renderers of a diagram: colors (RF40)
 * and font (RF39). It also measures text with the current font.
 */
public class RenderContext {

    private Theme theme;
    private FontSettings fontSettings;
    private final Text measuringText = new Text();

    public RenderContext(Theme theme, FontSettings fontSettings) {
        this.theme = theme;
        this.fontSettings = fontSettings;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public FontSettings getFontSettings() {
        return fontSettings;
    }

    public void setFontSettings(FontSettings fontSettings) {
        this.fontSettings = fontSettings;
    }

    /**
     * @return the size of a single line of text
     */
    public Dimension measure(String text, TextStyle style) {
        measuringText.setFont(fontSettings.font(style));
        measuringText.setText(text.isEmpty() ? " " : text);
        double width = text.isEmpty() ? 0 : measuringText.getLayoutBounds().getWidth();
        return new Dimension(width, measuringText.getLayoutBounds().getHeight());
    }

    public double lineHeight(TextStyle style) {
        return measure("Ag", style).height();
    }
}
