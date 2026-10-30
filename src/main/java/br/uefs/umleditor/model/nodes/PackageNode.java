package br.uefs.umleditor.model.nodes;

import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;

/**
 * A package of a class diagram, which can contain classes, interfaces and
 * other packages as children (RF15).
 */
public class PackageNode extends ParentNode {

    @Override
    public boolean canAccept(Node node) {
        return node instanceof ClassNode || node instanceof InterfaceNode || node instanceof PackageNode
                || node instanceof PackageDescriptionNode;
    }
}
