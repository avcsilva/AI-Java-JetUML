package br.uefs.umleditor.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * History of the operations performed on a diagram, in order.
 */
public class UndoManager {

    private final Deque<DiagramOperation> done = new ArrayDeque<>();
    private final Deque<DiagramOperation> undone = new ArrayDeque<>();

    public void execute(DiagramOperation operation) {
        operation.execute();
        record(operation);
    }

    /**
     * Stores an operation whose effect was already applied, such as a move
     * done progressively while dragging.
     */
    public void record(DiagramOperation operation) {
        done.push(operation);
        undone.clear();
    }

    public boolean canUndo() {
        return !done.isEmpty();
    }

    public boolean canRedo() {
        return !undone.isEmpty();
    }

    public void undo() {
        if (canUndo()) {
            DiagramOperation operation = done.pop();
            operation.undo();
            undone.push(operation);
        }
    }

    public void redo() {
        if (canRedo()) {
            DiagramOperation operation = undone.pop();
            operation.execute();
            done.push(operation);
        }
    }
}
