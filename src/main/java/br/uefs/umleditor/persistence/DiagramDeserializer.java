package br.uefs.umleditor.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.InvalidDiagramException;
import br.uefs.umleditor.model.NamedNode;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;
import br.uefs.umleditor.model.edges.AggregationEdge;
import br.uefs.umleditor.model.edges.AssociationEdge;
import br.uefs.umleditor.model.edges.CallEdge;
import br.uefs.umleditor.model.edges.ClassRelationshipEdge;
import br.uefs.umleditor.model.edges.DependencyEdge;
import br.uefs.umleditor.model.edges.Directionality;
import br.uefs.umleditor.model.edges.GeneralizationEdge;
import br.uefs.umleditor.model.edges.LabeledEdge;
import br.uefs.umleditor.model.edges.NoteEdge;
import br.uefs.umleditor.model.edges.ObjectCollaborationEdge;
import br.uefs.umleditor.model.edges.ObjectReferenceEdge;
import br.uefs.umleditor.model.edges.ReturnEdge;
import br.uefs.umleditor.model.edges.StateTransitionEdge;
import br.uefs.umleditor.model.edges.UseCaseAssociationEdge;
import br.uefs.umleditor.model.edges.UseCaseDependencyEdge;
import br.uefs.umleditor.model.edges.UseCaseGeneralizationEdge;
import br.uefs.umleditor.model.nodes.ActorNode;
import br.uefs.umleditor.model.nodes.CallNode;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.FinalStateNode;
import br.uefs.umleditor.model.nodes.ImplicitParameterNode;
import br.uefs.umleditor.model.nodes.InitialStateNode;
import br.uefs.umleditor.model.nodes.InterfaceNode;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.ObjectNode;
import br.uefs.umleditor.model.nodes.PackageDescriptionNode;
import br.uefs.umleditor.model.nodes.PackageNode;
import br.uefs.umleditor.model.nodes.PointNode;
import br.uefs.umleditor.model.nodes.StateNode;
import br.uefs.umleditor.model.nodes.UseCaseNode;
import br.uefs.umleditor.model.rules.DiagramStructureValidator;
import br.uefs.umleditor.persistence.json.JsonException;
import br.uefs.umleditor.persistence.json.JsonParser;

/**
 * Reads the JSON format written by {@link DiagramSerializer} (RNF02). It
 * checks that the document is well formed: known types, unique ids,
 * references to existing nodes and children accepted by their parents. The
 * rules of the diagram type are checked afterwards by the structure validator.
 * Missing text properties are read as empty.
 */
public final class DiagramDeserializer {

    private static final Map<String, Supplier<Node>> NODE_TYPES = new LinkedHashMap<>();
    private static final Map<String, Supplier<Edge>> EDGE_TYPES = new LinkedHashMap<>();

