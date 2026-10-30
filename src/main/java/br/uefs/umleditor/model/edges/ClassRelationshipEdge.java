package br.uefs.umleditor.model.edges;

import java.util.List;

import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Property;

/**
 * Structural relationship between classes, with start, middle and end labels
 * and directionality (RF20).
 */
public abstract class ClassRelationshipEdge extends Edge {

    private String startLabel = "";
    private String middleLabel = "";
    private String endLabel = "";
    private Directionality directionality = Directionality.UNSPECIFIED;

    public String getStartLabel() {
        return startLabel;
    }

    public void setStartLabel(String startLabel) {
        this.startLabel = startLabel == null ? "" : startLabel;
    }

    public String getMiddleLabel() {
        return middleLabel;
    }

    public void setMiddleLabel(String middleLabel) {
        this.middleLabel = middleLabel == null ? "" : middleLabel;
    }

    public String getEndLabel() {
        return endLabel;
    }

    public void setEndLabel(String endLabel) {
        this.endLabel = endLabel == null ? "" : endLabel;
    }

    public Directionality getDirectionality() {
        return directionality;
    }

    public void setDirectionality(Directionality directionality) {
        this.directionality = directionality;
    }

    @Override
    public List<Property> getProperties() {
        return List.of(Property.text("Start Label", this::getStartLabel, this::setStartLabel),
                Property.text("Middle Label", this::getMiddleLabel, this::setMiddleLabel),
                Property.text("End Label", this::getEndLabel, this::setEndLabel),
                Property.choice("Directionality", List.of(Directionality.values()), this::getDirectionality,
                        this::setDirectionality));
    }
}
