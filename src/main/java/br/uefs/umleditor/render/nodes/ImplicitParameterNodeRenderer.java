package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Dimension;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.nodes.ImplicitParameterNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.LineStyle;
import br.uefs.umleditor.render.TextStyle;

/**
 * Draws an implicit parameter of a sequence diagram: a box with the name and
 * a dashed lifeline below it.
 */
public class ImplicitParameterNodeRenderer extends NodeRenderer<ImplicitParameterNode> {

    private static final double MIN_WIDTH = 80;
    private static final double MIN_BOX_HEIGHT = 30;
    private static final double LIFELINE_LENGTH = 150;

    public ImplicitParameterNodeRenderer(DiagramRenderer diagramRenderer) {
        super(diagramRenderer);
    }

    private double boxHeight(ImplicitParameterNode node) {
        return Math.max(MIN_BOX_HEIGHT, measure(node.getName(), TextStyle.PLAIN).height() + 2 * PADDING);
    }

    @Override
    public Rectangle getBounds(ImplicitParameterNode node) {
        Dimension text = measure(node.getName(), TextStyle.PLAIN);
        return new Rectangle(node.getX(), node.getY(), Math.max(MIN_WIDTH, text.width() + 2 * PADDING),
                boxHeight(node) + LIFELINE_LENGTH);
    }

    @Override
    public void draw(ImplicitParameterNode node, GraphicsAdapter g) {
        Rectangle b = getBounds(node);
        Rectangle box = new Rectangle(b.x(), b.y(), b.width(), boxHeight(node));
        g.drawLine(b.center().x(), box.maxY(), b.center().x(), b.maxY(), theme().getStroke(), LineStyle.DASHED);
        g.drawRectangle(box, theme().getFill(), theme().getStroke());
        drawText(g, node.getName(), TextStyle.PLAIN, box, true, true);
    }
}
