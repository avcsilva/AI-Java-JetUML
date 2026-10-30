package br.uefs.umleditor.model.nodes;

import br.uefs.umleditor.model.NamedNode;

/**
 * An interface of a class diagram, with name and methods.
 */
public class InterfaceNode extends NamedNode {

    private String methods = "";

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods == null ? "" : methods;
    }
}
