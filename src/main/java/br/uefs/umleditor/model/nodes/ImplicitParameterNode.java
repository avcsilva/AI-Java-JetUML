package br.uefs.umleditor.model.nodes;

import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;

/**
 * An object taking part in a sequence diagram, drawn with its lifeline.
 * Its children will be its activation boxes.
 */
public class ImplicitParameterNode extends ParentNode {

    @Override
    public boolean canAccept(Node node) {
        return false;
    }
}
