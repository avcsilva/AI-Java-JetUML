package br.uefs.umleditor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named node that can contain child nodes, such as a package (RF15)
 * or an object with its fields (RF25).
 */
public abstract class ParentNode extends NamedNode {

    private final List<Node> children = new ArrayList<>();

    @Override
    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * @return true if the given node may be a child of this node.
     */
    public abstract boolean canAccept(Node node);

    public void addChild(Node child) {
        addChild(children.size(), child);
    }

    public void addChild(int index, Node child) {
        if (!canAccept(child)) {
            throw new IllegalArgumentException(child.getClass().getSimpleName() + " cannot be a child of "
                    + getClass().getSimpleName());
        }
        child.setParent(this);
        children.add(index, child);
    }

    /**
     * @return the position the child had in this node
     */
    public int removeChild(Node child) {
        int index = children.indexOf(child);
        if (index < 0) {
            throw new IllegalArgumentException("Not a child of this node");
        }
        children.remove(index);
        child.setParent(null);
        return index;
    }
}
