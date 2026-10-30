package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Dimension;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.RenderContext;
import br.uefs.umleditor.render.TextBlock;
import br.uefs.umleditor.render.TextStyle;
import br.uefs.umleditor.render.Theme;

/**
 * Computes the bounds of one type of node and draws it.
 */
public abstract class NodeRenderer<N extends Node> {

    protected static final double PADDING = 6;

    private final DiagramRenderer diagramRenderer;

    protected NodeRenderer(DiagramRenderer diagramRenderer) {
        this.diagramRenderer = diagramRenderer;
    }

    public abstract Rectangle getBounds(N node);

    public abstract void draw(N node, GraphicsAdapter g);

    protected DiagramRenderer diagramRenderer() {
        return diagramRenderer;
    }

    protected RenderContext context() {
        return diagramRenderer.getContext();
    }

    protected Theme theme() {
        return context().getTheme();
    }

    protected Dimension measure(String text, TextStyle style) {
        return TextBlock.measure(text, style, context());
    }

    protected void drawText(GraphicsAdapter g, String text, TextStyle style, Rectangle box,
                            boolean centerHorizontally, boolean centerVertically) {
        TextBlock.draw(g, text, style, box, PADDING, centerHorizontally, centerVertically, context());
    }
}
