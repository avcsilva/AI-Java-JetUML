package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Dimension;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.NamedNode;
import br.uefs.umleditor.model.nodes.ActorNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.LineStyle;
import br.uefs.umleditor.render.TextStyle;

/**
 * Draws actors as stick figures with the name below, and use cases as
 * ellipses with the name inside.
 */
public class UseCaseNodeRenderer extends NodeRenderer<NamedNode> {

    private static final double FIGURE_WIDTH = 30;
    private static final double FIGURE_HEIGHT = 50;
    private static final double HEAD = 12;
    private static final double MIN_ELLIPSE_WIDTH = 110;
    private static final double MIN_ELLIPSE_HEIGHT = 40;

    public UseCaseNodeRenderer(DiagramRenderer diagramRenderer) {
        super(diagramRenderer);
    }

    @Override
    public Rectangle getBounds(NamedNode node) {
        Dimension text = measure(node.getName(), TextStyle.PLAIN);
        if (node instanceof ActorNode) {
            double nameHeight = node.getName().isEmpty() ? 0 : text.height() + PADDING;
            return new Rectangle(node.getX(), node.getY(), Math.max(FIGURE_WIDTH, text.width()),
                    FIGURE_HEIGHT + nameHeight);
        }
        return new Rectangle(node.getX(), node.getY(), Math.max(MIN_ELLIPSE_WIDTH, text.width() * 1.4 + 2 * PADDING),
                Math.max(MIN_ELLIPSE_HEIGHT, text.height() * 1.4 + 2 * PADDING));
    }

    @Override
    public void draw(NamedNode node, GraphicsAdapter g) {
        Rectangle b = getBounds(node);
        if (node instanceof ActorNode) {
            drawFigure(g, b.center().x(), b.y());
            Rectangle nameArea = new Rectangle(b.x(), b.y() + FIGURE_HEIGHT, b.width(), b.height() - FIGURE_HEIGHT);
            drawText(g, node.getName(), TextStyle.PLAIN, nameArea, true, true);
        } else {
            g.drawOval(b, theme().getFill(), theme().getStroke());
            drawText(g, node.getName(), TextStyle.PLAIN, b, true, true);
        }
    }

    private void drawFigure(GraphicsAdapter g, double centerX, double top) {
        var stroke = theme().getStroke();
        double neck = top + HEAD;
        double hip = top + FIGURE_HEIGHT * 0.65;
        double half = FIGURE_WIDTH / 2;
        g.drawOval(new Rectangle(centerX - HEAD / 2, top, HEAD, HEAD), theme().getFill(), stroke);
        g.drawLine(centerX, neck, centerX, hip, stroke, LineStyle.SOLID);
        g.drawLine(centerX - half, neck + 8, centerX + half, neck + 8, stroke, LineStyle.SOLID);
        g.drawLine(centerX, hip, centerX - half, top + FIGURE_HEIGHT, stroke, LineStyle.SOLID);
        g.drawLine(centerX, hip, centerX + half, top + FIGURE_HEIGHT, stroke, LineStyle.SOLID);
    }
}
