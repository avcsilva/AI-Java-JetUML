package br.uefs.umleditor.model.rules;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.CallEdge;
import br.uefs.umleditor.model.edges.ReturnEdge;
import br.uefs.umleditor.model.nodes.CallNode;

/**
 * Sequence diagram rules (RF30): calls and returns only connect activation
 * boxes, an activation box is called by a single caller, and a return goes
 * from an activation to the activation that called it, once. The rule of a
 * single activation without caller is enforced when calls are created.
 * Invalid attempts fail silently, without notification.
 */
public class SequenceDiagramRules extends DiagramRules {

    @Override
    protected ValidationResult checkEdge(Diagram diagram, Edge edge, Node start, Node end) {
        if (!(start instanceof CallNode) || !(end instanceof CallNode) || start == end) {
            return ValidationResult.SILENT_FAILURE;
        }
        if (edge instanceof CallEdge) {
            boolean alreadyCalled = diagram.getEdges().stream()
                    .anyMatch(other -> other instanceof CallEdge && other.getEnd() == end);
            return alreadyCalled ? ValidationResult.SILENT_FAILURE : ValidationResult.VALID;
        }
        if (edge instanceof ReturnEdge) {
            boolean calledByEnd = count(diagram, CallEdge.class, end, start) > 0;
            boolean alreadyReturned = diagram.getEdges().stream()
                    .anyMatch(other -> other instanceof ReturnEdge && other.getStart() == start);
            return calledByEnd && !alreadyReturned ? ValidationResult.VALID : ValidationResult.SILENT_FAILURE;
        }
        return ValidationResult.SILENT_FAILURE;
    }

    /**
     * @return the activation that called the given one, or null for the first activation
     */
    public static CallNode callerOf(Diagram diagram, CallNode callee) {
        return diagram.getEdges().stream()
                .filter(edge -> edge instanceof CallEdge && edge.getEnd() == callee)
                .map(edge -> (CallNode) edge.getStart())
                .findFirst().orElse(null);
    }
}
