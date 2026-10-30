package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.ObjectNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.LineStyle;
import br.uefs.umleditor.render.TextStyle;

/**
 * Draws an object as a box with its underlined name, followed by one row per
 * field (RF25). The fields are laid out by this renderer.
 */
public class ObjectNodeRenderer extends NodeRenderer<ObjectNode> {

    private static final double MIN_WIDTH = 80;
    private static final double MIN_NAME_HEIGHT = 30;
    private static final double ROW_SPACING = 4;

    public ObjectNodeRenderer(DiagramRenderer diagramRenderer) {
        super(diagramRenderer);
    }

    static String fieldText(FieldNode field) {
        return field.getName() + " = " + field.getValue();
    }

    private double nameHeight(ObjectNode node) {
        return Math.max(MIN_NAME_HEIGHT, measure(node.getName(), TextStyle.UNDERLINED).height() + 2 * PADDING);
    }

    private double rowHeight(FieldNode field) {
        return Math.max(context().lineHeight(TextStyle.PLAIN), measure(fieldText(field), TextStyle.PLAIN).height())
                + ROW_SPACING;
    }

    private double width(ObjectNode node) {
        double width = measure(node.getName(), TextStyle.UNDERLINED).width();
        for (Node child : node.getChildren()) {
            width = Math.max(width, measure(fieldText((FieldNode) child), TextStyle.PLAIN).width());
        }
        return Math.max(MIN_WIDTH, width + 2 * PADDING);
    }

    @Override
    public Rectangle getBounds(ObjectNode node) {
        double height = nameHeight(node);
        if (!node.getChildren().isEmpty()) {
            height += PADDING;
            for (Node child : node.getChildren()) {
                height += rowHeight((FieldNode) child);
            }
        }
        return new Rectangle(node.getX(), node.getY(), width(node), height);
    }

    /**
     * @return the row of the object occupied by the field
     */
    public Rectangle getFieldBounds(ObjectNode node, FieldNode field) {
        double y = node.getY() + nameHeight(node) + PADDING / 2;
        for (Node child : node.getChildren()) {
            double height = rowHeight((FieldNode) child);
            if (child == field) {
                return new Rectangle(node.getX(), y, width(node), height);
            }
            y += height;
        }
        throw new IllegalArgumentException("Not a field of this object");
    }

    @Override
    public void draw(ObjectNode node, GraphicsAdapter g) {
        Rectangle b = getBounds(node);
        g.drawRectangle(b, theme().getFill(), theme().getStroke());
        double nameHeight = nameHeight(node);
        drawText(g, node.getName(), TextStyle.UNDERLINED, new Rectangle(b.x(), b.y(), b.width(), nameHeight),
                true, true);
        if (!node.getChildren().isEmpty()) {
            g.drawLine(b.x(), b.y() + nameHeight, b.maxX(), b.y() + nameHeight, theme().getStroke(),
                    LineStyle.SOLID);
        }
    }
}
