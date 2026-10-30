package br.uefs.umleditor.model;

import java.util.List;

/**
 * A node of a diagram. Its position is the top-left corner of its bounds,
 * in absolute diagram coordinates. The bounds themselves depend on the text
 * and font, so they are computed by a {@link BoundsCalculator}.
 */
public abstract class Node {

    private double x;
    private double y;
    private ParentNode parent;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**
     * Places this node without moving its children.
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Moves this node and all of its descendants.
     */
    public void translate(double dx, double dy) {
        x += dx;
        y += dy;
        for (Node child : getChildren()) {
            child.translate(dx, dy);
        }
    }

    public ParentNode getParent() {
        return parent;
    }

    void setParent(ParentNode parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return List.of();
    }

    /**
     * @return true if this node may only exist as the child of another node.
     */
    public boolean requiresParent() {
        return false;
    }

    /**
     * @return true if the given node is this node or one of its ancestors.
     */
    public boolean hasAncestorOrSelf(Node node) {
        for (Node current = this; current != null; current = current.getParent()) {
            if (current == node) {
                return true;
            }
        }
        return false;
    }

    public Node getRoot() {
        Node current = this;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }
}