    static {
        NODE_TYPES.put("ClassNode", ClassNode::new);
        NODE_TYPES.put("InterfaceNode", InterfaceNode::new);
        NODE_TYPES.put("PackageNode", PackageNode::new);
        NODE_TYPES.put("PackageDescriptionNode", PackageDescriptionNode::new);
        NODE_TYPES.put("NoteNode", NoteNode::new);
        NODE_TYPES.put("PointNode", PointNode::new);
        NODE_TYPES.put("ImplicitParameterNode", ImplicitParameterNode::new);
        NODE_TYPES.put("CallNode", CallNode::new);
        NODE_TYPES.put("StateNode", StateNode::new);
        NODE_TYPES.put("InitialStateNode", InitialStateNode::new);
        NODE_TYPES.put("FinalStateNode", FinalStateNode::new);
        NODE_TYPES.put("ObjectNode", ObjectNode::new);
        NODE_TYPES.put("FieldNode", FieldNode::new);
        NODE_TYPES.put("ActorNode", ActorNode::new);
        NODE_TYPES.put("UseCaseNode", UseCaseNode::new);

        EDGE_TYPES.put("DependencyEdge", DependencyEdge::new);
        EDGE_TYPES.put("GeneralizationEdge", () -> new GeneralizationEdge(GeneralizationEdge.Type.INHERITANCE));
        EDGE_TYPES.put("AssociationEdge", AssociationEdge::new);
        EDGE_TYPES.put("AggregationEdge", () -> new AggregationEdge(AggregationEdge.Type.AGGREGATION));
        EDGE_TYPES.put("NoteEdge", NoteEdge::new);
        EDGE_TYPES.put("CallEdge", CallEdge::new);
        EDGE_TYPES.put("ReturnEdge", ReturnEdge::new);
        EDGE_TYPES.put("StateTransitionEdge", StateTransitionEdge::new);
        EDGE_TYPES.put("ObjectReferenceEdge", ObjectReferenceEdge::new);
        EDGE_TYPES.put("ObjectCollaborationEdge", ObjectCollaborationEdge::new);
        EDGE_TYPES.put("UseCaseAssociationEdge", UseCaseAssociationEdge::new);
        EDGE_TYPES.put("UseCaseDependencyEdge", () -> new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.NONE));
        EDGE_TYPES.put("UseCaseGeneralizationEdge", UseCaseGeneralizationEdge::new);
    }

    private DiagramDeserializer() {
    }

    /**
     * Reads a whole diagram, checking also the rules of its type.
     */
    public static Diagram readDiagram(String json) throws InvalidDiagramException {
        DiagramContent content = read(json);
        return DiagramStructureValidator.build(content.type(), content.width(), content.height(), content.roots(),
                content.edges());
    }

    public static DiagramContent read(String json) throws InvalidDiagramException {
        Object document;
        try {
            document = JsonParser.parse(json);
        } catch (JsonException e) {
            throw new InvalidDiagramException("The file is not valid JSON: " + e.getMessage());
        }
        Map<?, ?> root = asObject(document, "document");
        DiagramType type = diagramType(text(root, "diagram", null));
        int width = (int) number(root, "width", 0);
        int height = (int) number(root, "height", 0);

        Map<Long, Node> nodesById = new LinkedHashMap<>();
        Map<Node, List<?>> childIds = new HashMap<>();
        for (Object item : array(root, "nodes")) {
            Map<?, ?> data = asObject(item, "node");
            long id = (long) number(data, "id", Double.NaN);
            if (nodesById.containsKey(id)) {
                throw new InvalidDiagramException("Two nodes have the id " + id);
            }
            Node node = createNode(data);
            nodesById.put(id, node);
            childIds.put(node, data.containsKey("children") ? asArray(data.get("children"), "children") : List.of());
        }

        Set<Node> children = new HashSet<>();
        for (Node node : nodesById.values()) {
            for (Object childId : childIds.get(node)) {
                Node child = nodeById(nodesById, childId);
                if (!(node instanceof ParentNode parent) || !parent.canAccept(child)) {
                    throw new InvalidDiagramException(child.getClass().getSimpleName() + " cannot be a child of "
                            + node.getClass().getSimpleName());
                }
                if (!children.add(child)) {
                    throw new InvalidDiagramException("A node is the child of more than one node");
                }
                parent.addChild(child);
            }
        }
        List<Node> roots = new ArrayList<>();
        for (Node node : nodesById.values()) {
            if (!children.contains(node)) {
                roots.add(node);
            }
        }

        List<Edge> edges = new ArrayList<>();
        for (Object item : array(root, "edges")) {
            Map<?, ?> data = asObject(item, "edge");
            Edge edge = createEdge(data);
            edge.connect(nodeById(nodesById, data.get("start")), nodeById(nodesById, data.get("end")));
            edges.add(edge);
        }

        DiagramContent content = new DiagramContent(type, width, height, roots, edges);
        if (content.allNodes().size() != nodesById.size()) {
            throw new InvalidDiagramException("The containment of nodes forms a cycle");
        }
        return content;
    }

    public static DiagramType diagramType(String name) throws InvalidDiagramException {
        for (DiagramType type : DiagramType.values()) {
            if (DiagramSerializer.typeName(type).equals(name)) {
                return type;
            }
        }
        throw new InvalidDiagramException("Unknown diagram type: " + name);
    }

    private static Node createNode(Map<?, ?> data) throws InvalidDiagramException {
        String type = text(data, "type", null);
        Supplier<Node> factory = NODE_TYPES.get(type);
        if (factory == null) {
            throw new InvalidDiagramException("Unknown node type: " + type);
        }
        Node node = factory.get();
        node.setPosition(number(data, "x", 0), number(data, "y", 0));
        if (node instanceof NamedNode named) {
            named.setName(text(data, "name", ""));
        }
        if (node instanceof ClassNode classNode) {
            classNode.setAttributes(text(data, "attributes", ""));
            classNode.setMethods(text(data, "methods", ""));
        } else if (node instanceof InterfaceNode interfaceNode) {
            interfaceNode.setMethods(text(data, "methods", ""));
        } else if (node instanceof PackageDescriptionNode description) {
            description.setContents(text(data, "contents", ""));
        } else if (node instanceof FieldNode field) {
            field.setValue(text(data, "value", ""));
        } else if (node instanceof NoteNode note) {
            note.setText(text(data, "text", ""));
        } else if (node instanceof CallNode call) {
            call.setOpenBottom(bool(data, "openBottom"));
        }
        return node;
    }

    private static Edge createEdge(Map<?, ?> data) throws InvalidDiagramException {
        String type = text(data, "type", null);
        Supplier<Edge> factory = EDGE_TYPES.get(type);
        if (factory == null) {
            throw new InvalidDiagramException("Unknown edge type: " + type);
        }
        Edge edge = factory.get();
        if (edge instanceof DependencyEdge dependency) {
            dependency.setDirectionality(choice(data, "directionality", Directionality.values(),
                    Directionality.UNIDIRECTIONAL, Directionality::getDisplayName));
        } else if (edge instanceof GeneralizationEdge generalization) {
            generalization.setType(choice(data, "generalizationType", GeneralizationEdge.Type.values(),
                    GeneralizationEdge.Type.INHERITANCE, GeneralizationEdge.Type::getDisplayName));
        } else if (edge instanceof ClassRelationshipEdge relationship) {
            relationship.setStartLabel(text(data, "startLabel", ""));
            relationship.setMiddleLabel(text(data, "middleLabel", ""));
            relationship.setEndLabel(text(data, "endLabel", ""));
            relationship.setDirectionality(choice(data, "directionality", Directionality.values(),
                    Directionality.UNSPECIFIED, Directionality::getDisplayName));
            if (relationship instanceof AggregationEdge aggregation) {
                aggregation.setType(choice(data, "aggregationType", AggregationEdge.Type.values(),
                        AggregationEdge.Type.AGGREGATION, AggregationEdge.Type::getDisplayName));
            }
        } else if (edge instanceof LabeledEdge labeled) {
            labeled.setMiddleLabel(text(data, "middleLabel", ""));
            if (labeled instanceof CallEdge call) {
                call.setConstructorCall(bool(data, "constructorCall"));
            }
        } else if (edge instanceof UseCaseDependencyEdge dependency) {
            dependency.setType(choice(data, "dependencyType", UseCaseDependencyEdge.Type.values(),
                    UseCaseDependencyEdge.Type.NONE, UseCaseDependencyEdge.Type::getDisplayName));
        }
        return edge;
    }

    private static Node nodeById(Map<Long, Node> nodes, Object id) throws InvalidDiagramException {
        Node node = id instanceof Number number ? nodes.get(number.longValue()) : null;
        if (node == null) {
            throw new InvalidDiagramException("Reference to a node that does not exist: " + id);
        }
        return node;
    }

    private static Map<?, ?> asObject(Object value, String what) throws InvalidDiagramException {
        if (value instanceof Map<?, ?> map) {
            return map;
        }
        throw new InvalidDiagramException("Expected an object for " + what);
    }

    private static List<?> asArray(Object value, String what) throws InvalidDiagramException {
        if (value instanceof List<?> list) {
            return list;
        }
        throw new InvalidDiagramException("Expected an array for " + what);
    }

    private static List<?> array(Map<?, ?> data, String key) throws InvalidDiagramException {
        return data.containsKey(key) ? asArray(data.get(key), key) : List.of();
    }

    private static String text(Map<?, ?> data, String key, String defaultValue) throws InvalidDiagramException {
        Object value = data.get(key);
        if (value == null) {
            if (defaultValue == null) {
                throw new InvalidDiagramException("Missing \"" + key + "\"");
            }
            return defaultValue;
        }
        if (!(value instanceof String text)) {
            throw new InvalidDiagramException("\"" + key + "\" must be a text");
        }
        return text;
    }

    private static double number(Map<?, ?> data, String key, double defaultValue) throws InvalidDiagramException {
        Object value = data.get(key);
        if (value == null) {
            if (Double.isNaN(defaultValue)) {
                throw new InvalidDiagramException("Missing \"" + key + "\"");
            }
            return defaultValue;
        }
        if (!(value instanceof Number number)) {
            throw new InvalidDiagramException("\"" + key + "\" must be a number");
        }
        return number.doubleValue();
    }

    private static boolean bool(Map<?, ?> data, String key) throws InvalidDiagramException {
        Object value = data.get(key);
        if (value == null) {
            return false;
        }
        if (!(value instanceof Boolean bool)) {
            throw new InvalidDiagramException("\"" + key + "\" must be true or false");
        }
        return bool;
    }

    private static <E> E choice(Map<?, ?> data, String key, E[] values, E defaultValue,
                                Function<E, String> name) throws InvalidDiagramException {
        String text = text(data, key, "");
        if (text.isEmpty()) {
            return defaultValue;
        }
        for (E value : values) {
            if (name.apply(value).equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new InvalidDiagramException("Invalid value for \"" + key + "\": " + text);
    }
}
