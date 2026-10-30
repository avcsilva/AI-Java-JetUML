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
import br.uefs.umleditor.command.RemoveEdgeOperation;
import br.uefs.umleditor.command.RemoveNodeOperation;
import br.uefs.umleditor.command.ReparentNodeOperation;
import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.layout.EdgePathCalculator;
import br.uefs.umleditor.layout.GridSnap;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramElement;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;
import br.uefs.umleditor.model.edges.NoteEdge;
import br.uefs.umleditor.model.edges.ObjectReferenceEdge;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.PackageNode;
import br.uefs.umleditor.model.nodes.PointNode;
import br.uefs.umleditor.model.rules.DiagramRules;
import br.uefs.umleditor.model.rules.ValidationResult;

/**
 * Translates the user's gestures on a diagram into operations, applying the
 * structural rules of the diagram.
 */
public class DiagramBuilder {

    /**
     * Outcome of an edge creation gesture: the validation result and, when
     * valid, the operation that adds the edge.
     */
    public record EdgeCreation(ValidationResult validation, DiagramOperation operation) {

        static EdgeCreation rejected(ValidationResult validation) {
            return new EdgeCreation(validation, null);
        }
    }

    private final Diagram diagram;
    private final BoundsCalculator bounds;
    private final DiagramRules rules;
    private final EdgePathCalculator paths;

    public DiagramBuilder(Diagram diagram, BoundsCalculator bounds) {
        this.diagram = diagram;
        this.bounds = bounds;
        this.rules = DiagramRules.forType(diagram.getType());
        this.paths = new EdgePathCalculator(diagram, bounds);
    }

    public Diagram getDiagram() {
        return diagram;
    }

