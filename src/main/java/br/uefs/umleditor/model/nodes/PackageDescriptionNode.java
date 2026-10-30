package br.uefs.umleditor.model.nodes;

import br.uefs.umleditor.model.NamedNode;

/**
 * A package shown with a textual description of its contents.
 */
public class PackageDescriptionNode extends NamedNode {

    private String contents = "";

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents == null ? "" : contents;
    }
}
