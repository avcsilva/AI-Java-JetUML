package br.uefs.umleditor.model.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.builder.DiagramBuilder;
import br.uefs.umleditor.model.edges.AggregationEdge;
import br.uefs.umleditor.model.edges.AssociationEdge;
import br.uefs.umleditor.model.edges.DependencyEdge;
import br.uefs.umleditor.model.edges.GeneralizationEdge;
import br.uefs.umleditor.model.edges.NoteEdge;
import br.uefs.umleditor.model.edges.ObjectCollaborationEdge;
import br.uefs.umleditor.model.edges.ObjectReferenceEdge;
import br.uefs.umleditor.model.edges.StateTransitionEdge;
import br.uefs.umleditor.model.edges.UseCaseAssociationEdge;
import br.uefs.umleditor.model.edges.UseCaseDependencyEdge;
import br.uefs.umleditor.model.edges.UseCaseGeneralizationEdge;
import br.uefs.umleditor.model.nodes.ActorNode;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.FinalStateNode;
import br.uefs.umleditor.model.nodes.InitialStateNode;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.ObjectNode;
import br.uefs.umleditor.model.nodes.PointNode;
import br.uefs.umleditor.model.nodes.StateNode;
import br.uefs.umleditor.model.nodes.UseCaseNode;

/**
 * Edge creation through the builder, checking the rules of RF14, RF16,
 * RF18, RF19 and RF26.
 */
class EdgeRulesTest {

    /**
     * Nodes are 100 x 60. Fields take the first row of their object, and their
     * value part is the right half of the row.
     */
    private static final BoundsCalculator BOUNDS = new BoundsCalculator() {
        @Override
        public Rectangle getBounds(Node node) {
            if (node instanceof FieldNode field) {
                Node parent = field.getParent();
                int index = parent.getChildren().indexOf(field);
                return new Rectangle(parent.getX(), parent.getY() + 30 + index * 10, 100, 10);
            }
            if (node instanceof PointNode) {
                return new Rectangle(node.getX(), node.getY(), 0, 0);
            }
            return new Rectangle(node.getX(), node.getY(), 100, 60);
        }

        @Override
        public Rectangle getConnectionBounds(Node node) {
            Rectangle row = getBounds(node);
            return node instanceof FieldNode ? new Rectangle(row.x() + 50, row.y(), 50, row.height()) : row;
        }
    };

    private Diagram diagram;
    private DiagramBuilder builder;

    private void newDiagram(DiagramType type) {
        diagram = new Diagram(type, 2000, 2000);
        builder = new DiagramBuilder(diagram, BOUNDS);
    }

    private <N extends Node> N node(N node, double x, double y) {
        node.setPosition(x, y);
        diagram.addRootNode(node);
        return node;
    }

    private static Point center(Node node) {
        return BOUNDS.getBounds(node).center();
    }

    /**
     * Drags an edge between the centers of two nodes and applies it when valid.
     */
    private ValidationResult connect(Edge edge, Node start, Node end) {
        return connect(edge, center(start), center(end));
    }

    private ValidationResult connect(Edge edge, Point start, Point end) {
        DiagramBuilder.EdgeCreation creation = builder.createEdge(edge, start, end);
        if (creation.validation().isValid()) {
            creation.operation().execute();
        }
        return creation.validation();
    }

    private static void assertValid(ValidationResult result) {
        assertTrue(result.isValid(), result.message());
    }

    private static void assertError(ValidationResult result) {
        assertTrue(result.isError(), "expected an error notification");
    }

    @Test
    void createsEdgeBetweenNodesAndIgnoresDropOnEmptySpace() {
        newDiagram(DiagramType.CLASS);
        ClassNode a = node(new ClassNode(), 0, 0);
        ClassNode b = node(new ClassNode(), 300, 0);
        AssociationEdge edge = new AssociationEdge();
        assertValid(connect(edge, a, b));
        assertSame(a, edge.getStart());
        assertSame(b, edge.getEnd());
        assertEquals(List.of(edge), diagram.getEdges());

        assertEquals(ValidationResult.SILENT_FAILURE, connect(new DependencyEdge(), center(a), new Point(800, 800)));
        assertEquals(1, diagram.getEdges().size());
    }

