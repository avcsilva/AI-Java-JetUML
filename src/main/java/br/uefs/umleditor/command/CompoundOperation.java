package br.uefs.umleditor.command;

import java.util.List;

/**
 * Several operations executed as a single action, and undone in reverse order.
 */
public class CompoundOperation implements DiagramOperation {

    private final List<DiagramOperation> operations;

    public CompoundOperation(List<DiagramOperation> operations) {
        this.operations = List.copyOf(operations);
    }

    @Override
    public void execute() {
        for (DiagramOperation operation : operations) {
            operation.execute();
        }
    }

    @Override
    public void undo() {
        for (int i = operations.size() - 1; i >= 0; i--) {
            operations.get(i).undo();
        }
    }
}
