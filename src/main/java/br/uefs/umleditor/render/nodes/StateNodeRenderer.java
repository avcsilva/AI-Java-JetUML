package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Dimension;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.nodes.FinalStateNode;
import br.uefs.umleditor.model.nodes.InitialStateNode;
import br.uefs.umleditor.model.nodes.StateNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.TextStyle;

/**
 * Draws states as rounded rectangles, the initial state as a filled circle
 * and the final state as a circled dot.
 */
public class StateNodeRenderer extends NodeRenderer<Node> {

    private static final double MIN_WIDTH = 80;
    private static final double MIN_HEIGHT = 40;
    private static final double ARC = 20;
    private static final double CIRCLE = 20;
    private static final double FINAL_DOT = 12;

    public StateNodeRenderer(DiagramRenderer diagramRenderer) {
        super(diagramRenderer);
    }

    @Override
    public Rectangle getBounds(Node node) {
        if (node instanceof StateNode state) {
            Dimension text = measure(state.getName(), TextStyle.PLAIN);
            return new Rectangle(node.getX(), node.getY(), Math.max(MIN_WIDTH, text.width() + 4 * PADDING),
                    Math.max(MIN_HEIGHT, text.height() + 2 * PADDING));
        }
        return new Rectangle(node.getX(), node.getY(), CIRCLE, CIRCLE);
    }

    @Override
    public void draw(Node node, GraphicsAdapter g) {
        Rectangle b = getBounds(node);
        if (node instanceof StateNode state) {
            g.drawRoundedRectangle(b, ARC, theme().getFill(), theme().getStroke());
            drawText(g, state.getName(), TextStyle.PLAIN, b, true, true);
        } else if (node instanceof InitialStateNode) {
            g.drawOval(b, theme().getStroke(), theme().getStroke());
        } else if (node instanceof FinalStateNode) {
            g.drawOval(b, theme().getFill(), theme().getStroke());
            double inset = (CIRCLE - FINAL_DOT) / 2;
            g.drawOval(new Rectangle(b.x() + inset, b.y() + inset, FINAL_DOT, FINAL_DOT), theme().getStroke(), null);
        }
    }
}
