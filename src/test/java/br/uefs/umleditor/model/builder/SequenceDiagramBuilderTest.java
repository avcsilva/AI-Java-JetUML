package br.uefs.umleditor.model.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.layout.SequenceLayout;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.CallEdge;
import br.uefs.umleditor.model.edges.ReturnEdge;
import br.uefs.umleditor.model.nodes.CallNode;
import br.uefs.umleditor.model.nodes.ImplicitParameterNode;
import br.uefs.umleditor.model.rules.ValidationResult;

/**
 * Calls and returns of sequence diagrams (RF27-RF31).
 */
class SequenceDiagramBuilderTest {

    private static final double BOX_HEIGHT = 30;
    private static final double WIDTH = 80;

    /** Lifelines are 80 wide with a 30-high box and reach 400 pixels down. */
    private static final BoundsCalculator BOUNDS = new BoundsCalculator() {
        @Override
        public Rectangle getBounds(Node node) {
            if (node instanceof CallNode call) {
                return new Rectangle(call.getX(), call.getY(), SequenceLayout.CALL_WIDTH, call.getHeight());
            }
            return new Rectangle(node.getX(), node.getY(), WIDTH, 400);
        }

        @Override
        public Rectangle getConnectionBounds(Node node) {
            return node instanceof ImplicitParameterNode
                    ? new Rectangle(node.getX(), node.getY(), WIDTH, BOX_HEIGHT) : getBounds(node);
        }
    };

    private Diagram diagram;
    private SequenceDiagramBuilder builder;
    private ImplicitParameterNode a;
    private ImplicitParameterNode b;
    private ImplicitParameterNode c;

    @BeforeEach
    void setUp() {
        diagram = new Diagram(DiagramType.SEQUENCE, 2000, 2000);
        builder = new SequenceDiagramBuilder(diagram, BOUNDS);
        a = lifeline(0);
        b = lifeline(200);
        c = lifeline(400);
    }

    private ImplicitParameterNode lifeline(double x) {
        ImplicitParameterNode node = new ImplicitParameterNode();
        node.setPosition(x, 0);
        diagram.addRootNode(node);
        return node;
    }

    private void layout() {
        new SequenceLayout(diagram, node -> BOX_HEIGHT, node -> node.getX() + WIDTH / 2).apply();
    }

    private ValidationResult drag(Edge edge, Point start, Point end) {
        DiagramBuilder.EdgeCreation creation = builder.createEdge(edge, start, end);
        if (creation.validation().isValid()) {
            creation.operation().execute();
            layout();
        }
        return creation.validation();
    }

    private static Point onLifeline(ImplicitParameterNode node, double y) {
        return new Point(node.getX() + WIDTH / 2, y);
    }

    private static Point inside(CallNode node, double dy) {
        return new Point(node.getX() + SequenceLayout.CALL_WIDTH / 2, node.getY() + dy);
    }

    private CallNode activation(ImplicitParameterNode lifeline, int index) {
        return (CallNode) lifeline.getChildren().get(index);
    }

    private CallEdge firstCall() {
        CallEdge call = new CallEdge();
        assertTrue(drag(call, onLifeline(a, 100), onLifeline(b, 120)).isValid());
        return call;
    }

    @Test
    void firstCallCreatesActivationsOfCallerAndCallee() {
        CallEdge call = firstCall();
        CallNode caller = activation(a, 0);
        CallNode callee = activation(b, 0);
        assertSame(caller, call.getStart());
        assertSame(callee, call.getEnd());
        assertEquals(100, caller.getY());
        assertTrue(callee.getY() > caller.getY());
        assertTrue(caller.getY() + caller.getHeight() > callee.getY() + callee.getHeight());
    }

    @Test
    void secondCallerWithoutActivationFailsSilently() {
        firstCall();
        assertEquals(ValidationResult.SILENT_FAILURE, drag(new CallEdge(), onLifeline(c, 300), onLifeline(b, 300)));
        assertEquals(1, diagram.getEdges().size());
    }

