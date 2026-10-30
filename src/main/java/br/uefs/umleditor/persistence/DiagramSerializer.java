package br.uefs.umleditor.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.NamedNode;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.AggregationEdge;
import br.uefs.umleditor.model.edges.CallEdge;
import br.uefs.umleditor.model.edges.ClassRelationshipEdge;
import br.uefs.umleditor.model.edges.DependencyEdge;
import br.uefs.umleditor.model.edges.GeneralizationEdge;
import br.uefs.umleditor.model.edges.LabeledEdge;
import br.uefs.umleditor.model.edges.UseCaseDependencyEdge;
import br.uefs.umleditor.model.nodes.CallNode;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.InterfaceNode;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.PackageDescriptionNode;
import br.uefs.umleditor.persistence.json.JsonWriter;

/**
 * Writes diagrams in the open JSON format documented in the README (RNF02).
 * Nodes get numeric ids in the order they are written; edges and lists of
 * children refer to nodes by these ids.
 */
public final class DiagramSerializer {

    public static final String FORMAT_VERSION = "1.0";

    private DiagramSerializer() {
    }

    public static String toJson(Diagram diagram) {
        return JsonWriter.write(toMap(diagram.getType(), diagram.getWidth(), diagram.getHeight(),
                diagram.getRootNodes(), diagram.getEdges()));
    }

    /**
     * Writes some nodes, with their children, and the edges between them,
     * such as the elements copied to the clipboard (RF34).
     */
    public static String toJson(DiagramType type, List<Node> roots, List<Edge> edges) {
        return JsonWriter.write(toMap(type, 0, 0, roots, edges));
    }

    public static String typeName(DiagramType type) {
        return switch (type) {
            case CLASS -> "ClassDiagram";
            case SEQUENCE -> "SequenceDiagram";
            case STATE -> "StateDiagram";
            case OBJECT -> "ObjectDiagram";
            case USE_CASE -> "UseCaseDiagram";
        };
    }

    private static Map<String, Object> toMap(DiagramType type, int width, int height, List<Node> roots,
                                             List<Edge> edges) {
        DiagramContent content = new DiagramContent(type, width, height, roots, edges);
        Map<Node, Integer> ids = new HashMap<>();
        for (Node node : content.allNodes()) {
            ids.put(node, ids.size());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("formatVersion", FORMAT_VERSION);
        result.put("diagram", typeName(type));
        result.put("width", width);
        result.put("height", height);
        List<Object> nodes = new ArrayList<>();
        for (Node node : content.allNodes()) {
            nodes.add(nodeToMap(node, ids));
        }
        result.put("nodes", nodes);
        List<Object> edgeList = new ArrayList<>();
        for (Edge edge : edges) {
            if (ids.containsKey(edge.getStart()) && ids.containsKey(edge.getEnd())) {
                edgeList.add(edgeToMap(edge, ids));
            }
        }
        result.put("edges", edgeList);
        return result;
    }

    private static Map<String, Object> nodeToMap(Node node, Map<Node, Integer> ids) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", ids.get(node));
        result.put("type", node.getClass().getSimpleName());
        result.put("x", node.getX());
        result.put("y", node.getY());
        if (node instanceof ClassNode classNode) {
            result.put("name", classNode.getName());
            result.put("attributes", classNode.getAttributes());
            result.put("methods", classNode.getMethods());
        } else if (node instanceof InterfaceNode interfaceNode) {
            result.put("name", interfaceNode.getName());
            result.put("methods", interfaceNode.getMethods());
        } else if (node instanceof PackageDescriptionNode description) {
            result.put("name", description.getName());
            result.put("contents", description.getContents());
        } else if (node instanceof FieldNode field) {
            result.put("name", field.getName());
            result.put("value", field.getValue());
        } else if (node instanceof NamedNode named) {
            result.put("name", named.getName());
        } else if (node instanceof NoteNode note) {
            result.put("text", note.getText());
        } else if (node instanceof CallNode call) {
            result.put("openBottom", call.isOpenBottom());
        }
        List<Object> children = new ArrayList<>();
        for (Node child : node.getChildren()) {
            children.add(ids.get(child));
        }
        result.put("children", children);
        return result;
    }

    private static Map<String, Object> edgeToMap(Edge edge, Map<Node, Integer> ids) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", edge.getClass().getSimpleName());
        result.put("start", ids.get(edge.getStart()));
        result.put("end", ids.get(edge.getEnd()));
        if (edge instanceof DependencyEdge dependency) {
            result.put("directionality", dependency.getDirectionality().getDisplayName());
        } else if (edge instanceof GeneralizationEdge generalization) {
            result.put("generalizationType", generalization.getType().getDisplayName());
        } else if (edge instanceof ClassRelationshipEdge relationship) {
            result.put("startLabel", relationship.getStartLabel());
            result.put("middleLabel", relationship.getMiddleLabel());
            result.put("endLabel", relationship.getEndLabel());
            result.put("directionality", relationship.getDirectionality().getDisplayName());
            if (relationship instanceof AggregationEdge aggregation) {
                result.put("aggregationType", aggregation.getType().getDisplayName());
            }
        } else if (edge instanceof LabeledEdge labeled) {
            result.put("middleLabel", labeled.getMiddleLabel());
            if (labeled instanceof CallEdge call) {
                result.put("constructorCall", call.isConstructorCall());
            }
        } else if (edge instanceof UseCaseDependencyEdge dependency) {
            result.put("dependencyType", dependency.getType().getDisplayName());
        }
        return result;
    }
}
