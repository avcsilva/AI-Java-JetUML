package br.uefs.umleditor.model.edges;

import java.util.Optional;

import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.ToolType;

/**
 * Creates the edge corresponding to an edge creation tool (RF16).
 */
public final class EdgeFactory {

    private EdgeFactory() {
    }

    /**
     * @return a new, unconnected edge for the tool, or empty if the tool does not create edges
     */
    public static Optional<Edge> create(ToolType tool) {
        Edge edge = switch (tool) {
            case DEPENDENCY -> new DependencyEdge();
            case INHERITANCE -> new GeneralizationEdge(GeneralizationEdge.Type.INHERITANCE);
            case REALIZATION -> new GeneralizationEdge(GeneralizationEdge.Type.IMPLEMENTATION);
            case CLASS_ASSOCIATION -> new AssociationEdge();
            case AGGREGATION -> new AggregationEdge(AggregationEdge.Type.AGGREGATION);
            case COMPOSITION -> new AggregationEdge(AggregationEdge.Type.COMPOSITION);
            case NOTE_CONNECTOR -> new NoteEdge();
            case TRANSITION -> new StateTransitionEdge();
            case REFERENCE -> new ObjectReferenceEdge();
            case COLLABORATION -> new ObjectCollaborationEdge();
            case USE_CASE_ASSOCIATION -> new UseCaseAssociationEdge();
            case INCLUDE -> new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.INCLUDE);
            case EXTEND -> new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.EXTEND);
            case GENERALIZATION -> new UseCaseGeneralizationEdge();
            case CALL -> new CallEdge();
            case RETURN -> new ReturnEdge();
            default -> null;
        };
        return Optional.ofNullable(edge);
    }
}
