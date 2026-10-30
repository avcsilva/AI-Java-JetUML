package br.uefs.umleditor.persistence;

import java.util.ArrayList;
import java.util.List;

import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;

/**
 * Nodes and edges read from JSON, not yet added to a diagram: a whole
 * diagram read from a file, or the elements copied to the clipboard.
 *
 * @param roots the nodes without parent; their children are reached through them
 */
public record DiagramContent(DiagramType type, int width, int height, List<Node> roots, List<Edge> edges) {

    /**
     * @return all nodes, each node before its children
     */
    public List<Node> allNodes() {
        List<Node> result = new ArrayList<>();
        for (Node root : roots) {
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
