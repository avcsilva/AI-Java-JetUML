package br.uefs.umleditor.command;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;

/**
 * Removes an edge from the diagram.
 */
public class RemoveEdgeOperation implements DiagramOperation {

    private final Diagram diagram;
    private final Edge edge;
    private int index;

    public RemoveEdgeOperation(Diagram diagram, Edge edge) {
        this.diagram = diagram;
        this.edge = edge;
    }

    @Override
    public void execute() {
        index = diagram.removeEdge(edge);
    }

    @Override
    public void undo() {
        diagram.addEdge(index, edge);
    }
}