    @Test
    void classDiagramRejectsDuplicates() {
        newDiagram(DiagramType.CLASS);
        ClassNode a = node(new ClassNode(), 0, 0);
        ClassNode b = node(new ClassNode(), 300, 0);
        assertValid(connect(new DependencyEdge(), a, b));
        assertError(connect(new DependencyEdge(), a, b));
        assertEquals(1, diagram.getEdges().size());
    }

    @Test
    void classDiagramRejectsDirectCycles() {
        newDiagram(DiagramType.CLASS);
        ClassNode a = node(new ClassNode(), 0, 0);
        ClassNode b = node(new ClassNode(), 300, 0);
        assertValid(connect(new GeneralizationEdge(GeneralizationEdge.Type.INHERITANCE), a, b));
        assertError(connect(new GeneralizationEdge(GeneralizationEdge.Type.INHERITANCE), b, a));
        assertValid(connect(new DependencyEdge(), a, b));
        assertError(connect(new DependencyEdge(), b, a));
        assertValid(connect(new AssociationEdge(), a, b));
        assertError(connect(new AssociationEdge(), b, a));

        ClassNode c = node(new ClassNode(), 0, 300);
        ClassNode d = node(new ClassNode(), 300, 300);
        assertValid(connect(new AggregationEdge(AggregationEdge.Type.AGGREGATION), c, d));
        assertError(connect(new AggregationEdge(AggregationEdge.Type.COMPOSITION), d, c));
    }

    @Test
    void classDiagramRejectsAssociationTogetherWithAggregation() {
        newDiagram(DiagramType.CLASS);
        ClassNode a = node(new ClassNode(), 0, 0);
        ClassNode b = node(new ClassNode(), 300, 0);
        assertValid(connect(new AggregationEdge(AggregationEdge.Type.AGGREGATION), a, b));
        assertError(connect(new AssociationEdge(), b, a));
        assertError(connect(new AssociationEdge(), a, b));
    }

    @Test
    void classDiagramSelfEdges() {
        newDiagram(DiagramType.CLASS);
        ClassNode a = node(new ClassNode(), 0, 0);
        Point inside = new Point(20, 20);
        Point alsoInside = new Point(70, 40);
        assertError(connect(new GeneralizationEdge(GeneralizationEdge.Type.INHERITANCE), inside, alsoInside));
        assertError(connect(new GeneralizationEdge(GeneralizationEdge.Type.IMPLEMENTATION), inside, alsoInside));
        assertError(connect(new DependencyEdge(), inside, alsoInside));
        assertValid(connect(new AssociationEdge(), inside, alsoInside));
        assertSame(a, diagram.getEdges().get(0).getEnd());
    }

    @Test
    void useCaseDiagramRejectsSelfEdgesAndDuplicates() {
        newDiagram(DiagramType.USE_CASE);
        ActorNode actor = node(new ActorNode(), 0, 0);
        UseCaseNode useCase = node(new UseCaseNode(), 300, 0);
        UseCaseNode other = node(new UseCaseNode(), 300, 300);
        Point inActor = new Point(20, 20);
        Point alsoInActor = new Point(60, 40);
        assertError(connect(new UseCaseAssociationEdge(), inActor, alsoInActor));
        assertError(connect(new UseCaseGeneralizationEdge(), inActor, alsoInActor));
        assertError(connect(new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.INCLUDE), center(useCase),
                new Point(310, 10)));

