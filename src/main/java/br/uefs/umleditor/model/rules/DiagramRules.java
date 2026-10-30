package br.uefs.umleditor.model.rules;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.NoteEdge;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.PointNode;

/**
 * Rules that decide whether an edge may connect two nodes in a diagram
 * (RF18, RF19). Note connectors follow the same rules in every diagram type;
 * the other edges are checked by the rules of each diagram type.
 */
public abstract class DiagramRules {

    public static DiagramRules forType(DiagramType type) {
        return switch (type) {
            case CLASS -> new ClassDiagramRules();
            case SEQUENCE -> new SequenceDiagramRules();
            case STATE -> new StateDiagramRules();
            case OBJECT -> new ObjectDiagramRules();
            case USE_CASE -> new UseCaseDiagramRules();
        };
    }

    /**
     * Checks whether the edge may be added from start to end. The edge is
     * not yet connected nor part of the diagram.
     */
    public ValidationResult check(Diagram diagram, Edge edge, Node start, Node end) {
        if (edge instanceof NoteEdge) {
            return checkNoteEdge(diagram, start, end);
        }
        return checkEdge(diagram, edge, start, end);
    }

    protected abstract ValidationResult checkEdge(Diagram diagram, Edge edge, Node start, Node end);

    /**
     * A note connector links an element to a note, or a note to a fixed point (RF14).
     */
    private static ValidationResult checkNoteEdge(Diagram diagram, Node start, Node end) {
        boolean elementToNote = end instanceof NoteNode && !(start instanceof PointNode) && start != end;
        boolean noteToPoint = start instanceof NoteNode && end instanceof PointNode;
        if (!elementToNote && !noteToPoint) {
            return ValidationResult.error("A note connector must link an element to a note, or a note to a point");
        }
        if (count(diagram, NoteEdge.class, start, end) > 0) {
            return duplicate("note connector");
        }
        return ValidationResult.VALID;
    }

    /**
     * @return the number of edges of the type going from start to end
     */
    protected static long count(Diagram diagram, Class<? extends Edge> type, Node start, Node end) {
        return diagram.getEdges().stream()
                .filter(edge -> type.isInstance(edge) && edge.getStart() == start && edge.getEnd() == end)
                .count();
    }

    /**
     * @return true if an edge of the type links the two nodes, in either direction
     */
    protected static boolean linked(Diagram diagram, Class<? extends Edge> type, Node first, Node second) {
        return diagram.getEdges().stream().anyMatch(edge -> type.isInstance(edge) && edge.links(first, second));
    }

    protected static boolean isAny(Node node, Class<?>... types) {
        for (Class<?> type : types) {
            if (type.isInstance(node)) {
                return true;
            }
        }
        return false;
    }

    protected static ValidationResult duplicate(String edgeName) {
        return ValidationResult.error("There is already a " + edgeName + " between these elements");
    }

    protected static ValidationResult invalidEnds(String edgeName, String allowed) {
        return ValidationResult.error("A " + edgeName + " can only connect " + allowed);
    }
}
