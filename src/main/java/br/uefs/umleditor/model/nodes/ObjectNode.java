package br.uefs.umleditor.model.nodes;

import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;

/**
 * An object of an object diagram. Its children are its fields (RF25).
 */
public class ObjectNode extends ParentNode {

    @Override
    public boolean canAccept(Node node) {
        return node instanceof FieldNode;
    }
}
