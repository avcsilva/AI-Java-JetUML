package br.uefs.umleditor.model.rules;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.UseCaseAssociationEdge;
import br.uefs.umleditor.model.edges.UseCaseDependencyEdge;
import br.uefs.umleditor.model.nodes.ActorNode;
import br.uefs.umleditor.model.nodes.UseCaseNode;

/**
 * Use case diagram rules: no edge from an actor or use case to itself (RF19)
 * and no duplicates (RF18). Include and extend link use cases, and a
 * generalization links two actors or two use cases.
 */
public class UseCaseDiagramRules extends DiagramRules {

    @Override
    protected ValidationResult checkEdge(Diagram diagram, Edge edge, Node start, Node end) {
        String name = name(edge);
        if (edge instanceof UseCaseAssociationEdge) {
            if (!isAny(start, ActorNode.class, UseCaseNode.class) || !isAny(end, ActorNode.class, UseCaseNode.class)) {
                return invalidEnds(name, "actors and use cases");
            }
        } else if (edge instanceof UseCaseDependencyEdge) {
            if (!(start instanceof UseCaseNode) || !(end instanceof UseCaseNode)) {
                return invalidEnds(name, "use cases");
            }
        } else if (!(start instanceof ActorNode && end instanceof ActorNode
                || start instanceof UseCaseNode && end instanceof UseCaseNode)) {
            return invalidEnds(name, "two actors or two use cases");
        }
        if (start == end) {
            return ValidationResult.error("An actor or use case cannot have a " + name + " to itself");
        }
        if (count(diagram, edge.getClass(), start, end) > 0) {
            return duplicate(name);
        }
        return ValidationResult.VALID;
    }

    private static String name(Edge edge) {
        if (edge instanceof UseCaseAssociationEdge) {
            return "association";
        }
        if (edge instanceof UseCaseDependencyEdge dependency) {
            return dependency.getType() == UseCaseDependencyEdge.Type.NONE ? "dependency"
                    : dependency.getType().getDisplayName().toLowerCase();
        }
        return "generalization";
    }
}
