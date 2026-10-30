package br.uefs.umleditor.model;

import static br.uefs.umleditor.model.ToolType.*;

import java.util.List;

/**
 * The five kinds of diagram supported by the editor (RF01), each with its
 * tool bar contents in the order required by RF07.
 */
public enum DiagramType {
    CLASS("Class Diagram", List.of(SELECT, ToolType.CLASS, INTERFACE, PACKAGE, PACKAGE_DESCRIPTION, NOTE,
            DEPENDENCY, INHERITANCE, REALIZATION, CLASS_ASSOCIATION, AGGREGATION, COMPOSITION,
            NOTE_CONNECTOR, TO_CLIPBOARD)),
    SEQUENCE("Sequence Diagram", List.of(SELECT, IMPLICIT_PARAMETER, NOTE, CALL, RETURN,
            NOTE_CONNECTOR, TO_CLIPBOARD)),
    STATE("State Diagram", List.of(SELECT, ToolType.STATE, INITIAL_STATE, FINAL_STATE, NOTE,
            TRANSITION, NOTE_CONNECTOR, TO_CLIPBOARD)),
    OBJECT("Object Diagram", List.of(SELECT, ToolType.OBJECT, FIELD, NOTE, REFERENCE, COLLABORATION,
            NOTE_CONNECTOR, TO_CLIPBOARD)),
    USE_CASE("Use Case Diagram", List.of(SELECT, ACTOR, ToolType.USE_CASE, NOTE, USE_CASE_ASSOCIATION,
            INCLUDE, EXTEND, GENERALIZATION, NOTE_CONNECTOR, TO_CLIPBOARD));

    private final String displayName;
    private final List<ToolType> tools;

    DiagramType(String displayName, List<ToolType> tools) {
        this.displayName = displayName;
        this.tools = tools;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return the tools of this diagram type, from top to bottom of the tool bar.
     */
    public List<ToolType> getTools() {
        return tools;
    }

    /**
     * Returns the keyboard shortcut of the tool at the given position of the
     * tool bar (RF08): 1-9, then 0, then A, B, C...
     */
    public static char shortcutFor(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Negative tool index: " + index);
        }
        if (index < 9) {
            return (char) ('1' + index);
        }
        if (index == 9) {
            return '0';
        }
        return (char) ('A' + index - 10);
    }

    /**
     * @return the position in the tool bar whose shortcut is the given character, or -1.
     */
    public int toolIndexForShortcut(char key) {
        char upper = Character.toUpperCase(key);
        for (int i = 0; i < tools.size(); i++) {
            if (shortcutFor(i) == upper) {
                return i;
            }
        }
        return -1;
    }
}
