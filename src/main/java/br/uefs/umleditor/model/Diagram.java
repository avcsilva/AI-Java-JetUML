package br.uefs.umleditor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A UML diagram of a given type with a fixed size in pixels (RF38).
 * It holds the root nodes; child nodes are reached through their parents.
 */
public class Diagram {

    public static final int MIN_SIZE = 250;
    public static final int MAX_SIZE = 4000;

    private final DiagramType type;
    private final List<Node> rootNodes = new ArrayList<>();
    private int width;
    private int height;

    public Diagram(DiagramType type, int width, int height) {
        this.type = Objects.requireNonNull(type);
        setSize(width, height);
    }

    public DiagramType getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public final void setSize(int width, int height) {
        this.width = clampSize(width);
        this.height = clampSize(height);
    }

    /**
     * Limits a diagram dimension to the allowed range of 250 to 4000 pixels (RF38).
     */
    public static int clampSize(int value) {
        return Math.max(MIN_SIZE, Math.min(MAX_SIZE, value));
    }

    public List<Node> getRootNodes() {
        return Collections.unmodifiableList(rootNodes);
    }

    public void addRootNode(Node node) {
        addRootNode(rootNodes.size(), node);
    }

    public void addRootNode(int index, Node node) {
        if (node.getParent() != null) {
            throw new IllegalArgumentException("A root node cannot have a parent");
        }
        if (node.requiresParent()) {
            throw new IllegalArgumentException(node.getClass().getSimpleName() + " cannot be a root node");
        }
        rootNodes.add(index, node);
    }

    /**
     * @return the position the node had among the root nodes
     */
    public int removeRootNode(Node node) {
        int index = rootNodes.indexOf(node);
        if (index < 0) {
            throw new IllegalArgumentException("Not a root node of this diagram");
        }
        rootNodes.remove(index);
        return index;
    }

    /**
     * @return all nodes in painting order: each node comes before its children.
     */
    public List<Node> getAllNodes() {
        List<Node> result = new ArrayList<>();
        for (Node root : rootNodes) {
            collect(root, result);
        }
        return result;
    }

    private static void collect(Node node, List<Node> result) {
        result.add(node);
        for (Node child : node.getChildren()) {
            collect(child, result);
        }
    }
}
