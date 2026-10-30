package br.uefs.umleditor.command;

/**
 * A reversible change to a diagram. Every user action is expressed as an
 * operation so that it can be undone and redone.
 */
public interface DiagramOperation {

    void execute();

    void undo();
}
