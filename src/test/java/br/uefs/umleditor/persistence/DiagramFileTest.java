package br.uefs.umleditor.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.InvalidDiagramException;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;
import br.uefs.umleditor.model.edges.AggregationEdge;
import br.uefs.umleditor.model.edges.CallEdge;
import br.uefs.umleditor.model.edges.Directionality;
import br.uefs.umleditor.model.edges.GeneralizationEdge;
import br.uefs.umleditor.model.edges.NoteEdge;
import br.uefs.umleditor.model.edges.ObjectReferenceEdge;
import br.uefs.umleditor.model.edges.ReturnEdge;
import br.uefs.umleditor.model.edges.StateTransitionEdge;
import br.uefs.umleditor.model.edges.UseCaseDependencyEdge;
import br.uefs.umleditor.model.nodes.ActorNode;
import br.uefs.umleditor.model.nodes.CallNode;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.ImplicitParameterNode;
import br.uefs.umleditor.model.nodes.InitialStateNode;
import br.uefs.umleditor.model.nodes.InterfaceNode;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.ObjectNode;
import br.uefs.umleditor.model.nodes.PackageNode;
import br.uefs.umleditor.model.nodes.PointNode;
import br.uefs.umleditor.model.nodes.StateNode;
import br.uefs.umleditor.model.nodes.UseCaseNode;

/**
 * Saving and opening diagrams in the JSON format (RF02, RNF02), and the
 * checks made when a file is opened.
 */
class DiagramFileTest {

    private static <N extends Node> N at(N node, double x, double y) {
        node.setPosition(x, y);
        return node;
    }

    private static Edge connect(Diagram diagram, Edge edge, Node start, Node end) {
        edge.connect(start, end);
        diagram.addEdge(edge);
        return edge;
    }

    private static Diagram reopen(Diagram diagram) throws InvalidDiagramException {
        return DiagramDeserializer.readDiagram(DiagramSerializer.toJson(diagram));
    }

    @Test
    void classDiagramRoundTrip() throws InvalidDiagramException {
        Diagram diagram = new Diagram(DiagramType.CLASS, 800, 600);
        PackageNode pkg = at(new PackageNode(), 10, 10);
        pkg.setName("domain");
        ClassNode shape = at(new ClassNode(), 30, 40);
        shape.setName("/Shape/");
        shape.setAttributes("- id : int\n_count_");
        shape.setMethods("+ area() : double");
        pkg.addChild(shape);
        InterfaceNode drawable = at(new InterfaceNode(), 300, 40);
        drawable.setName("Drawable");
        NoteNode note = at(new NoteNode(), 500, 20);
        note.setText("A \"quoted\" note");
        PointNode point = at(new PointNode(), 700, 300);
        diagram.addRootNode(pkg);
        diagram.addRootNode(drawable);
        diagram.addRootNode(note);
        diagram.addRootNode(point);
        connect(diagram, new GeneralizationEdge(GeneralizationEdge.Type.IMPLEMENTATION), shape, drawable);
        AggregationEdge composition = new AggregationEdge(AggregationEdge.Type.COMPOSITION);
        composition.setStartLabel("1");
        composition.setEndLabel("*");
        composition.setDirectionality(Directionality.BIDIRECTIONAL);
        connect(diagram, composition, drawable, shape);
        connect(diagram, new NoteEdge(), note, point);

        String json = DiagramSerializer.toJson(diagram);
        Diagram read = DiagramDeserializer.readDiagram(json);
        assertEquals(json, DiagramSerializer.toJson(read));
        ClassNode readShape = (ClassNode) read.getRootNodes().get(0).getChildren().get(0);
        assertEquals("/Shape/", readShape.getName());
        assertEquals(30, readShape.getX());
        AggregationEdge readComposition = (AggregationEdge) read.getEdges().get(1);
        assertEquals(AggregationEdge.Type.COMPOSITION, readComposition.getType());
        assertEquals(Directionality.BIDIRECTIONAL, readComposition.getDirectionality());
        assertEquals(800, read.getWidth());
        assertTrue(json.contains("\"diagram\": \"ClassDiagram\""));
        assertTrue(json.contains("\"type\": \"ClassNode\""));
    }