    /**
     * @return the topmost node at the point: children are found before their
     *         parents. Point nodes are invisible and never found.
     */
    public Optional<Node> findNodeAt(Point point) {
        List<Node> nodes = diagram.getAllNodes();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            if (!(node instanceof PointNode) && bounds.getBounds(node).contains(point)) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    /**
     * @return the edge or node at the point; edges are drawn over nodes, so they come first
     */
    public Optional<DiagramElement> findElementAt(Point point) {
        List<Edge> edges = diagram.getEdges();
        for (int i = edges.size() - 1; i >= 0; i--) {
            if (paths.contains(edges.get(i), point)) {
                return Optional.of(edges.get(i));
            }
        }
        return findNodeAt(point).map(DiagramElement.class::cast);
    }

    /**
     * @return the elements lying entirely inside the region (lasso selection, RF32)
     */
    public List<DiagramElement> findElementsInside(Rectangle region) {
        List<DiagramElement> result = new ArrayList<>();
        for (Node node : diagram.getAllNodes()) {
            if (!(node instanceof PointNode) && region.contains(bounds.getBounds(node))) {
                result.add(node);
            }
        }
        for (Edge edge : diagram.getEdges()) {
            if (region.contains(paths.getBounds(edge))) {
                result.add(edge);
            }
        }
        return result;
    }

    /**
     * @return every selectable element of the diagram (RF33)
     */
    public List<DiagramElement> getAllElements() {
        List<DiagramElement> result = new ArrayList<>();
        for (Node node : diagram.getAllNodes()) {
            if (!(node instanceof PointNode)) {
                result.add(node);
            }
        }
        result.addAll(diagram.getEdges());
        return result;
    }

    /**
     * Creates a node where the user clicked (RF11). Clicking empty space creates
     * a root node; clicking a node that accepts the new node as a child, such as
     * a package (RF15) or an object receiving a field (RF25), creates a child.
     * Any other click has no effect.
     *
     * @return the operation that adds the node, or empty if the click is not valid
     */
    public Optional<DiagramOperation> createNode(Node node, Point point) {
        Optional<Node> target = findNodeAt(point);
        if (node.requiresParent()) {
            return target.flatMap(clicked -> createRequiredChild(node, clicked));
        }
        ParentNode parent = null;
        if (target.isPresent()) {
            if (target.get() instanceof ParentNode candidate && candidate.canAccept(node)) {
                parent = candidate;
            } else {
                return Optional.empty();
            }
        }
        Point position = GridSnap.snap(point);
        node.setPosition(position.x(), position.y());
        if (parent == null) {
            keepInsideDiagram(node);
            return Optional.of(new AddNodeOperation(diagram, node, null, diagram.getRootNodes().size()));
        }
        return Optional.of(new AddNodeOperation(diagram, node, parent, parent.getChildren().size()));
    }

    /**
     * A node that requires a parent is added to the clicked node, or right
     * after the clicked sibling.
     */
    private Optional<DiagramOperation> createRequiredChild(Node node, Node clicked) {
        if (clicked instanceof ParentNode parent && parent.canAccept(node)) {
            node.setPosition(parent.getX(), parent.getY());
            return Optional.of(new AddNodeOperation(diagram, node, parent, parent.getChildren().size()));
        }
        ParentNode parent = clicked.getParent();
        if (parent != null && parent.canAccept(node)) {
            node.setPosition(parent.getX(), parent.getY());
            int index = parent.getChildren().indexOf(clicked) + 1;
            return Optional.of(new AddNodeOperation(diagram, node, parent, index));
        }
        return Optional.empty();
    }

    private void keepInsideDiagram(Node node) {
        Rectangle area = bounds.getBounds(node);
        double x = Math.max(0, Math.min(node.getX(), GridSnap.snapDown(diagram.getWidth() - area.width())));
        double y = Math.max(0, Math.min(node.getY(), GridSnap.snapDown(diagram.getHeight() - area.height())));
        node.setPosition(x, y);
    }

    /**
     * Creates an edge dragged from one point to another (RF16). The drag must
     * start on a node and end on a node; a drag inside a single node is an
     * attempt to create a self-edge (RF19). A note connector dragged from a note
     * to empty space ends at a new fixed point (RF14). A reference must start at
     * the value part of a field (RF26). The rules of the diagram decide whether
     * the connection is valid (RF18).
     */
    public EdgeCreation createEdge(Edge edge, Point startPoint, Point endPoint) {
        Optional<Node> start = findNodeAt(startPoint);
        if (start.isEmpty()) {
            return EdgeCreation.rejected(ValidationResult.SILENT_FAILURE);
        }
        Node startNode = start.get();
        List<DiagramOperation> operations = new ArrayList<>();
        Node endNode;
        Optional<Node> end = findNodeAt(endPoint);
        if (end.isPresent()) {
            endNode = end.get();
        } else if (edge instanceof NoteEdge && startNode instanceof NoteNode) {
            endNode = createPoint(endPoint);
            operations.add(new AddNodeOperation(diagram, endNode, null, diagram.getRootNodes().size()));
        } else {
            return EdgeCreation.rejected(ValidationResult.SILENT_FAILURE);
        }
        if (edge instanceof ObjectReferenceEdge) {
            if (startNode instanceof FieldNode && !bounds.getConnectionBounds(startNode).contains(startPoint)) {
                return EdgeCreation.rejected(
                        ValidationResult.error("A reference must start at the value part of a field"));
            }
            if (endNode instanceof FieldNode && endNode.getParent() != null) {
                endNode = endNode.getParent();
            }
        }
        ValidationResult validation = rules.check(diagram, edge, startNode, endNode);
        if (!validation.isValid()) {
            return EdgeCreation.rejected(validation);
        }
        edge.connect(startNode, endNode);
        operations.add(new AddEdgeOperation(diagram, edge));
        return new EdgeCreation(validation,
                operations.size() == 1 ? operations.get(0) : new CompoundOperation(operations));
    }

    private PointNode createPoint(Point point) {
        PointNode node = new PointNode();
        Point snapped = GridSnap.snap(point);
        node.setPosition(Math.max(0, Math.min(snapped.x(), diagram.getWidth())),
                Math.max(0, Math.min(snapped.y(), diagram.getHeight())));
        return node;
    }

    /**
     * @return the displacement that brings the trees containing the nodes back
     *         inside the diagram area, or (0, 0) if they are already inside
     */
    public Point correctionToKeepInside(Collection<Node> nodes) {
        Rectangle area = null;
        for (Node node : nodes) {
            Rectangle nodeArea = bounds.getBounds(node.getRoot());
            area = area == null ? nodeArea : area.union(nodeArea);
        }
        if (area == null) {
            return new Point(0, 0);
        }
        return new Point(correction(area.x(), area.maxX(), diagram.getWidth()),
                correction(area.y(), area.maxY(), diagram.getHeight()));
    }

    private static double correction(double min, double max, double limit) {
        if (min < 0) {
            return -min;
        }
        if (max > limit) {
            return Math.max(limit - max, -min);
        }
        return 0;
    }

    private static List<Node> nodesIn(Collection<? extends DiagramElement> elements) {
        return elements.stream().filter(Node.class::isInstance).map(Node.class::cast).toList();
    }

    private static List<Edge> edgesIn(Collection<? extends DiagramElement> elements) {
        return elements.stream().filter(Edge.class::isInstance).map(Edge.class::cast).toList();
    }

    /**
     * @return the selected nodes that are not inside another selected node
     */
    public static List<Node> topmostNodes(Collection<? extends DiagramElement> selected) {
        List<Node> nodes = nodesIn(selected);
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) {
            boolean insideSelected = false;
            for (Node ancestor = node.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
                insideSelected |= nodes.contains(ancestor);
            }
            if (!insideSelected) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * @return the nodes moved when the selection is dragged: fields follow their
     *         object, and the fixed points of selected note connectors move with them
     */
    public static List<Node> movableNodes(Collection<? extends DiagramElement> selected) {
        List<Node> result = new ArrayList<>(
                topmostNodes(selected).stream().filter(node -> !node.requiresParent()).toList());
        for (Edge edge : edgesIn(selected)) {
            for (Node end : List.of(edge.getStart(), edge.getEnd())) {
                if (end instanceof PointNode && !result.contains(end)) {
                    result.add(end);
                }
            }
        }
        return result;
    }

    /**
     * Removes the selected elements (RF35). Removing a node also removes its
     * children and every edge attached to them; removing a note connector that
     * ends at a fixed point also removes the point.
     */
    public Optional<DiagramOperation> removeElements(Collection<? extends DiagramElement> selected) {
        List<Node> nodes = new ArrayList<>(topmostNodes(selected));
        Set<Node> removedNodes = new LinkedHashSet<>();
        for (Node node : nodes) {
            collectSubtree(node, removedNodes);
        }
        Set<Edge> edges = new LinkedHashSet<>();
        for (Edge edge : diagram.getEdges()) {
            if (selected.contains(edge) || removedNodes.contains(edge.getStart())
                    || removedNodes.contains(edge.getEnd())) {
                edges.add(edge);
            }
        }
        for (Edge edge : edges) {
            for (Node end : List.of(edge.getStart(), edge.getEnd())) {
                if (end instanceof PointNode && removedNodes.add(end)) {
                    nodes.add(end);
                }
            }
        }
        List<DiagramOperation> operations = new ArrayList<>();
        for (Edge edge : edges) {
            operations.add(new RemoveEdgeOperation(diagram, edge));
        }
        for (Node node : nodes) {
            operations.add(new RemoveNodeOperation(diagram, node));
        }
        return operations.isEmpty() ? Optional.empty() : Optional.of(new CompoundOperation(operations));
    }

    private static void collectSubtree(Node node, Set<Node> result) {
        result.add(node);
        for (Node child : node.getChildren()) {
            collectSubtree(child, result);
        }
    }

    /**
     * Shift key (RF15): a selected node inside a package is detached and becomes
     * a root node; a selected root node positioned over a package becomes a child
     * of the innermost package under its center.
     */
    public Optional<DiagramOperation> toggleParent(Collection<? extends DiagramElement> selected) {
        List<DiagramOperation> operations = new ArrayList<>();
        for (Node node : movableNodes(nodesIn(selected))) {
            if (node.getParent() instanceof PackageNode) {
                operations.add(new ReparentNodeOperation(diagram, node, null));
            } else if (node.getParent() == null) {
                findPackageAt(bounds.getBounds(node).center(), node)
                        .filter(target -> target.canAccept(node))
                        .ifPresent(target -> operations.add(new ReparentNodeOperation(diagram, node, target)));
            }
        }
        return operations.isEmpty() ? Optional.empty() : Optional.of(new CompoundOperation(operations));
    }

    private Optional<PackageNode> findPackageAt(Point point, Node excluded) {
        List<Node> nodes = diagram.getAllNodes();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i) instanceof PackageNode candidate && !candidate.hasAncestorOrSelf(excluded)
                    && bounds.getBounds(candidate).contains(point)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
