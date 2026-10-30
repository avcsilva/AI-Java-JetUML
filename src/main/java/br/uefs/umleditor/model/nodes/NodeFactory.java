package br.uefs.umleditor.model.nodes;

import java.util.Optional;

import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ToolType;

/**
 * Creates the node corresponding to a node creation tool (RF11).
 */
public final class NodeFactory {

    private NodeFactory() {
    }

    /**
     * @return a new node for the tool, or empty if the tool does not create nodes
     */
    public static Optional<Node> create(ToolType tool) {
        Node node = switch (tool) {
            case CLASS -> new ClassNode();
            case INTERFACE -> new InterfaceNode();
            case PACKAGE -> new PackageNode();
            case PACKAGE_DESCRIPTION -> new PackageDescriptionNode();
            case NOTE -> new NoteNode();
            case IMPLICIT_PARAMETER -> new ImplicitParameterNode();
            case STATE -> new StateNode();
            case INITIAL_STATE -> new InitialStateNode();
            case FINAL_STATE -> new FinalStateNode();
            case OBJECT -> new ObjectNode();
            case FIELD -> new FieldNode();
            case ACTOR -> new ActorNode();
            case USE_CASE -> new UseCaseNode();
            default -> null;
        };
        return Optional.ofNullable(node);
    }
}
