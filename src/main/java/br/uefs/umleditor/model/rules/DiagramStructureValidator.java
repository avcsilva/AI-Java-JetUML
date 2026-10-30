package br.uefs.umleditor.model.rules;

import java.util.List;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramElement;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.ElementCompatibility;
import br.uefs.umleditor.model.InvalidDiagramException;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.CallEdge;
import br.uefs.umleditor.model.nodes.CallNode;

/**
 * Builds a diagram from elements read from a file, checking the same rules
 * the editor enforces: only elements of the diagram type, no node that
 * requires a parent as a root (RF25), the connection rules (RF18, RF19, RF30)
 * and a single activation without caller in sequence diagrams (RF30).
 */
public final class DiagramStructureValidator {

    private DiagramStructureValidator() {
    }

    public static Diagram build(DiagramType type, int width, int height, List<Node> roots, List<Edge> edges)
            throws InvalidDiagramException {
        Diagram diagram = new Diagram(type, width, height);
        for (Node root : roots) {
            if (root.requiresParent()) {
                throw new InvalidDiagramException("A " + root.getClass().getSimpleName()
                        + " cannot exist without its parent");
            }
            diagram.addRootNode(root);
        }
        for (Node node : diagram.getAllNodes()) {
            checkCompatible(type, node);
        }
        for (Edge edge : edges) {
            checkCompatible(type, edge);
            diagram.addEdge(edge);
        }
        // Each edge is checked against all the others, as if it were the last one created.
        DiagramRules rules = DiagramRules.forType(type);
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            diagram.removeEdge(edge);
            ValidationResult result = rules.check(diagram, edge, edge.getStart(), edge.getEnd());
            diagram.addEdge(i, edge);
            if (!result.isValid()) {
                throw new InvalidDiagramException(result.isError() ? result.message()
                        : "Invalid " + edge.getClass().getSimpleName() + " between "
                                + edge.getStart().getClass().getSimpleName() + " and "
                                + edge.getEnd().getClass().getSimpleName());
            }
        }
        if (type == DiagramType.SEQUENCE) {
            long rootCalls = diagram.getAllNodes().stream()
                    .filter(node -> node instanceof CallNode && diagram.getEdges().stream()
                            .noneMatch(edge -> edge instanceof CallEdge && edge.getEnd() == node))
                    .count();
            if (rootCalls > 1) {
                throw new InvalidDiagramException("A sequence diagram can have only one activation without caller");
            }
        }
        return diagram;
    }

    private static void checkCompatible(DiagramType type, DiagramElement element) throws InvalidDiagramException {
        if (!ElementCompatibility.accepts(type, element)) {
            throw new InvalidDiagramException(element.getClass().getSimpleName() + " is not allowed in a "
                    + type.getDisplayName());
        }
    }
}
