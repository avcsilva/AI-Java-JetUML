package br.uefs.umleditor.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.nodes.ClassNode;

class UndoManagerTest {

    @Test
    void undoesAndRedoesOperationsInOrder() {
        Diagram diagram = new Diagram(DiagramType.CLASS, 500, 500);
        ClassNode node = new ClassNode();
        UndoManager manager = new UndoManager();

        manager.execute(new AddNodeOperation(diagram, node, null, 0));
        manager.execute(new MoveNodesOperation(List.of(node), 10, 20));
        assertEquals(10, node.getX());

        manager.undo();
        assertEquals(0, node.getX());
        manager.undo();
        assertTrue(diagram.getRootNodes().isEmpty());
        assertFalse(manager.canUndo());

        manager.redo();
        manager.redo();
        assertEquals(List.of(node), diagram.getRootNodes());
        assertEquals(20, node.getY());
    }

    @Test
    void newOperationClearsRedoHistory() {
        Diagram diagram = new Diagram(DiagramType.CLASS, 500, 500);
        UndoManager manager = new UndoManager();
        manager.execute(new AddNodeOperation(diagram, new ClassNode(), null, 0));
        manager.undo();
        manager.record(new MoveNodesOperation(List.of(), 1, 1));
        assertFalse(manager.canRedo());
    }
}