    @Test
    void additionalCallsStartAtAnActivationAndAreOrderedByHeight() {
        firstCall();
        CallNode caller = activation(a, 0);
        CallEdge later = new CallEdge();
        assertTrue(drag(later, inside(caller, caller.getHeight() - 2), onLifeline(c, 300)).isValid());
        CallEdge earlier = new CallEdge();
        assertTrue(drag(earlier, inside(caller, 1), onLifeline(c, 300)).isValid());
        assertTrue(earlier.getEnd().getY() < activation(b, 0).getY());
        assertTrue(later.getEnd().getY() > activation(b, 0).getY());
    }

    @Test
    void dragInsideActivationCreatesSelfCall() {
        firstCall();
        CallNode caller = activation(a, 0);
        CallEdge self = new CallEdge();
        assertTrue(drag(self, inside(caller, 5), inside(caller, 8)).isValid());
        CallNode nested = (CallNode) self.getEnd();
        assertSame(a, nested.getParent());
        assertTrue(nested.getX() > caller.getX());
    }

    @Test
    void dropOnOwnLifelineOutsideActivationFailsSilently() {
        firstCall();
        CallNode caller = activation(a, 0);
        assertEquals(ValidationResult.SILENT_FAILURE, drag(new CallEdge(), inside(caller, 5), onLifeline(a, 350)));
    }

    @Test
    void callToBoxOfImplicitParameterIsConstructorCall() {
        firstCall();
        CallNode caller = activation(a, 0);
        CallEdge create = new CallEdge();
        assertTrue(drag(create, inside(caller, caller.getHeight() - 2), new Point(c.getX() + 10, 10)).isValid());
        assertTrue(create.isConstructorCall());
        assertEquals(CallEdge.CREATE_LABEL, create.getMiddleLabel());
        CallNode callee = (CallNode) create.getEnd();
        assertSame(c, callee.getParent());
        assertEquals(c.getY() + BOX_HEIGHT, callee.getY(), 0.001);
        assertTrue(c.getY() > 0);

        CallEdge again = new CallEdge();
        assertEquals(ValidationResult.SILENT_FAILURE, drag(again, inside(caller, 1), new Point(c.getX() + 10,
                c.getY() + 10)));
    }

    @Test
    void returnGoesBackToTheCallerOnce() {
        CallEdge call = firstCall();
        CallNode callee = (CallNode) call.getEnd();
        ReturnEdge back = new ReturnEdge();
        assertTrue(drag(back, inside(callee, 5), onLifeline(a, 400)).isValid());
        assertSame(call.getStart(), back.getEnd());
        assertEquals(ValidationResult.SILENT_FAILURE, drag(new ReturnEdge(), inside(callee, 5), onLifeline(a, 400)));
        assertEquals(ValidationResult.SILENT_FAILURE,
                drag(new ReturnEdge(), inside((CallNode) call.getStart(), 5), onLifeline(b, 400)));
    }

    @Test
    void callsAndReturnsOnlyConnectActivations() {
        assertEquals(ValidationResult.SILENT_FAILURE, drag(new ReturnEdge(), onLifeline(a, 100), onLifeline(b, 100)));
        assertEquals(ValidationResult.SILENT_FAILURE, drag(new CallEdge(), onLifeline(a, 100), new Point(1000, 100)));
        assertTrue(diagram.getEdges().isEmpty());
    }

    @Test
    void deletingCallRemovesTheActivationsThatDependOnIt() {
        CallEdge call = firstCall();
        CallNode callee = (CallNode) call.getEnd();
        drag(new CallEdge(), inside(callee, 5), onLifeline(c, 200));
        drag(new ReturnEdge(), inside(callee, 5), onLifeline(a, 400));
        assertEquals(3, diagram.getEdges().size());

        var removal = builder.removeElements(List.of(call)).orElseThrow();
        removal.execute();
        assertTrue(diagram.getEdges().isEmpty());
        assertTrue(b.getChildren().isEmpty());
        assertTrue(c.getChildren().isEmpty());
        assertFalse(a.getChildren().isEmpty());

        removal.undo();
        assertEquals(3, diagram.getEdges().size());
    }

    @Test
    void openBottomIsAPropertyOfTheActivation() {
        CallNode node = new CallNode();
        node.getProperties().get(0).set(true);
        assertTrue(node.isOpenBottom());
        assertEquals("Open Bottom", node.getProperties().get(0).getName());
    }
}
