package br.uefs.umleditor.command;

import br.uefs.umleditor.model.Property;

/**
 * Changes the value of a property of an element.
 */
public class ChangePropertyOperation implements DiagramOperation {

    private final Property property;
    private final Object oldValue;
    private final Object newValue;

    public ChangePropertyOperation(Property property, Object oldValue, Object newValue) {
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        property.set(newValue);
    }

    @Override
    public void undo() {
        property.set(oldValue);
    }
}
