package br.uefs.umleditor.model.nodes;

import br.uefs.umleditor.model.NamedNode;

/**
 * A class of a class diagram, with name, attributes and methods.
 */
public class ClassNode extends NamedNode {

    private String attributes = "";
    private String methods = "";

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes == null ? "" : attributes;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods == null ? "" : methods;
    }
}
