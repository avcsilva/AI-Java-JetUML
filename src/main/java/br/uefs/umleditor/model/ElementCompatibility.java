package br.uefs.umleditor.model;

import br.uefs.umleditor.model.edges.EdgeFactory;
import br.uefs.umleditor.model.nodes.CallNode;
import br.uefs.umleditor.model.nodes.NodeFactory;
import br.uefs.umleditor.model.nodes.PointNode;

/**
 * Tells which kinds of element belong to each diagram type: those created by
 * the tools of the type, plus the fixed points of note connectors and, in
 * sequence diagrams, the activation boxes created by calls.
 */
public final class ElementCompatibility {

    private ElementCompatibility() {
    }

    public static boolean accepts(DiagramType type, DiagramElement element) {
        if (element instanceof PointNode) {
            return true;
        }
        if (element instanceof CallNode) {
            return type == DiagramType.SEQUENCE;
        }
        for (ToolType tool : type.getTools()) {
            boolean sameNode = NodeFactory.create(tool).map(node -> node.getClass() == element.getClass())
                    .orElse(false);
            boolean sameEdge = EdgeFactory.create(tool).map(edge -> edge.getClass() == element.getClass())
                    .orElse(false);
            if (sameNode || sameEdge) {
                return true;
            }
        }
        return false;
    }
}
