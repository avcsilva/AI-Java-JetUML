package br.uefs.umleditor.model.rules;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.StateTransitionEdge;
import br.uefs.umleditor.model.nodes.FinalStateNode;
import br.uefs.umleditor.model.nodes.InitialStateNode;
import br.uefs.umleditor.model.nodes.StateNode;

/**
 * State diagram rules (RF19): each node accepts up to two self-transitions,
 * two nodes accept up to two transitions in each direction, no transition
 * leaves a final state and none arrives at an initial state. Unlike the other
 * diagrams, repeated transitions are allowed within these limits (RF18).
 */
public class StateDiagramRules extends DiagramRules {

    public static final int MAX_TRANSITIONS_PER_DIRECTION = 2;

    @Override
    protected ValidationResult checkEdge(Diagram diagram, Edge edge, Node start, Node end) {
        if (!isAny(start, StateNode.class, InitialStateNode.class, FinalStateNode.class)
                || !isAny(end, StateNode.class, InitialStateNode.class, FinalStateNode.class)) {
            return invalidEnds("transition", "states");
        }
        if (start instanceof FinalStateNode) {
            return ValidationResult.error("A transition cannot start at a final state");
        }
        if (end instanceof InitialStateNode) {
            return ValidationResult.error("A transition cannot arrive at an initial state");
        }
        if (count(diagram, StateTransitionEdge.class, start, end) >= MAX_TRANSITIONS_PER_DIRECTION) {
            return ValidationResult.error(start == end
                    ? "A state accepts at most " + MAX_TRANSITIONS_PER_DIRECTION + " self-transitions"
                    : "Two states accept at most " + MAX_TRANSITIONS_PER_DIRECTION + " transitions in each direction");
        }
        return ValidationResult.VALID;
    }
}
