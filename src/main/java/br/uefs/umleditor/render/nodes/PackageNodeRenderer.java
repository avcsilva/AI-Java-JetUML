package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Dimension;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.NamedNode;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.nodes.PackageDescriptionNode;
import br.uefs.umleditor.model.nodes.PackageNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.TextStyle;

/**
 * Draws packages and package descriptions: a tab with the name above a body.
 * The body of a package with children grows to contain them (RF15); the body
 * of a package description shows its contents.
 */
public class PackageNodeRenderer extends NodeRenderer<NamedNode> {

    private static final double MIN_TAB_WIDTH = 40;
    private static final double MIN_TAB_HEIGHT = 16;
    private static final double MIN_BODY_WIDTH = 100;
    private static final double MIN_BODY_HEIGHT = 60;
    private static final double CHILD_MARGIN = 10;

    public PackageNodeRenderer(DiagramRenderer diagramRenderer) {
        super(diagramRenderer);
    }

    private Dimension tabSize(NamedNode node) {
        Dimension name = measure(node.getName(), TextStyle.PLAIN);
        return new Dimension(Math.max(MIN_TAB_WIDTH, name.width() + 2 * PADDING),
                Math.max(MIN_TAB_HEIGHT, name.height() + PADDING));
    }

    private static String contents(NamedNode node) {
        return node instanceof PackageDescriptionNode description ? description.getContents() : "";
    }

    @Override
    public Rectangle getBounds(NamedNode node) {
        Dimension tab = tabSize(node);
        if (node instanceof PackageNode packageNode && !packageNode.getChildren().isEmpty()) {
            Rectangle children = null;
            for (Node child : packageNode.getChildren()) {
                Rectangle childBounds = diagramRenderer().getBounds(child);
                children = children == null ? childBounds : children.union(childBounds);
            }
            Rectangle body = children.expanded(CHILD_MARGIN);
            return new Rectangle(body.x(), body.y() - tab.height(), Math.max(body.width(), tab.width()),
                    body.height() + tab.height());
        }
        Dimension text = measure(contents(node), TextStyle.PLAIN);
        double width = Math.max(Math.max(MIN_BODY_WIDTH, tab.width()), text.width() + 2 * PADDING);
        double height = Math.max(MIN_BODY_HEIGHT, text.height() + 2 * PADDING);
        return new Rectangle(node.getX(), node.getY(), width, tab.height() + height);
    }

    @Override
    public void draw(NamedNode node, GraphicsAdapter g) {
        Rectangle bounds = getBounds(node);
        Dimension tab = tabSize(node);
        Rectangle tabArea = new Rectangle(bounds.x(), bounds.y(), tab.width(), tab.height());
        Rectangle body = new Rectangle(bounds.x(), bounds.y() + tab.height(), bounds.width(),
                bounds.height() - tab.height());
        g.drawRectangle(tabArea, theme().getFill(), theme().getStroke());
        g.drawRectangle(body, theme().getFill(), theme().getStroke());
        drawText(g, node.getName(), TextStyle.PLAIN, tabArea, false, true);
        drawText(g, contents(node), TextStyle.PLAIN, body, false, false);
    }
}
