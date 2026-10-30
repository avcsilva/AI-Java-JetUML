package br.uefs.umleditor.command;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;

/**
 * Moves a node to another parent, or makes it a root node, keeping its
 * position in the diagram (RF15).
 */
public class ReparentNodeOperation implements DiagramOperation {

    private final Diagram diagram;
    private final Node node;
    private final ParentNode newParent;
    private ParentNode oldParent;
    private int oldIndex;

    /**
     * @param newParent the new parent, or null to make the node a root node
     */
    public ReparentNodeOperation(Diagram diagram, Node node, ParentNode newParent) {
        this.diagram = diagram;
        this.node = node;
        this.newParent = newParent;
    }

    @Override
    public void execute() {
        oldParent = node.getParent();
        oldIndex = oldParent == null ? diagram.removeRootNode(node) : oldParent.removeChild(node);
        if (newParent == null) {
            diagram.addRootNode(node);
        } else {
            newParent.addChild(node);
        }
    }

    @Override
    public void undo() {
        if (newParent == null) {
            diagram.removeRootNode(node);
        } else {
            newParent.removeChild(node);
        }
        if (oldParent == null) {
            diagram.addRootNode(oldIndex, node);
        } else {
            oldParent.addChild(oldIndex, node);
        }
    }
}
