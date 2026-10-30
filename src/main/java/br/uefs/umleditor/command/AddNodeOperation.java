package br.uefs.umleditor.command;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;

/**
 * Adds a node to the diagram, as a root node or as the child of a parent node.
 */
public class AddNodeOperation implements DiagramOperation {

    private final Diagram diagram;
    private final Node node;
    private final ParentNode parent;
    private final int index;

    /**
     * @param parent the parent of the node, or null to add it as a root node
     * @param index the position among the siblings
     */
    public AddNodeOperation(Diagram diagram, Node node, ParentNode parent, int index) {
        this.diagram = diagram;
        this.node = node;
        this.parent = parent;
        this.index = index;
    }

    @Override
    public void execute() {
        if (parent == null) {
            diagram.addRootNode(index, node);
        } else {
            parent.addChild(index, node);
        }
    }

    @Override
    public void undo() {
        if (parent == null) {
            diagram.removeRootNode(node);
        } else {
            parent.removeChild(node);
        }
    }
}
