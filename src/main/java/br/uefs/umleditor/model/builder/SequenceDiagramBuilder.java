package br.uefs.umleditor.model.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import br.uefs.umleditor.command.AddEdgeOperation;
import br.uefs.umleditor.command.AddNodeOperation;
import br.uefs.umleditor.command.CompoundOperation;
import br.uefs.umleditor.command.DiagramOperation;
import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.layout.GridSnap;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramElement;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.CallEdge;
import br.uefs.umleditor.model.edges.ReturnEdge;
import br.uefs.umleditor.model.nodes.CallNode;
import br.uefs.umleditor.model.nodes.ImplicitParameterNode;
import br.uefs.umleditor.model.rules.SequenceDiagramRules;
import br.uefs.umleditor.model.rules.ValidationResult;

/**
 * Builder of sequence diagrams. A call dragged from a lifeline or activation
 * box to another lifeline creates the activation box of the callee (RF27); a
 * call dragged inside an activation box is a self-call (RF28); a call dropped
 * on the box of an implicit parameter without activations is a constructor
 * call (RF29). Attempts that would break the flow of control fail silently
 * (RF30).
 */
public class SequenceDiagramBuilder extends DiagramBuilder {

    public SequenceDiagramBuilder(Diagram diagram, BoundsCalculator bounds) {
        super(diagram, bounds);
    }

    @Override
    public EdgeCreation createEdge(Edge edge, Point startPoint, Point endPoint) {
        if (!(edge instanceof CallEdge) && !(edge instanceof ReturnEdge)) {
            return super.createEdge(edge, startPoint, endPoint);
        }
        Optional<Node> start = findNodeAt(startPoint);
        Optional<Node> end = findNodeAt(endPoint);
        if (start.isEmpty() || end.isEmpty()) {
            return silentFailure();
        }
        return edge instanceof CallEdge call
                ? createCall(call, start.get(), startPoint, end.get(), endPoint)
                : createReturn(edge, start.get(), end.get());
    }

    private static EdgeCreation silentFailure() {
        return EdgeCreation.rejected(ValidationResult.SILENT_FAILURE);
    }

    private EdgeCreation createCall(CallEdge call, Node start, Point startPoint, Node end, Point endPoint) {
        List<DiagramOperation> operations = new ArrayList<>();
        CallNode caller;
        if (start instanceof CallNode activation) {
            caller = activation;
        } else if (start instanceof ImplicitParameterNode lifeline && !hasActivations()) {
            // The first call of the diagram starts on a lifeline and creates its activation.
            caller = new CallNode();
            caller.setPosition(lifeline.getX(), GridSnap.snap(startPoint.y()));
            operations.add(new AddNodeOperation(getDiagram(), caller, lifeline, lifeline.getChildren().size()));
        } else {
            return silentFailure();
        }
        ImplicitParameterNode callerLifeline = start instanceof CallNode
                ? (ImplicitParameterNode) caller.getParent() : (ImplicitParameterNode) start;

        ImplicitParameterNode calleeLifeline;
        boolean constructor = false;
        if (end == caller) {
            calleeLifeline = callerLifeline;
        } else {
            calleeLifeline = lifelineOf(end);
            if (calleeLifeline == null || calleeLifeline == callerLifeline) {
                return silentFailure();
            }
            if (end == calleeLifeline && bounds().getConnectionBounds(calleeLifeline).contains(endPoint)) {
                if (!calleeLifeline.getChildren().isEmpty()) {
                    return silentFailure();
                }
                constructor = true;
            }
        }

        CallNode callee = new CallNode();
        callee.setPosition(calleeLifeline.getX(), endPoint.y());
        ValidationResult validation = rules().check(getDiagram(), call, caller, callee);
        if (!validation.isValid()) {
            return EdgeCreation.rejected(validation);
        }
        operations.add(new AddNodeOperation(getDiagram(), callee, calleeLifeline,
                calleeLifeline.getChildren().size()));
        call.connect(caller, callee);
        call.setConstructorCall(constructor);
        if (constructor) {
            call.setMiddleLabel(CallEdge.CREATE_LABEL);
        }
        operations.add(new AddEdgeOperation(getDiagram(), call, insertionIndex(caller, startPoint.y())));
        return new EdgeCreation(validation, new CompoundOperation(operations));
    }

    private boolean hasActivations() {
        return getDiagram().getAllNodes().stream().anyMatch(CallNode.class::isInstance);
    }

    private static ImplicitParameterNode lifelineOf(Node node) {
        if (node instanceof ImplicitParameterNode lifeline) {
            return lifeline;
        }
        if (node instanceof CallNode && node.getParent() instanceof ImplicitParameterNode lifeline) {
            return lifeline;
        }
        return null;
    }

    /**
     * A new call of an activation goes between its existing calls, according
     * to the height where the drag started.
     *
     * @return the position of the new call in the edge list of the diagram
     */
    private int insertionIndex(CallNode caller, double y) {
        List<Edge> edges = getDiagram().getEdges();
        for (Edge edge : edges) {
            if (edge instanceof CallEdge && edge.getStart() == caller && edge.getEnd().getY() > y) {
                return edges.indexOf(edge);
            }
        }
        return edges.size();
    }

    /**
     * A return goes from an activation back to the activation that called it;
     * it may be dropped anywhere on the lifeline of the caller.
     */
    private EdgeCreation createReturn(Edge edge, Node start, Node end) {
        if (!(start instanceof CallNode callee)) {
            return silentFailure();
        }
        CallNode caller = SequenceDiagramRules.callerOf(getDiagram(), callee);
        if (caller == null) {
            return silentFailure();
        }
        Node target = end;
        if (end != caller && lifelineOf(end) == caller.getParent() && end != callee) {
            target = caller;
        }
        ValidationResult validation = rules().check(getDiagram(), edge, callee, target);
        if (!validation.isValid()) {
            return EdgeCreation.rejected(validation);
        }
        edge.connect(callee, caller);
        return new EdgeCreation(validation, new AddEdgeOperation(getDiagram(), edge));
    }

    /**
     * Removing a call or an activation also removes the calls that depend on
     * it, so that no activation is left without its caller (RF30).
     */
    @Override
    public Optional<DiagramOperation> removeElements(Collection<? extends DiagramElement> selected) {
        Set<DiagramElement> expanded = new LinkedHashSet<>(selected);
        List<CallNode> pending = new ArrayList<>();
        for (DiagramElement element : selected) {
            if (element instanceof CallEdge call) {
                pending.add((CallNode) call.getEnd());
            } else if (element instanceof CallNode activation) {
                pending.add(activation);
            } else if (element instanceof ImplicitParameterNode lifeline) {
                lifeline.getChildren().forEach(child -> pending.add((CallNode) child));
            }
        }
        while (!pending.isEmpty()) {
            CallNode activation = pending.remove(pending.size() - 1);
            expanded.add(activation);
            for (Edge edge : getDiagram().getEdges()) {
                if (edge instanceof CallEdge && edge.getStart() == activation && !expanded.contains(edge.getEnd())) {
                    pending.add((CallNode) edge.getEnd());
                }
            }
        }
        return super.removeElements(expanded);
    }
}
