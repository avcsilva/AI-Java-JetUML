package br.uefs.umleditor.command;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;

/**
 * Adds a connected edge to the diagram.
 */
public class AddEdgeOperation implements DiagramOperation {

    private final Diagram diagram;
    private final Edge edge;

    public AddEdgeOperation(Diagram diagram, Edge edge) {
        this.diagram = diagram;
        this.edge = edge;
    }

    @Override
    public void execute() {
        diagram.addEdge(edge);
    }

    @Override
    public void undo() {
        diagram.removeEdge(edge);
    }
}
