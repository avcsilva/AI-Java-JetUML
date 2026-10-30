package br.uefs.umleditor.model;

import java.util.List;

/**
 * A node with an editable name.
 */
public abstract class NamedNode extends Node {

    private String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    @Override
    public List<Property> getProperties() {
        return List.of(Property.text("Name", this::getName, this::setName));
    }
}
