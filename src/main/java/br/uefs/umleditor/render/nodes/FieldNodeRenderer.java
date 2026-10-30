package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.ObjectNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.TextStyle;

/**
 * Draws a field as a "name = value" row of its object.
 */
public class FieldNodeRenderer extends NodeRenderer<FieldNode> {

    private final ObjectNodeRenderer objectRenderer;

    public FieldNodeRenderer(DiagramRenderer diagramRenderer, ObjectNodeRenderer objectRenderer) {
        super(diagramRenderer);
        this.objectRenderer = objectRenderer;
    }

    @Override
    public Rectangle getBounds(FieldNode node) {
        if (node.getParent() instanceof ObjectNode object) {
            return objectRenderer.getFieldBounds(object, node);
        }
        return new Rectangle(node.getX(), node.getY(), measure(ObjectNodeRenderer.fieldText(node),
                TextStyle.PLAIN).width() + 2 * PADDING, context().lineHeight(TextStyle.PLAIN));
    }

    /**
     * @return the value part of the field row, where references start (RF26)
     */
    public Rectangle getValueBounds(FieldNode node) {
        Rectangle row = getBounds(node);
        double nameWidth = measure(node.getName() + " =", TextStyle.PLAIN).width();
        double x = Math.min(row.maxX(), row.x() + PADDING + nameWidth);
        return new Rectangle(x, row.y(), row.maxX() - x, row.height());
    }

    @Override
    public void draw(FieldNode node, GraphicsAdapter g) {
        drawText(g, ObjectNodeRenderer.fieldText(node), TextStyle.PLAIN, getBounds(node), false, true);
    }
}
