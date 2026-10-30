package br.uefs.umleditor.command;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;

/**
 * Adds a connected edge to the diagram.
 */
public class AddEdgeOperation implements DiagramOperation {

    private final Diagram diagram;
    private final Edge edge;
    private final int index;

    /**
     * Adds the edge after all the others.
     */
    public AddEdgeOperation(Diagram diagram, Edge edge) {
        this(diagram, edge, -1);
    }

    /**
     * Adds the edge at a position of the edge list, which matters for the
     * order of the calls of a sequence diagram.
     */
    public AddEdgeOperation(Diagram diagram, Edge edge, int index) {
        this.diagram = diagram;
        this.edge = edge;
        this.index = index;
    }

    @Override
    public void execute() {
        if (index < 0) {
            diagram.addEdge(edge);
        } else {
            diagram.addEdge(index, edge);
        }
    }

    @Override
    public void undo() {
        diagram.removeEdge(edge);
    }
}
