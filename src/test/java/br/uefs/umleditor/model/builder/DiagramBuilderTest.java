package br.uefs.umleditor.model.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.uefs.umleditor.command.DiagramOperation;
import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.ObjectNode;
import br.uefs.umleditor.model.nodes.PackageNode;

class DiagramBuilderTest {

    /** Every node is 100 x 60, except packages, which are 300 x 300. */
    private static final BoundsCalculator BOUNDS = node -> node instanceof PackageNode
            ? new Rectangle(node.getX(), node.getY(), 300, 300)
            : new Rectangle(node.getX(), node.getY(), 100, 60);

    private Diagram diagram;
    private DiagramBuilder builder;

    @BeforeEach
    void setUp() {
        diagram = new Diagram(DiagramType.CLASS, 1000, 800);
        builder = new DiagramBuilder(diagram, BOUNDS);
    }

    private Node create(Node node, double x, double y) {
        builder.createNode(node, new Point(x, y)).ifPresent(DiagramOperation::execute);
        return node;
    }

    @Test
    void createsRootNodeOnEmptySpaceSnappedToGrid() {
        ClassNode node = (ClassNode) create(new ClassNode(), 104, 57);
        assertEquals(List.of(node), diagram.getRootNodes());
        assertEquals(100, node.getX());
        assertEquals(60, node.getY());
    }

    @Test
    void keepsNewNodeInsideDiagram() {
        ClassNode node = (ClassNode) create(new ClassNode(), 990, 790);
        assertEquals(900, node.getX());
        assertEquals(740, node.getY());
    }

    @Test
    void clickInsidePackageCreatesChild() {
        PackageNode pkg = (PackageNode) create(new PackageNode(), 0, 0);
        ClassNode child = (ClassNode) create(new ClassNode(), 50, 50);
        assertEquals(List.of(pkg), diagram.getRootNodes());
        assertSame(pkg, child.getParent());
    }

    @Test
    void clickOnNodeThatCannotContainNewNodeHasNoEffect() {
        create(new ClassNode(), 0, 0);
        assertTrue(builder.createNode(new ClassNode(), new Point(20, 20)).isEmpty());
        create(new PackageNode(), 400, 0);
        assertTrue(builder.createNode(new NoteNode(), new Point(450, 50)).isEmpty());
    }

    @Test
    void fieldIsOnlyCreatedInsideObject() {
        Diagram objects = new Diagram(DiagramType.OBJECT, 1000, 800);
        DiagramBuilder objectBuilder = new DiagramBuilder(objects, BOUNDS);
        assertTrue(objectBuilder.createNode(new FieldNode(), new Point(10, 10)).isEmpty());

        ObjectNode object = new ObjectNode();
        objectBuilder.createNode(object, new Point(0, 0)).orElseThrow().execute();
        FieldNode field = new FieldNode();
        objectBuilder.createNode(field, new Point(20, 20)).orElseThrow().execute();
        assertEquals(List.of(field), object.getChildren());
        assertEquals(List.of(object), objects.getRootNodes());
    }

    @Test
    void removesSelectedNodesAndUndoRestoresThem() {
        PackageNode pkg = (PackageNode) create(new PackageNode(), 0, 0);
        ClassNode child = (ClassNode) create(new ClassNode(), 50, 50);
        ClassNode other = (ClassNode) create(new ClassNode(), 500, 500);

        DiagramOperation removal = builder.removeNodes(List.of(child, pkg)).orElseThrow();
        removal.execute();
        assertEquals(List.of(other), diagram.getRootNodes());

        removal.undo();
        assertEquals(List.of(pkg, other), diagram.getRootNodes());
        assertSame(pkg, child.getParent());
    }

    @Test
    void shiftAttachesNodeOverPackageAndDetachesChild() {
        PackageNode pkg = (PackageNode) create(new PackageNode(), 0, 0);
        ClassNode node = (ClassNode) create(new ClassNode(), 500, 500);
        node.translate(-400, -400);

        builder.toggleParent(List.of(node)).orElseThrow().execute();
        assertSame(pkg, node.getParent());
        assertEquals(100, node.getX());

        builder.toggleParent(List.of(node)).orElseThrow().execute();
        assertEquals(null, node.getParent());
        assertEquals(List.of(pkg, node), diagram.getRootNodes());
        assertEquals(100, node.getX());
    }

    @Test
    void shiftOnRootNodeOutsidePackageHasNoEffect() {
        ClassNode node = (ClassNode) create(new ClassNode(), 500, 500);
        assertTrue(builder.toggleParent(List.of(node)).isEmpty());
    }

    @Test
    void lassoSelectsNodesEntirelyInside() {
        ClassNode inside = (ClassNode) create(new ClassNode(), 10, 10);
        create(new ClassNode(), 150, 10);
        assertEquals(List.of(inside), builder.findNodesInside(new Rectangle(0, 0, 200, 100)));
    }

    @Test
    void correctionBringsNodesBackInsideDiagram() {
        ClassNode node = (ClassNode) create(new ClassNode(), 10, 10);
        node.translate(-30, 0);
        assertEquals(new Point(20, 0), builder.correctionToKeepInside(List.of(node)));
    }
}
