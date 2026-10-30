package br.uefs.umleditor.layout;

import java.util.ArrayList;
import java.util.List;

import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.edges.CallEdge;
import br.uefs.umleditor.model.edges.ObjectReferenceEdge;
import br.uefs.umleditor.model.edges.ReturnEdge;
import br.uefs.umleditor.model.edges.StateTransitionEdge;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.FinalStateNode;
import br.uefs.umleditor.model.nodes.InitialStateNode;
import br.uefs.umleditor.model.nodes.UseCaseNode;

/**
 * Computes the path of edges from the current position of their nodes, so
 * that edges follow the nodes when they move (RF17). The user changes a path
 * only by moving nodes.
 *
 * <ul>
 * <li>Edges between two nodes are straight segments between the borders of
 * the nodes; several edges between the same two nodes are spread apart.
 * State transitions between the same two states are drawn as arcs.</li>
 * <li>An edge from a node to itself is a loop on one of the corners.</li>
 * <li>A reference starts at the side of the value part of its field.</li>
 * <li>Calls and returns are horizontal: a call leaves the caller at the top
 * of the callee activation, or at the middle of the box of the object it
 * creates; a return leaves the callee at its bottom. Calls and returns on the
 * same lifeline are drawn as small loops on the right side.</li>
 * </ul>
 */
public class EdgePathCalculator {

    private static final double SPACING = 14;
    private static final double LOOP_SIZE = 20;
    private static final int CURVE_SEGMENTS = 16;
    private static final double HIT_DISTANCE = 4;

    private final Diagram diagram;
    private final BoundsCalculator bounds;

    public EdgePathCalculator(Diagram diagram, BoundsCalculator bounds) {
        this.diagram = diagram;
        this.bounds = bounds;
    }

    /**
     * @return the points of the edge path, from the start node to the end node
     */
    public List<Point> getPath(Edge edge) {
        if (edge instanceof CallEdge call) {
            return callPath(call);
        }
        if (edge instanceof ReturnEdge) {
            return returnPath(edge);
        }
        if (edge instanceof ObjectReferenceEdge && edge.getStart() instanceof FieldNode field) {
            return referencePath(field, edge.getEnd());
        }
        if (edge.getStart() == edge.getEnd()) {
            return selfLoop(edge);
        }
        return betweenNodes(edge);
    }

    public Rectangle getBounds(Edge edge) {
        List<Point> path = getPath(edge);
        Rectangle result = new Rectangle(path.get(0).x(), path.get(0).y(), 0, 0);
        for (Point point : path) {
            result = result.union(new Rectangle(point.x(), point.y(), 0, 0));
        }
        return result;
    }

