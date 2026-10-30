package br.uefs.umleditor.model;

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
}
