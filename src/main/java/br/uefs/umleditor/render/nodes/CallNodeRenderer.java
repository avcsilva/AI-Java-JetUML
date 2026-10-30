package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.layout.SequenceLayout;
import br.uefs.umleditor.model.nodes.CallNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.LineStyle;

/**
 * Draws an activation box. With Open Bottom, the bottom side is not drawn,
 * showing that the sequence of calls is incomplete (RF31).
 */
public class CallNodeRenderer extends NodeRenderer<CallNode> {

    public CallNodeRenderer(DiagramRenderer diagramRenderer) {
        super(diagramRenderer);
    }

    @Override
    public Rectangle getBounds(CallNode node) {
        return new Rectangle(node.getX(), node.getY(), SequenceLayout.CALL_WIDTH, node.getHeight());
    }

    @Override
    public void draw(CallNode node, GraphicsAdapter g) {
        Rectangle b = getBounds(node);
        if (!node.isOpenBottom()) {
            g.drawRectangle(b, theme().getFill(), theme().getStroke());
            return;
        }
        g.drawRectangle(b, theme().getFill(), null);
        g.drawLine(b.x(), b.y(), b.maxX(), b.y(), theme().getStroke(), LineStyle.SOLID);
        g.drawLine(b.x(), b.y(), b.x(), b.maxY(), theme().getStroke(), LineStyle.SOLID);
        g.drawLine(b.maxX(), b.y(), b.maxX(), b.maxY(), theme().getStroke(), LineStyle.SOLID);
    }
}
