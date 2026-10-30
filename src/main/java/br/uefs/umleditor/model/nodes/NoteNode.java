package br.uefs.umleditor.model.nodes;

import br.uefs.umleditor.model.Node;

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
}
