package br.uefs.umleditor.model.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramElement;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.ElementCompatibility;
import br.uefs.umleditor.model.InvalidDiagramException;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.DependencyEdge;
import br.uefs.umleditor.model.edges.NoteEdge;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.ObjectNode;
import br.uefs.umleditor.model.nodes.PointNode;
import br.uefs.umleditor.model.nodes.StateNode;
import br.uefs.umleditor.persistence.DiagramContent;
import br.uefs.umleditor.persistence.DiagramDeserializer;
import br.uefs.umleditor.persistence.DiagramSerializer;

/**
 * Cut, copy and paste of elements (RF34).
 */
class CopyPasteTest {

    private static final BoundsCalculator BOUNDS = node -> new Rectangle(node.getX(), node.getY(), 100, 60);

    private static DiagramContent copy(Diagram diagram, DiagramBuilder builder, List<Node> selected,
                                       List<Edge> selectedEdges)
            throws InvalidDiagramException {
        List<DiagramElement> selection = new ArrayList<>(selected);
        selection.addAll(selectedEdges);
        DiagramBuilder.Fragment fragment = builder.copyFragment(selection);
        return DiagramDeserializer.read(DiagramSerializer.toJson(diagram.getType(), fragment.roots(),
                fragment.edges()));
    }

    @Test
    void copiesSelectedNodesWithTheEdgesBetweenThem() throws InvalidDiagramException {
        Diagram diagram = new Diagram(DiagramType.CLASS, 1000, 1000);
        DiagramBuilder builder = new DiagramBuilder(diagram, BOUNDS);
        ClassNode a = new ClassNode();
        ClassNode b = new ClassNode();
        ClassNode c = new ClassNode();
        b.setPosition(200, 0);
        c.setPosition(400, 0);
        diagram.addRootNode(a);
        diagram.addRootNode(b);
        diagram.addRootNode(c);
        DependencyEdge ab = new DependencyEdge();
        ab.connect(a, b);
        DependencyEdge bc = new DependencyEdge();
        bc.connect(b, c);
        diagram.addEdge(ab);
        diagram.addEdge(bc);

        DiagramContent content = copy(diagram, builder, List.of(a, b), List.of());
        assertEquals(2, content.roots().size());
        assertEquals(1, content.edges().size());

        DiagramBuilder.Paste paste = builder.paste(content.roots(), content.edges(), 10);
        paste.operation().execute();
        assertEquals(5, diagram.getRootNodes().size());
        assertEquals(3, diagram.getEdges().size());
        assertEquals(3, paste.elements().size());
        assertEquals(10, diagram.getRootNodes().get(3).getX());
        assertNotSame(a, diagram.getRootNodes().get(3));

        paste.operation().undo();
        assertEquals(3, diagram.getRootNodes().size());
    }

    @Test
    void fieldAloneIsNotCopiedButObjectCarriesItsFields() throws InvalidDiagramException {
        Diagram diagram = new Diagram(DiagramType.OBJECT, 1000, 1000);
        DiagramBuilder builder = new DiagramBuilder(diagram, BOUNDS);
        ObjectNode object = new ObjectNode();
        FieldNode field = new FieldNode();
        object.addChild(field);
        diagram.addRootNode(object);
        assertTrue(builder.copyFragment(List.of(field)).isEmpty());
        DiagramContent content = copy(diagram, builder, List.of(object), List.of());
        assertEquals(2, content.allNodes().size());
    }

    @Test
    void noteConnectorToPointCarriesThePoint() throws InvalidDiagramException {
        Diagram diagram = new Diagram(DiagramType.STATE, 1000, 1000);
        DiagramBuilder builder = new DiagramBuilder(diagram, BOUNDS);
        NoteNode note = new NoteNode();
        PointNode point = new PointNode();
        point.setPosition(300, 300);
        diagram.addRootNode(note);
        diagram.addRootNode(point);
        NoteEdge edge = new NoteEdge();
        edge.connect(note, point);
        diagram.addEdge(edge);
        DiagramContent content = copy(diagram, builder, List.of(note), List.of(edge));
        assertEquals(2, content.roots().size());
        assertEquals(1, content.edges().size());
    }

    @Test
    void compatibilityDependsOnTheDiagramType() {
        assertTrue(ElementCompatibility.accepts(DiagramType.CLASS, new ClassNode()));
        assertTrue(ElementCompatibility.accepts(DiagramType.USE_CASE, new NoteNode()));
        assertTrue(ElementCompatibility.accepts(DiagramType.OBJECT, new FieldNode()));
        assertFalse(ElementCompatibility.accepts(DiagramType.CLASS, new StateNode()));
        assertFalse(ElementCompatibility.accepts(DiagramType.STATE, new DependencyEdge()));
        assertTrue(ElementCompatibility.accepts(DiagramType.STATE, new NoteEdge()));
    }
}
