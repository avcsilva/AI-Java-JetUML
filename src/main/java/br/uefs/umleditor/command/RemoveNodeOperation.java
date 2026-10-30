package br.uefs.umleditor.command;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;

/**
 * Removes a node, together with its children, from the diagram.
 */
public class RemoveNodeOperation implements DiagramOperation {

    private final Diagram diagram;
    private final Node node;
    private ParentNode parent;
    private int index;

    public RemoveNodeOperation(Diagram diagram, Node node) {
        this.diagram = diagram;
        this.node = node;
    }

    @Override
    public void execute() {
        parent = node.getParent();
        index = parent == null ? diagram.removeRootNode(node) : parent.removeChild(node);
    }

    @Override
    public void undo() {
        if (parent == null) {
            diagram.addRootNode(index, node);
        } else {
            parent.addChild(index, node);
        }
    }
}
