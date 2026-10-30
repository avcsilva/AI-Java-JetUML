package br.uefs.umleditor.render;

import java.util.Locale;

import br.uefs.umleditor.geom.Rectangle;
import javafx.scene.paint.Color;

/**
 * Draws into an SVG document, so that exported images keep the same
 * appearance as the screen (RF04).
 */
public class SvgGraphicsAdapter implements GraphicsAdapter {

    private final RenderContext context;
    private final StringBuilder body = new StringBuilder();

    public SvgGraphicsAdapter(RenderContext context) {
        this.context = context;
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2, Color color, LineStyle style) {
        body.append("  <line x1=\"").append(n(x1)).append("\" y1=\"").append(n(y1)).append("\" x2=\"").append(n(x2))
                .append("\" y2=\"").append(n(y2)).append('"').append(stroke(color)).append(dashes(style))
                .append("/>\n");
    }

    @Override
    public void drawRectangle(Rectangle r, Color fill, Color stroke) {
        drawRoundedRectangle(r, 0, fill, stroke);
    }

    @Override
    public void drawRoundedRectangle(Rectangle r, double arc, Color fill, Color stroke) {
        body.append("  <rect x=\"").append(n(r.x())).append("\" y=\"").append(n(r.y())).append("\" width=\"")
                .append(n(r.width())).append("\" height=\"").append(n(r.height())).append('"');
        if (arc > 0) {
            body.append(" rx=\"").append(n(arc / 2)).append("\" ry=\"").append(n(arc / 2)).append('"');
        }
        body.append(fill(fill)).append(stroke(stroke)).append("/>\n");
    }

    @Override
    public void drawOval(Rectangle r, Color fill, Color stroke) {
        body.append("  <ellipse cx=\"").append(n(r.center().x())).append("\" cy=\"").append(n(r.center().y()))
                .append("\" rx=\"").append(n(r.width() / 2)).append("\" ry=\"").append(n(r.height() / 2)).append('"')
                .append(fill(fill)).append(stroke(stroke)).append("/>\n");
    }

    @Override
    public void drawPolygon(double[] xs, double[] ys, Color fill, Color stroke) {
        body.append("  <polygon points=\"");
        for (int i = 0; i < xs.length; i++) {
            if (i > 0) {
                body.append(' ');
            }
            body.append(n(xs[i])).append(',').append(n(ys[i]));
        }
        body.append('"').append(fill(fill)).append(stroke(stroke)).append("/>\n");
    }

    @Override
    public void drawText(String text, double x, double y, TextStyle style, Color color) {
        if (text.isEmpty()) {
            return;
        }
        FontSettings font = context.getFontSettings();
        body.append("  <text x=\"").append(n(x)).append("\" y=\"").append(n(y + context.baselineOffset(style)))
                .append("\" font-family=\"").append(escape(font.family())).append(", sans-serif\" font-size=\"")
                .append(n(font.size())).append('"');
        if (style.bold()) {
            body.append(" font-weight=\"bold\"");
        }
        if (style.italic()) {
            body.append(" font-style=\"italic\"");
        }
        if (style.underline()) {
            body.append(" text-decoration=\"underline\"");
        }
        body.append(" fill=\"").append(rgb(color)).append("\" xml:space=\"preserve\">").append(escape(text))
                .append("</text>\n");
    }

    /**
     * @return the SVG document showing the given area of the drawing on a background color
     */
    public String toSvg(Rectangle area, Color background) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + n(area.width()) + "\" height=\""
                + n(area.height()) + "\" viewBox=\"" + n(area.x()) + " " + n(area.y()) + " " + n(area.width()) + " "
                + n(area.height()) + "\">\n"
                + "  <rect x=\"" + n(area.x()) + "\" y=\"" + n(area.y()) + "\" width=\"" + n(area.width())
                + "\" height=\"" + n(area.height()) + "\" fill=\"" + rgb(background) + "\"/>\n"
                + body
                + "</svg>\n";
    }

    private static String fill(Color color) {
        return " fill=\"" + (color == null ? "none" : rgb(color)) + "\"";
    }

    private static String stroke(Color color) {
        return color == null ? " stroke=\"none\"" : " stroke=\"" + rgb(color) + "\" stroke-width=\"1\"";
    }

    private static String dashes(LineStyle style) {
        double[] dashes = style.getDashes();
        if (dashes.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder(" stroke-dasharray=\"");
        for (int i = 0; i < dashes.length; i++) {
            result.append(i > 0 ? "," : "").append(n(dashes[i]));
        }
        return result.append('"').toString();
    }

    private static String rgb(Color color) {
        return String.format(Locale.ROOT, "rgb(%d,%d,%d)", Math.round(color.getRed() * 255),
                Math.round(color.getGreen() * 255), Math.round(color.getBlue() * 255));
    }

    private static String n(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        return text.contains(".") ? text.replaceAll("0+$", "").replaceAll("\\.$", "") : text;
    }

    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
