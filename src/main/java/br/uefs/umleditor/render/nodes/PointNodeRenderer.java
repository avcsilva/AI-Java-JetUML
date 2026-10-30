package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.nodes.PointNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;

/**
 * A point node has no size and is not drawn: only its connector is visible.
 */
public class PointNodeRenderer extends NodeRenderer<PointNode> {

    public PointNodeRenderer(DiagramRenderer diagramRenderer) {
        super(diagramRenderer);
    }

    @Override
    public Rectangle getBounds(PointNode node) {
        return new Rectangle(node.getX(), node.getY(), 0, 0);
    }

    @Override
    public void draw(PointNode node, GraphicsAdapter g) {
    }
}
