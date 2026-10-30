package br.uefs.umleditor.model.nodes;

import java.util.List;

import br.uefs.umleditor.model.NamedNode;
import br.uefs.umleditor.model.Property;

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

    @Override
    public List<Property> getProperties() {
        return List.of(Property.multilineText("Name", this::getName, this::setName),
                Property.multilineText("Methods", this::getMethods, this::setMethods));
    }
}
