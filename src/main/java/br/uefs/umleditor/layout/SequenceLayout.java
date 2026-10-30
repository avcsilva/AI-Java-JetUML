package br.uefs.umleditor.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.CallEdge;
import br.uefs.umleditor.model.nodes.CallNode;
import br.uefs.umleditor.model.nodes.ImplicitParameterNode;

/**
 * Places the activation boxes of a sequence diagram from the structure of
 * the calls (RF27-RF29): each box is centered on its lifeline, shifted to
 * the right when it is nested in another active box of the same lifeline;
 * the calls of an activation follow each other from top to bottom in the
 * order of the edges; the box of an object created by a constructor call is
 * aligned with that call; each activation is tall enough for its calls.
 */
public class SequenceLayout {

    public static final double CALL_WIDTH = 16;
    private static final double FIRST_CALL_OFFSET = 20;
    private static final double CALL_SPACING = 12;
    private static final double ROOT_MARGIN = 10;

    private final Diagram diagram;
    private final ToDoubleFunction<ImplicitParameterNode> boxHeight;
    private final ToDoubleFunction<ImplicitParameterNode> lifelineCenter;
    private final Map<ImplicitParameterNode, Integer> activeBoxes = new HashMap<>();

    /**
     * @param boxHeight height of the box at the top of a lifeline, which depends on the text
     * @param lifelineCenter horizontal position of the lifeline of an implicit parameter
     */
    public SequenceLayout(Diagram diagram, ToDoubleFunction<ImplicitParameterNode> boxHeight,
                          ToDoubleFunction<ImplicitParameterNode> lifelineCenter) {
        this.diagram = diagram;
        this.boxHeight = boxHeight;
        this.lifelineCenter = lifelineCenter;
    }

    public void apply() {
        activeBoxes.clear();
        for (Node node : diagram.getAllNodes()) {
            if (node instanceof CallNode root && root.getParent() instanceof ImplicitParameterNode lifeline
                    && isRoot(root)) {
                double top = lifeline.getY() + boxHeight.applyAsDouble(lifeline) + ROOT_MARGIN;
                place(root, Math.max(top, root.getY()));
            }
        }
    }

    private boolean isRoot(CallNode node) {
        return diagram.getEdges().stream().noneMatch(edge -> edge instanceof CallEdge && edge.getEnd() == node);
    }

    private List<CallEdge> callsFrom(CallNode caller) {
        return diagram.getEdges().stream()
                .filter(edge -> edge instanceof CallEdge && edge.getStart() == caller)
                .map(CallEdge.class::cast)
                .toList();
    }

    /**
     * Places the activation and, recursively, the activations it calls.
     *
     * @return the bottom of the activation
     */
    private double place(CallNode node, double y) {
        ImplicitParameterNode lifeline = (ImplicitParameterNode) node.getParent();
        int level = activeBoxes.getOrDefault(lifeline, 0);
        double x = lifelineCenter.applyAsDouble(lifeline) - CALL_WIDTH / 2 + level * CALL_WIDTH / 2;
        node.setPosition(x, y);
        activeBoxes.put(lifeline, level + 1);

        double cursor = y + FIRST_CALL_OFFSET;
        for (Edge edge : callsFrom(node)) {
            CallNode callee = (CallNode) edge.getEnd();
            ImplicitParameterNode calleeLifeline = (ImplicitParameterNode) callee.getParent();
            double calleeY = cursor;
            if (((CallEdge) edge).isConstructorCall()) {
                double box = boxHeight.applyAsDouble(calleeLifeline);
                calleeLifeline.setPosition(calleeLifeline.getX(), cursor - box / 2);
                calleeY = cursor + box / 2;
            }
            cursor = place(callee, calleeY) + CALL_SPACING;
        }
        double height = callsFrom(node).isEmpty() ? CallNode.MIN_HEIGHT : Math.max(CallNode.MIN_HEIGHT, cursor - y);
        node.setHeight(height);
        activeBoxes.put(lifeline, level);
        return y + height;
    }
}
