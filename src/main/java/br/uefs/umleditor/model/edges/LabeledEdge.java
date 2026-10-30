package br.uefs.umleditor.model.edges;

import br.uefs.umleditor.model.Edge;

/**
 * An edge with a middle label, such as a state transition or an object
 * collaboration (RF20).
 */
public abstract class LabeledEdge extends Edge {

    private String middleLabel = "";

    public String getMiddleLabel() {
        return middleLabel;
    }

    public void setMiddleLabel(String middleLabel) {
        this.middleLabel = middleLabel == null ? "" : middleLabel;
    }
}
