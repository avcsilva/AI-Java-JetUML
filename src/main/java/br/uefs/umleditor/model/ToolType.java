package br.uefs.umleditor.model;

/**
 * Every tool that can appear in the tool bar of some diagram type (RF07).
 * Each tool has the name shown in tooltips and tool hints, and an extended
 * description of the corresponding UML element used by verbose tooltips (RF10).
 */
public enum ToolType {
    SELECT("Select", "Selects, moves and edits the elements of the diagram."),
    CLASS("Class", "A class describes a set of objects that share the same attributes, operations and relationships."),
    INTERFACE("Interface", "An interface declares a set of operations that realizing classes must provide."),
    PACKAGE("Package", "A package groups related model elements and can contain other elements as children."),
    PACKAGE_DESCRIPTION("Package Description", "A package shown with a textual description of its contents instead of child elements."),
    NOTE("Note", "A note holds a free-text comment that adds complementary information to the diagram."),
    DEPENDENCY("Dependency", "A dependency indicates that one element uses or relies on another element."),
    INHERITANCE("Inheritance", "A generalization in which a subclass inherits the structure and behavior of a superclass."),
    REALIZATION("Realization", "A realization indicates that a class implements the operations declared by an interface."),
    CLASS_ASSOCIATION("Association", "An association is a structural relationship between instances of two classes."),
    AGGREGATION("Aggregation", "An aggregation is a whole-part association in which the parts can exist without the whole."),
    COMPOSITION("Composition", "A composition is a whole-part association in which the parts belong exclusively to the whole."),
    NOTE_CONNECTOR("Note Connector", "A note connector links a note to the element or point it comments on."),
    TO_CLIPBOARD("To Clipboard", "Copies an image of the whole diagram to the clipboard."),
    IMPLICIT_PARAMETER("Implicit Parameter", "An implicit parameter is an object that takes part in the interaction, with its lifeline."),
    CALL("Call", "A call is a message sent from a caller activation to a callee, creating an activation box."),
    RETURN("Return", "A return indicates that control goes back from a callee activation to its caller."),
    STATE("State", "A state is a condition in the life of an object during which it satisfies some condition or waits for an event."),
    INITIAL_STATE("Initial State", "The initial state is the pseudostate where the state machine starts."),
    FINAL_STATE("Final State", "The final state indicates that the state machine has completed."),
    TRANSITION("Transition", "A transition is a change from a source state to a target state, triggered by an event."),
    OBJECT("Object", "An object is an instance of a class, shown with its name and its fields."),
    FIELD("Field", "A field is a named slot of an object that holds a value or a reference."),
    REFERENCE("Reference", "A reference links the value of a field to the object it refers to."),
    COLLABORATION("Collaboration", "A collaboration is a link between two objects that work together."),
    ACTOR("Actor", "An actor is a role played by a user or external system that interacts with the system."),
    USE_CASE("Use Case", "A use case is a unit of functionality that the system provides to its actors."),
    USE_CASE_ASSOCIATION("Association", "An association indicates that an actor participates in a use case."),
    INCLUDE("Include", "An include dependency indicates that a use case always incorporates the behavior of another use case."),
    EXTEND("Extend", "An extend dependency indicates that a use case may optionally extend the behavior of another use case."),
    GENERALIZATION("Generalization", "A generalization indicates that an actor or use case is a specialization of another one.");

    private final String displayName;
    private final String description;

    ToolType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Tools that act immediately when chosen instead of becoming the active tool.
     */
    public boolean isAction() {
        return this == TO_CLIPBOARD;
    }
}
