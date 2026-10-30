package br.uefs.umleditor.model.nodes;

import java.util.List;

import br.uefs.umleditor.model.NamedNode;
import br.uefs.umleditor.model.Property;

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

    @Override
    public List<Property> getProperties() {
        return List.of(Property.text("Name", this::getName, this::setName),
                Property.multilineText("Contents", this::getContents, this::setContents));
    }
}
