package br.uefs.umleditor.model.nodes;

import java.util.List;

import br.uefs.umleditor.model.NamedNode;
import br.uefs.umleditor.model.Property;

/**
 * A field of an object, with name and value. A field always belongs to an
 * object and never exists as a root node (RF25).
 */
public class FieldNode extends NamedNode {

    private String value = "";

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? "" : value;
    }

    @Override
    public boolean requiresParent() {
        return true;
    }

    @Override
    public List<Property> getProperties() {
        return List.of(Property.text("Name", this::getName, this::setName),
                Property.text("Value", this::getValue, this::setValue));
    }
}