    @Test
    void otherDiagramTypesRoundTrip() throws InvalidDiagramException {
        Diagram objects = new Diagram(DiagramType.OBJECT, 800, 600);
        ObjectNode list = at(new ObjectNode(), 0, 0);
        FieldNode head = new FieldNode();
        head.setName("head");
        head.setValue("");
        list.addChild(head);
        objects.addRootNode(list);
        connect(objects, new ObjectReferenceEdge(), head, list);
        assertEquals(DiagramSerializer.toJson(objects), DiagramSerializer.toJson(reopen(objects)));

        Diagram states = new Diagram(DiagramType.STATE, 800, 600);
        InitialStateNode initial = at(new InitialStateNode(), 0, 0);
        StateNode idle = at(new StateNode(), 100, 0);
        states.addRootNode(initial);
        states.addRootNode(idle);
        StateTransitionEdge start = new StateTransitionEdge();
        start.setMiddleLabel("start");
        connect(states, start, initial, idle);
        connect(states, new StateTransitionEdge(), idle, idle);
        assertEquals(DiagramSerializer.toJson(states), DiagramSerializer.toJson(reopen(states)));

        Diagram useCases = new Diagram(DiagramType.USE_CASE, 800, 600);
        UseCaseNode a = at(new UseCaseNode(), 0, 0);
        UseCaseNode b = at(new UseCaseNode(), 200, 0);
        useCases.addRootNode(at(new ActorNode(), 0, 200));
        useCases.addRootNode(a);
        useCases.addRootNode(b);
        connect(useCases, new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.EXTEND), a, b);
        Diagram readUseCases = reopen(useCases);
        assertEquals(UseCaseDependencyEdge.Type.EXTEND,
                ((UseCaseDependencyEdge) readUseCases.getEdges().get(0)).getType());
    }

    private static Diagram sequence() {
        Diagram diagram = new Diagram(DiagramType.SEQUENCE, 800, 600);
        ImplicitParameterNode a = at(new ImplicitParameterNode(), 0, 0);
        ImplicitParameterNode b = at(new ImplicitParameterNode(), 200, 0);
        CallNode caller = at(new CallNode(), 0, 80);
        CallNode callee = at(new CallNode(), 200, 100);
        callee.setOpenBottom(true);
        a.addChild(caller);
        b.addChild(callee);
        diagram.addRootNode(a);
        diagram.addRootNode(b);
        CallEdge call = (CallEdge) connect(diagram, new CallEdge(), caller, callee);
        call.setConstructorCall(true);
        call.setMiddleLabel(CallEdge.CREATE_LABEL);
        connect(diagram, new ReturnEdge(), callee, caller);
        return diagram;
    }

    @Test
    void sequenceDiagramRoundTrip() throws InvalidDiagramException {
        Diagram diagram = sequence();
        Diagram read = reopen(diagram);
        assertEquals(DiagramSerializer.toJson(diagram), DiagramSerializer.toJson(read));
        CallEdge call = (CallEdge) read.getEdges().get(0);
        assertTrue(call.isConstructorCall());
        assertTrue(((CallNode) call.getEnd()).isOpenBottom());
    }

    private static void assertRejected(String json) {
        assertThrows(InvalidDiagramException.class, () -> DiagramDeserializer.readDiagram(json));
    }

    private static String document(String type, String nodes, String edges) {
        return "{\"diagram\": \"" + type + "\", \"width\": 500, \"height\": 500, \"nodes\": [" + nodes
                + "], \"edges\": [" + edges + "]}";
    }

    @Test
    void rejectsMalformedOrInvalidFiles() {
        assertRejected("not json");
        assertRejected(document("BoxDiagram", "", ""));
        assertRejected(document("ClassDiagram", "{\"id\": 0, \"type\": \"Spaceship\"}", ""));
        assertRejected(document("ClassDiagram", "{\"id\": 0, \"type\": \"ClassNode\"}, "
                + "{\"id\": 0, \"type\": \"ClassNode\"}", ""));
        assertRejected(document("ClassDiagram", "{\"id\": 0, \"type\": \"ClassNode\", \"children\": [7]}", ""));
        assertRejected(document("ClassDiagram", "{\"id\": 0, \"type\": \"ClassNode\"}",
                "{\"type\": \"DependencyEdge\", \"start\": 0, \"end\": 3}"));
        assertRejected(document("ClassDiagram", "{\"id\": 0, \"type\": \"StateNode\"}", ""));
        assertRejected(document("ClassDiagram", "{\"id\": 0, \"type\": \"PackageNode\", \"children\": [1]}, "
                + "{\"id\": 1, \"type\": \"NoteNode\"}", ""));
    }

    @Test
    void rejectsFieldWithoutObject() {
        assertRejected(document("ObjectDiagram", "{\"id\": 0, \"type\": \"FieldNode\", \"name\": \"x\"}", ""));
    }

    @Test
    void rejectsConnectionsThatBreakTheRules() {
        String twoClasses = "{\"id\": 0, \"type\": \"ClassNode\"}, {\"id\": 1, \"type\": \"ClassNode\"}";
        String dependency = "{\"type\": \"DependencyEdge\", \"start\": 0, \"end\": 1}";
        assertRejected(document("ClassDiagram", twoClasses, dependency + ", " + dependency));
        assertRejected(document("ClassDiagram", twoClasses,
                "{\"type\": \"GeneralizationEdge\", \"start\": 0, \"end\": 0}"));
    }

    @Test
    void rejectsSequenceWithTwoActivationsWithoutCaller() {
        String nodes = "{\"id\": 0, \"type\": \"ImplicitParameterNode\", \"children\": [1, 2]}, "
                + "{\"id\": 1, \"type\": \"CallNode\"}, {\"id\": 2, \"type\": \"CallNode\"}";
        assertRejected(document("SequenceDiagram", nodes, ""));
    }

    @Test
    void contentOfCopiedElementsKeepsStructure() throws InvalidDiagramException {
        Diagram diagram = sequence();
        List<Node> roots = diagram.getRootNodes();
        DiagramContent content = DiagramDeserializer.read(DiagramSerializer.toJson(DiagramType.SEQUENCE, roots,
                diagram.getEdges()));
        assertEquals(2, content.roots().size());
        assertEquals(4, content.allNodes().size());
        assertEquals(2, content.edges().size());
        assertInstanceOf(ParentNode.class, content.roots().get(0));
    }
}