        assertValid(connect(new UseCaseAssociationEdge(), actor, useCase));
        assertError(connect(new UseCaseAssociationEdge(), actor, useCase));
        assertValid(connect(new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.EXTEND), useCase, other));
        assertError(connect(new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.INCLUDE), actor, useCase));
    }

    @Test
    void objectDiagramCollaborations() {
        newDiagram(DiagramType.OBJECT);
        ObjectNode a = node(new ObjectNode(), 0, 0);
        ObjectNode b = node(new ObjectNode(), 300, 0);
        assertError(connect(new ObjectCollaborationEdge(), new Point(10, 10), new Point(50, 20)));
        assertValid(connect(new ObjectCollaborationEdge(), a, b));
        assertError(connect(new ObjectCollaborationEdge(), a, b));
        assertError(connect(new ObjectCollaborationEdge(), b, a));
    }

    @Test
    void referencesStartAtTheValueOfAField() {
        newDiagram(DiagramType.OBJECT);
        ObjectNode a = node(new ObjectNode(), 0, 0);
        ObjectNode b = node(new ObjectNode(), 300, 0);
        FieldNode field = new FieldNode();
        a.addChild(field);

        assertError(connect(new ObjectReferenceEdge(), new Point(10, 35), center(b)));
        ObjectReferenceEdge reference = new ObjectReferenceEdge();
        assertValid(connect(reference, new Point(80, 35), center(b)));
        assertSame(field, reference.getStart());
        assertSame(b, reference.getEnd());
        assertValid(connect(new ObjectReferenceEdge(), new Point(80, 35), new Point(10, 10)));
    }

    @Test
    void stateDiagramLimits() {
        newDiagram(DiagramType.STATE);
        StateNode a = node(new StateNode(), 0, 0);
        StateNode b = node(new StateNode(), 300, 0);
        InitialStateNode initial = node(new InitialStateNode(), 0, 300);
        FinalStateNode end = node(new FinalStateNode(), 300, 300);

        Point inA = new Point(20, 20);
        Point alsoInA = new Point(70, 40);
        assertValid(connect(new StateTransitionEdge(), inA, alsoInA));
        assertValid(connect(new StateTransitionEdge(), inA, alsoInA));
        assertError(connect(new StateTransitionEdge(), inA, alsoInA));

        assertValid(connect(new StateTransitionEdge(), a, b));
        assertValid(connect(new StateTransitionEdge(), a, b));
        assertError(connect(new StateTransitionEdge(), a, b));
        assertValid(connect(new StateTransitionEdge(), b, a));
        assertValid(connect(new StateTransitionEdge(), b, a));
        assertError(connect(new StateTransitionEdge(), b, a));

        assertError(connect(new StateTransitionEdge(), end, a));
        assertError(connect(new StateTransitionEdge(), a, initial));
        assertValid(connect(new StateTransitionEdge(), initial, a));
        assertValid(connect(new StateTransitionEdge(), a, end));
    }

    @Test
    void noteConnectorsLinkElementToNoteOrNoteToPoint() {
        newDiagram(DiagramType.CLASS);
        ClassNode a = node(new ClassNode(), 0, 0);
        NoteNode note = node(new NoteNode(), 300, 0);
        assertValid(connect(new NoteEdge(), a, note));
        assertError(connect(new NoteEdge(), a, note));
        assertError(connect(new NoteEdge(), note, a));
        assertEquals(ValidationResult.SILENT_FAILURE, connect(new NoteEdge(), center(a), new Point(900, 900)));

        NoteEdge anchored = new NoteEdge();
        assertValid(connect(anchored, center(note), new Point(904, 897)));
        PointNode point = (PointNode) anchored.getEnd();
        assertEquals(900, point.getX());
        assertEquals(900, point.getY());
        assertTrue(diagram.getRootNodes().contains(point));
    }

    @Test
    void removingNodeRemovesAttachedEdgesAndFixedPoints() {
        newDiagram(DiagramType.CLASS);
        ClassNode a = node(new ClassNode(), 0, 0);
        ClassNode b = node(new ClassNode(), 300, 0);
        NoteNode note = node(new NoteNode(), 0, 300);
        connect(new DependencyEdge(), a, b);
        connect(new NoteEdge(), center(note), new Point(900, 900));

        var removal = builder.removeElements(List.of(a, note)).orElseThrow();
        removal.execute();
        assertTrue(diagram.getEdges().isEmpty());
        assertEquals(List.of(b), diagram.getRootNodes());

        removal.undo();
        assertEquals(2, diagram.getEdges().size());
        assertEquals(4, diagram.getRootNodes().size());
    }
}