    /**
     * @return true if the point is on the edge path, within a small tolerance
     */
    public boolean contains(Edge edge, Point point) {
        List<Point> path = getPath(edge);
        for (int i = 1; i < path.size(); i++) {
            if (distanceToSegment(point, path.get(i - 1), path.get(i)) <= HIT_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    private List<Point> betweenNodes(Edge edge) {
        Node start = edge.getStart();
        Node end = edge.getEnd();
        List<Edge> parallel = diagram.getEdges().stream().filter(other -> other.links(start, end)).toList();
        int index = Math.max(0, parallel.indexOf(edge));
        double offset = (index - (parallel.size() - 1) / 2.0) * SPACING;

        // The normal is taken from a fixed direction, so that edges in opposite
        // directions between the same nodes get different offsets.
        Node first = parallel.isEmpty() ? start : parallel.get(0).getStart();
        Node second = first == start ? end : start;
        Point from = center(first);
        Point to = center(second);
        Point normal = unitNormal(from, to);

        Point startCenter = center(start);
        Point endCenter = center(end);
        if (edge instanceof StateTransitionEdge && offset != 0) {
            Point middle = new Point((startCenter.x() + endCenter.x()) / 2 + normal.x() * offset * 2,
                    (startCenter.y() + endCenter.y()) / 2 + normal.y() * offset * 2);
            Point startPoint = exitPoint(start, startCenter, middle);
            Point endPoint = exitPoint(end, endCenter, middle);
            return quadraticCurve(startPoint, middle, endPoint);
        }
        Point shiftedStart = startCenter.translated(normal.x() * offset, normal.y() * offset);
        Point shiftedEnd = endCenter.translated(normal.x() * offset, normal.y() * offset);
        return List.of(exitPoint(start, shiftedStart, shiftedEnd), exitPoint(end, shiftedEnd, shiftedStart));
    }

    /**
     * A loop on a corner of the node; each loop of the same node uses another corner.
     */
    private List<Point> selfLoop(Edge edge) {
        Node node = edge.getStart();
        List<Edge> loops = diagram.getEdges().stream()
                .filter(other -> other.getStart() == node && other.getEnd() == node).toList();
        int corner = Math.max(0, loops.indexOf(edge)) % 4;
        Rectangle b = bounds.getBounds(node);
        double size = Math.min(LOOP_SIZE, Math.min(b.width(), b.height()) / 2);
        boolean right = corner == 0 || corner == 1;
        boolean top = corner == 0 || corner == 3;
        double x = right ? b.maxX() : b.x();
        double y = top ? b.y() : b.maxY();
        double sx = right ? 1 : -1;
        double sy = top ? -1 : 1;
        return List.of(
                new Point(x - sx * size, y),
                new Point(x - sx * size, y + sy * size),
                new Point(x + sx * size, y + sy * size),
                new Point(x + sx * size, y - sy * size),
                new Point(x, y - sy * size));
    }

    /**
     * A reference leaves the value part of its field from the side facing the
     * target object, or loops around when it refers to the object of the field.
     */
    private List<Point> referencePath(FieldNode field, Node target) {
        Rectangle row = bounds.getBounds(field);
        Rectangle targetBounds = bounds.getBounds(target);
        double y = row.center().y();
        if (field.hasAncestorOrSelf(target)) {
            double outX = row.maxX() + LOOP_SIZE;
            double aboveY = targetBounds.y() - LOOP_SIZE;
            double targetX = targetBounds.center().x();
            return List.of(new Point(row.maxX(), y), new Point(outX, y), new Point(outX, aboveY),
                    new Point(targetX, aboveY), new Point(targetX, targetBounds.y()));
        }
        boolean toLeft = targetBounds.center().x() < row.center().x();
        Point start = new Point(toLeft ? row.x() : row.maxX(), y);
        return List.of(start, exitPoint(target, targetBounds.center(), start));
    }

    private List<Point> callPath(CallEdge call) {
        Rectangle caller = bounds.getBounds(call.getStart());
        Rectangle callee = bounds.getBounds(call.getEnd());
        if (call.isConstructorCall()) {
            Rectangle box = bounds.getConnectionBounds(call.getEnd().getParent());
            return horizontal(caller, box, box.center().y());
        }
        if (call.getStart().getParent() == call.getEnd().getParent()) {
            double y = callee.y();
            double outX = callee.maxX() + LOOP_SIZE;
            return List.of(new Point(caller.maxX(), y), new Point(outX, y), new Point(outX, y + LOOP_SIZE / 2),
                    new Point(callee.maxX(), y + LOOP_SIZE / 2));
        }
        return horizontal(caller, callee, callee.y());
    }

    private List<Point> returnPath(Edge edge) {
        Rectangle callee = bounds.getBounds(edge.getStart());
        Rectangle caller = bounds.getBounds(edge.getEnd());
        double y = callee.maxY();
        if (edge.getStart().getParent() == edge.getEnd().getParent()) {
            double outX = callee.maxX() + LOOP_SIZE;
            return List.of(new Point(callee.maxX(), y), new Point(outX, y), new Point(outX, y + LOOP_SIZE / 2),
                    new Point(caller.maxX(), y + LOOP_SIZE / 2));
        }
        return horizontal(callee, caller, y);
    }

    /**
     * @return a horizontal segment at height y between the facing sides of two areas
     */
    private static List<Point> horizontal(Rectangle from, Rectangle to, double y) {
        if (to.center().x() >= from.center().x()) {
            return List.of(new Point(from.maxX(), y), new Point(to.x(), y));
        }
        return List.of(new Point(from.x(), y), new Point(to.maxX(), y));
    }

    private Point center(Node node) {
        return bounds.getBounds(node).center();
    }

    private static Point unitNormal(Point from, Point to) {
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double length = Math.hypot(dx, dy);
        if (length == 0) {
            return new Point(0, 0);
        }
        return new Point(-dy / length, dx / length);
    }

    /**
     * @return where the segment from inside (a point in the node) towards the
     *         target point leaves the shape of the node
     */
    private Point exitPoint(Node node, Point inside, Point toward) {
        Rectangle b = bounds.getBounds(node);
        if (!b.contains(inside) || b.contains(toward)) {
            return inside;
        }
        double dx = toward.x() - inside.x();
        double dy = toward.y() - inside.y();
        double t = isElliptical(node) ? ellipseExit(b, inside, dx, dy) : rectangleExit(b, inside, dx, dy);
        return new Point(inside.x() + t * dx, inside.y() + t * dy);
    }

    private static boolean isElliptical(Node node) {
        return node instanceof UseCaseNode || node instanceof InitialStateNode || node instanceof FinalStateNode;
    }

    private static double rectangleExit(Rectangle b, Point p, double dx, double dy) {
        double t = Double.MAX_VALUE;
        if (dx > 0) {
            t = Math.min(t, (b.maxX() - p.x()) / dx);
        } else if (dx < 0) {
            t = Math.min(t, (b.x() - p.x()) / dx);
        }
        if (dy > 0) {
            t = Math.min(t, (b.maxY() - p.y()) / dy);
        } else if (dy < 0) {
            t = Math.min(t, (b.y() - p.y()) / dy);
        }
        return t == Double.MAX_VALUE ? 0 : t;
    }

    private static double ellipseExit(Rectangle b, Point p, double dx, double dy) {
        double a = b.width() / 2;
        double c = b.height() / 2;
        Point center = b.center();
        double px = (p.x() - center.x()) / a;
        double py = (p.y() - center.y()) / c;
        double qx = dx / a;
        double qy = dy / c;
        double qa = qx * qx + qy * qy;
        double qb = 2 * (px * qx + py * qy);
        double qc = px * px + py * py - 1;
        double discriminant = qb * qb - 4 * qa * qc;
        if (qa == 0 || discriminant < 0) {
            return 0;
        }
        return (-qb + Math.sqrt(discriminant)) / (2 * qa);
    }

    private static List<Point> quadraticCurve(Point start, Point control, Point end) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i <= CURVE_SEGMENTS; i++) {
            double t = (double) i / CURVE_SEGMENTS;
            double u = 1 - t;
            points.add(new Point(u * u * start.x() + 2 * u * t * control.x() + t * t * end.x(),
                    u * u * start.y() + 2 * u * t * control.y() + t * t * end.y()));
        }
        return points;
    }

    static double distanceToSegment(Point p, Point a, Point b) {
        double dx = b.x() - a.x();
        double dy = b.y() - a.y();
        double lengthSquared = dx * dx + dy * dy;
        double t = lengthSquared == 0 ? 0
                : Math.max(0, Math.min(1, ((p.x() - a.x()) * dx + (p.y() - a.y()) * dy) / lengthSquared));
        return Math.hypot(p.x() - (a.x() + t * dx), p.y() - (a.y() + t * dy));
    }
}
