package br.uefs.umleditor.model;

/**
 * A connection from a start node to an end node. The path of an edge is
 * not stored: it is computed from the position of its nodes (RF17).
 */
public abstract class Edge implements DiagramElement {

    private Node start;
    private Node end;

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public void connect(Node start, Node end) {
        this.start = start;
        this.end = end;
    }

    /**
     * @return true if the edge links the two nodes, in either direction
     */
    public boolean links(Node first, Node second) {
        return (start == first && end == second) || (start == second && end == first);
    }
}
