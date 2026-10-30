package br.uefs.umleditor.model.nodes;

import java.util.List;

import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.Property;

/**
 * A note with free text, available in every diagram type (RF13).
 */
public class NoteNode extends Node {

    private String text = "";

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    @Override
    public List<Property> getProperties() {
        return List.of(Property.multilineText("Text", this::getText, this::setText));
    }
}
