package br.uefs.umleditor.model.nodes;

import java.util.List;

import br.uefs.umleditor.model.NamedNode;
import br.uefs.umleditor.model.Property;

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

    @Override
    public List<Property> getProperties() {
        return List.of(Property.multilineText("Name", this::getName, this::setName),
                Property.multilineText("Attributes", this::getAttributes, this::setAttributes),
                Property.multilineText("Methods", this::getMethods, this::setMethods));
    }
}
