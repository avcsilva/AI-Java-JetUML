package br.uefs.umleditor.model.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import br.uefs.umleditor.command.AddNodeOperation;
import br.uefs.umleditor.command.CompoundOperation;
import br.uefs.umleditor.command.DiagramOperation;
import br.uefs.umleditor.command.RemoveNodeOperation;
import br.uefs.umleditor.command.ReparentNodeOperation;
import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.layout.GridSnap;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.ParentNode;
import br.uefs.umleditor.model.nodes.PackageNode;

/**
 * Translates the user's gestures on a diagram into operations, applying the
 * structural rules of the diagram.
 */
public class DiagramBuilder {

    private final Diagram diagram;
    private final BoundsCalculator bounds;

    public DiagramBuilder(Diagram diagram, BoundsCalculator bounds) {
        this.diagram = diagram;
        this.bounds = bounds;
    }

    public Diagram getDiagram() {
        return diagram;
    }

    /**
     * @return the topmost node at the point: children are found before their parents
     */
    public Optional<Node> findNodeAt(Point point) {
        List<Node> nodes = diagram.getAllNodes();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (bounds.getBounds(nodes.get(i)).contains(point)) {
                return Optional.of(nodes.get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * @return the nodes whose bounds lie entirely inside the region (lasso selection, RF32)
     */
    public List<Node> findNodesInside(Rectangle region) {
        List<Node> result = new ArrayList<>();
        for (Node node : diagram.getAllNodes()) {
            if (region.contains(bounds.getBounds(node))) {
                result.add(node);
            }
        }
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

    /**
     * @return the selected nodes that are not inside another selected node
     */
    public static List<Node> topmostNodes(Collection<Node> selected) {
        List<Node> result = new ArrayList<>();
        for (Node node : selected) {
            boolean insideSelected = false;
            for (Node ancestor = node.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
                insideSelected |= selected.contains(ancestor);
            }
            if (!insideSelected) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * @return the selected nodes that can be dragged: fields follow their object
     */
    public static List<Node> movableNodes(Collection<Node> selected) {
        return topmostNodes(selected).stream().filter(node -> !node.requiresParent()).toList();
    }

    /**
     * Removes the selected nodes and their children (RF35).
     */
    public Optional<DiagramOperation> removeNodes(Collection<Node> selected) {
        List<DiagramOperation> operations = new ArrayList<>();
        for (Node node : topmostNodes(selected)) {
            operations.add(new RemoveNodeOperation(diagram, node));
        }
        return operations.isEmpty() ? Optional.empty() : Optional.of(new CompoundOperation(operations));
    }

    /**
     * Shift key (RF15): a selected node inside a package is detached and becomes
     * a root node; a selected root node positioned over a package becomes a child
     * of the innermost package under its center.
     */
    public Optional<DiagramOperation> toggleParent(Collection<Node> selected) {
        List<DiagramOperation> operations = new ArrayList<>();
        for (Node node : movableNodes(selected)) {
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
