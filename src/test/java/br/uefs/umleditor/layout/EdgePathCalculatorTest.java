package br.uefs.umleditor.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.edges.AssociationEdge;
import br.uefs.umleditor.model.edges.DependencyEdge;
import br.uefs.umleditor.model.nodes.ClassNode;

class EdgePathCalculatorTest {

    private static final BoundsCalculator BOUNDS = node -> new Rectangle(node.getX(), node.getY(), 100, 60);

    private Diagram diagram;
    private EdgePathCalculator paths;
    private ClassNode left;
    private ClassNode right;

    @BeforeEach
    void setUp() {
        diagram = new Diagram(DiagramType.CLASS, 1000, 1000);
        paths = new EdgePathCalculator(diagram, BOUNDS);
        left = new ClassNode();
        right = new ClassNode();
        right.setPosition(300, 0);
        diagram.addRootNode(left);
        diagram.addRootNode(right);
    }

    @Test
    void edgeGoesFromBorderToBorderAndFollowsNodes() {
        AssociationEdge edge = new AssociationEdge();
        edge.connect(left, right);
        diagram.addEdge(edge);
        assertEquals(List.of(new Point(100, 30), new Point(300, 30)), paths.getPath(edge));

        right.translate(0, 200);
        List<Point> moved = paths.getPath(edge);
        assertTrue(moved.get(moved.size() - 1).y() > 30);
        assertTrue(paths.contains(edge, moved.get(0)));
    }

    @Test
    void parallelEdgesAreSpreadApart() {
        AssociationEdge first = new AssociationEdge();
        first.connect(left, right);
        DependencyEdge second = new DependencyEdge();
        second.connect(right, left);
        diagram.addEdge(first);
        diagram.addEdge(second);
        assertNotEquals(paths.getPath(first).get(0).y(), paths.getPath(second).get(1).y());
    }

    @Test
    void selfEdgeIsLoopOutsideTheNode() {
        AssociationEdge loop = new AssociationEdge();
        loop.connect(left, left);
        diagram.addEdge(loop);
        List<Point> path = paths.getPath(loop);
        assertTrue(path.size() > 2);
        assertTrue(path.stream().anyMatch(point -> !BOUNDS.getBounds(left).contains(point)));
    }

    @Test
    void detectsPointsOnTheEdge() {
        AssociationEdge edge = new AssociationEdge();
        edge.connect(left, right);
        diagram.addEdge(edge);
        assertTrue(paths.contains(edge, new Point(200, 32)));
        assertFalse(paths.contains(edge, new Point(200, 50)));
        assertEquals(new Rectangle(100, 30, 200, 0), paths.getBounds(edge));
    }
}
