package br.uefs.umleditor.render.nodes;

import java.util.ArrayList;
import java.util.List;

import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.NamedNode;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.InterfaceNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.LineStyle;
import br.uefs.umleditor.render.TextStyle;

/**
 * Draws classes (name, attributes and methods) and interfaces (stereotype,
 * name and methods) as boxes with compartments.
 */
public class ClassNodeRenderer extends NodeRenderer<NamedNode> {

    private static final double MIN_WIDTH = 100;
    private static final double MIN_NAME_HEIGHT = 30;
    private static final double MIN_COMPARTMENT_HEIGHT = 20;
    private static final String INTERFACE_STEREOTYPE = "«interface»";

    public ClassNodeRenderer(DiagramRenderer diagramRenderer) {
        super(diagramRenderer);
    }

    private static String nameText(NamedNode node) {
        if (node instanceof InterfaceNode) {
            return node.getName().isEmpty() ? INTERFACE_STEREOTYPE : INTERFACE_STEREOTYPE + "\n" + node.getName();
        }
        return node.getName();
    }

    private static List<String> compartments(NamedNode node) {
        List<String> result = new ArrayList<>();
        if (node instanceof ClassNode classNode) {
            result.add(classNode.getAttributes());
            result.add(classNode.getMethods());
        } else if (node instanceof InterfaceNode interfaceNode) {
            result.add(interfaceNode.getMethods());
        }
        return result;
    }

    private double nameHeight(NamedNode node) {
        return Math.max(MIN_NAME_HEIGHT, measure(nameText(node), TextStyle.BOLD).height() + 2 * PADDING);
    }

    private double compartmentHeight(String text) {
        return Math.max(MIN_COMPARTMENT_HEIGHT, measure(text, TextStyle.PLAIN).height() + 2 * PADDING);
    }

    @Override
    public Rectangle getBounds(NamedNode node) {
        double width = measure(nameText(node), TextStyle.BOLD).width();
        double height = nameHeight(node);
        for (String compartment : compartments(node)) {
            width = Math.max(width, measure(compartment, TextStyle.PLAIN).width());
            height += compartmentHeight(compartment);
        }
        return new Rectangle(node.getX(), node.getY(), Math.max(MIN_WIDTH, width + 2 * PADDING), height);
    }

    @Override
    public void draw(NamedNode node, GraphicsAdapter g) {
        Rectangle bounds = getBounds(node);
        g.drawRectangle(bounds, theme().getFill(), theme().getStroke());
        double y = bounds.y();
        double nameHeight = nameHeight(node);
        drawText(g, nameText(node), TextStyle.BOLD, new Rectangle(bounds.x(), y, bounds.width(), nameHeight),
                true, true);
        y += nameHeight;
        for (String compartment : compartments(node)) {
            double height = compartmentHeight(compartment);
            g.drawLine(bounds.x(), y, bounds.maxX(), y, theme().getStroke(), LineStyle.SOLID);
            drawText(g, compartment, TextStyle.PLAIN, new Rectangle(bounds.x(), y, bounds.width(), height),
                    false, false);
            y += height;
        }
    }
}
