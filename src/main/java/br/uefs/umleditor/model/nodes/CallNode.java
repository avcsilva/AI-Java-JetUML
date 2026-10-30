package br.uefs.umleditor.model.nodes;

import java.util.List;

import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.Property;

/**
 * An activation box on the lifeline of an implicit parameter (RF27). Its
 * position and height are computed by the sequence layout from the calls it
 * makes; only the vertical position of the first activation of the diagram
 * is chosen by the user. Open Bottom marks an incomplete sequence of calls (RF31).
 */
public class CallNode extends Node {

    public static final double MIN_HEIGHT = 30;

    private boolean openBottom;
    private double height = MIN_HEIGHT;

    public boolean isOpenBottom() {
        return openBottom;
    }

    public void setOpenBottom(boolean openBottom) {
        this.openBottom = openBottom;
    }

    /**
     * @return the height computed by the last layout
     */
    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public boolean requiresParent() {
        return true;
    }

    @Override
    public List<Property> getProperties() {
        return List.of(Property.bool("Open Bottom", this::isOpenBottom, this::setOpenBottom));
    }
}
