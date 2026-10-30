package br.uefs.umleditor.model.rules;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.AggregationEdge;
import br.uefs.umleditor.model.edges.AssociationEdge;
import br.uefs.umleditor.model.edges.DependencyEdge;
import br.uefs.umleditor.model.edges.GeneralizationEdge;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.InterfaceNode;
import br.uefs.umleditor.model.nodes.PackageDescriptionNode;
import br.uefs.umleditor.model.nodes.PackageNode;

/**
 * Class diagram rules: no duplicates, no direct cycles of inheritance,
 * dependency, aggregation or association, no association combined with an
 * aggregation (RF18), and no self-inheritance or self-dependency (RF19).
 * Aggregation and composition are the same edge type, distinguished by a
 * property, so both count as aggregation.
 */
public class ClassDiagramRules extends DiagramRules {

    @Override
    protected ValidationResult checkEdge(Diagram diagram, Edge edge, Node start, Node end) {
        String name = name(edge);
        if (edge instanceof DependencyEdge) {
            if (!isAny(start, ClassNode.class, InterfaceNode.class, PackageNode.class, PackageDescriptionNode.class)
                    || !isAny(end, ClassNode.class, InterfaceNode.class, PackageNode.class,
                            PackageDescriptionNode.class)) {
                return invalidEnds(name, "classes, interfaces and packages");
            }
        } else if (!isAny(start, ClassNode.class, InterfaceNode.class)
                || !isAny(end, ClassNode.class, InterfaceNode.class)) {
            return invalidEnds(name, "classes and interfaces");
        }
        if (start == end && (edge instanceof DependencyEdge || edge instanceof GeneralizationEdge)) {
            return ValidationResult.error("An element cannot have a " + name + " to itself");
        }
        if (count(diagram, edge.getClass(), start, end) > 0) {
            return duplicate(name);
        }
        if (count(diagram, edge.getClass(), end, start) > 0) {
            return ValidationResult.error("A " + name + " in the opposite direction already exists: "
                    + "direct cycles are not allowed");
        }
        if (edge instanceof AssociationEdge && linked(diagram, AggregationEdge.class, start, end)
                || edge instanceof AggregationEdge && linked(diagram, AssociationEdge.class, start, end)) {
            return ValidationResult.error("Two classes cannot be linked by both an association and an aggregation");
        }
        return ValidationResult.VALID;
    }

    private static String name(Edge edge) {
        if (edge instanceof DependencyEdge) {
            return "dependency";
        }
        if (edge instanceof GeneralizationEdge generalization) {
            return generalization.getType() == GeneralizationEdge.Type.INHERITANCE ? "inheritance" : "realization";
        }
        if (edge instanceof AggregationEdge aggregation) {
            return aggregation.getType() == AggregationEdge.Type.AGGREGATION ? "aggregation" : "composition";
        }
        return "association";
    }
}
