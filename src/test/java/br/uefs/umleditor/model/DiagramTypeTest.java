package br.uefs.umleditor.model;

import static br.uefs.umleditor.model.ToolType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class DiagramTypeTest {

    @Test
    void classDiagramToolsFollowRequiredOrder() {
        assertEquals(List.of(SELECT, ToolType.CLASS, INTERFACE, PACKAGE, PACKAGE_DESCRIPTION, NOTE, DEPENDENCY,
                INHERITANCE, REALIZATION, CLASS_ASSOCIATION, AGGREGATION, COMPOSITION, NOTE_CONNECTOR,
                TO_CLIPBOARD), DiagramType.CLASS.getTools());
    }

    @Test
    void otherDiagramToolsFollowRequiredOrder() {
        assertEquals(List.of(SELECT, IMPLICIT_PARAMETER, NOTE, CALL, RETURN, NOTE_CONNECTOR, TO_CLIPBOARD),
                DiagramType.SEQUENCE.getTools());
        assertEquals(List.of(SELECT, ToolType.STATE, INITIAL_STATE, FINAL_STATE, NOTE, TRANSITION,
                NOTE_CONNECTOR, TO_CLIPBOARD), DiagramType.STATE.getTools());
        assertEquals(List.of(SELECT, ToolType.OBJECT, FIELD, NOTE, REFERENCE, COLLABORATION, NOTE_CONNECTOR,
                TO_CLIPBOARD), DiagramType.OBJECT.getTools());
        assertEquals(List.of(SELECT, ACTOR, ToolType.USE_CASE, NOTE, USE_CASE_ASSOCIATION, INCLUDE, EXTEND,
                GENERALIZATION, NOTE_CONNECTOR, TO_CLIPBOARD), DiagramType.USE_CASE.getTools());
    }

    @Test
    void shortcutsFollowDigitsThenZeroThenLetters() {
        assertEquals('1', DiagramType.shortcutFor(0));
        assertEquals('9', DiagramType.shortcutFor(8));
        assertEquals('0', DiagramType.shortcutFor(9));
        assertEquals('A', DiagramType.shortcutFor(10));
        assertEquals('D', DiagramType.shortcutFor(13));
    }

    @Test
    void shortcutLooksUpToolIndex() {
        assertEquals(0, DiagramType.CLASS.toolIndexForShortcut('1'));
        assertEquals(9, DiagramType.CLASS.toolIndexForShortcut('0'));
        assertEquals(13, DiagramType.CLASS.toolIndexForShortcut('d'));
        assertEquals(-1, DiagramType.SEQUENCE.toolIndexForShortcut('8'));
    }

    @Test
    void diagramSizeIsLimitedToAllowedRange() {
        Diagram diagram = new Diagram(DiagramType.CLASS, 100, 5000);
        assertEquals(Diagram.MIN_SIZE, diagram.getWidth());
        assertEquals(Diagram.MAX_SIZE, diagram.getHeight());
    }
}
