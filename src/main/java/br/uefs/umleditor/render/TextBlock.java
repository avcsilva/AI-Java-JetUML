package br.uefs.umleditor.render;

import java.util.List;

import br.uefs.umleditor.geom.Dimension;
import br.uefs.umleditor.geom.Rectangle;

/**
 * Measures and draws multi-line text, applying the line formatting of RF24.
 */
public final class TextBlock {

    private TextBlock() {
    }

    public static Dimension measure(String text, TextStyle style, RenderContext context) {
        double width = 0;
        double height = 0;
        for (String line : StyledText.lines(text)) {
            StyledText.StyledLine styled = StyledText.parse(line);
            Dimension size = context.measure(styled.text(), style.with(styled));
            width = Math.max(width, size.width());
            height += size.height();
        }
        return new Dimension(width, height);
    }

    /**
     * Draws the text inside the box: centered horizontally or starting at the
     * left padding, and centered vertically or starting at the top padding.
     */
    public static void draw(GraphicsAdapter g, String text, TextStyle style, Rectangle box, double padding,
                            boolean centerHorizontally, boolean centerVertically, RenderContext context) {
        List<String> lines = StyledText.lines(text);
        double totalHeight = measure(text, style, context).height();
        double y = centerVertically ? box.y() + (box.height() - totalHeight) / 2 : box.y() + padding;
        for (String line : lines) {
            StyledText.StyledLine styled = StyledText.parse(line);
            TextStyle lineStyle = style.with(styled);
            Dimension size = context.measure(styled.text(), lineStyle);
            double x = centerHorizontally ? box.x() + (box.width() - size.width()) / 2 : box.x() + padding;
            g.drawText(styled.text(), x, y, lineStyle, context.getTheme().getText());
            y += size.height();
        }
    }
}
