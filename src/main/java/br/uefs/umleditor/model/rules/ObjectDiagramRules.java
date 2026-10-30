package br.uefs.umleditor.model.rules;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.ObjectCollaborationEdge;
import br.uefs.umleditor.model.edges.ObjectReferenceEdge;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.ObjectNode;

/**
 * Object diagram rules: references go from a field to an object (RF26);
 * collaborations link two different objects (RF19), without duplicates or
 * cycles between the same two objects (RF18).
 */
public class ObjectDiagramRules extends DiagramRules {

    @Override
    protected ValidationResult checkEdge(Diagram diagram, Edge edge, Node start, Node end) {
        if (edge instanceof ObjectReferenceEdge) {
            if (!(start instanceof FieldNode) || !(end instanceof ObjectNode)) {
                return invalidEnds("reference", "the value of a field to an object");
            }
            if (count(diagram, ObjectReferenceEdge.class, start, end) > 0) {
                return duplicate("reference");
            }
            return ValidationResult.VALID;
        }
        if (!(start instanceof ObjectNode) || !(end instanceof ObjectNode)) {
            return invalidEnds("collaboration", "objects");
        }
        if (start == end) {
            return ValidationResult.error("An object cannot collaborate with itself");
        }
        if (count(diagram, ObjectCollaborationEdge.class, start, end) > 0) {
            return duplicate("collaboration");
        }
        if (count(diagram, ObjectCollaborationEdge.class, end, start) > 0) {
            return ValidationResult.error("A collaboration in the opposite direction already exists: "
                    + "collaboration cycles are not allowed");
        }
        return ValidationResult.VALID;
    }
}
