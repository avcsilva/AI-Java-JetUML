package br.uefs.umleditor.render;

import java.util.List;

import br.uefs.umleditor.geom.Dimension;
import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.layout.EdgePathCalculator;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.edges.AggregationEdge;
import br.uefs.umleditor.model.edges.ClassRelationshipEdge;
import br.uefs.umleditor.model.edges.DependencyEdge;
import br.uefs.umleditor.model.edges.Directionality;
import br.uefs.umleditor.model.edges.GeneralizationEdge;
import br.uefs.umleditor.model.edges.LabeledEdge;
import br.uefs.umleditor.model.edges.NoteEdge;
import br.uefs.umleditor.model.edges.ObjectReferenceEdge;
import br.uefs.umleditor.model.edges.StateTransitionEdge;
import br.uefs.umleditor.model.edges.UseCaseDependencyEdge;
import br.uefs.umleditor.model.edges.UseCaseGeneralizationEdge;

/**
 * Draws edges along the path computed by {@link EdgePathCalculator}, with
 * the line style, arrow heads and labels of each edge type.
 */
public class EdgeRenderer {

    private static final double LABEL_DISTANCE = 12;

    /**
     * How an edge looks: line pattern, heads at both ends and labels.
     */
    record Appearance(LineStyle line, ArrowHead startHead, ArrowHead endHead, String startLabel,
                      String middleLabel, String endLabel) {

        static Appearance of(LineStyle line, ArrowHead startHead, ArrowHead endHead) {
            return new Appearance(line, startHead, endHead, "", "", "");
        }
    }

    private final RenderContext context;

    public EdgeRenderer(RenderContext context) {
        this.context = context;
    }

    static Appearance appearanceOf(Edge edge) {
        if (edge instanceof DependencyEdge dependency) {
            boolean both = dependency.getDirectionality() == Directionality.BIDIRECTIONAL;
            return Appearance.of(LineStyle.DASHED, both ? ArrowHead.OPEN : ArrowHead.NONE, ArrowHead.OPEN);
        }
        if (edge instanceof GeneralizationEdge generalization) {
            boolean implementation = generalization.getType() == GeneralizationEdge.Type.IMPLEMENTATION;
            return Appearance.of(implementation ? LineStyle.DASHED : LineStyle.SOLID, ArrowHead.NONE,
                    ArrowHead.TRIANGLE);
        }
        if (edge instanceof ClassRelationshipEdge relationship) {
            Directionality directionality = relationship.getDirectionality();
            ArrowHead startHead = directionality == Directionality.BIDIRECTIONAL ? ArrowHead.OPEN : ArrowHead.NONE;
            if (relationship instanceof AggregationEdge aggregation) {
                startHead = aggregation.getType() == AggregationEdge.Type.COMPOSITION
                        ? ArrowHead.FILLED_DIAMOND : ArrowHead.DIAMOND;
            }
            ArrowHead endHead = directionality == Directionality.UNSPECIFIED ? ArrowHead.NONE : ArrowHead.OPEN;
            return new Appearance(LineStyle.SOLID, startHead, endHead, relationship.getStartLabel(),
                    relationship.getMiddleLabel(), relationship.getEndLabel());
        }
        if (edge instanceof NoteEdge) {
            return Appearance.of(LineStyle.DASHED, ArrowHead.NONE, ArrowHead.NONE);
        }
        if (edge instanceof StateTransitionEdge transition) {
            return new Appearance(LineStyle.SOLID, ArrowHead.NONE, ArrowHead.OPEN, "", transition.getMiddleLabel(), "");
        }
        if (edge instanceof LabeledEdge labeled) {
            return new Appearance(LineStyle.SOLID, ArrowHead.NONE, ArrowHead.NONE, "", labeled.getMiddleLabel(), "");
        }
        if (edge instanceof ObjectReferenceEdge) {
            return Appearance.of(LineStyle.SOLID, ArrowHead.NONE, ArrowHead.FILLED_TRIANGLE);
        }
        if (edge instanceof UseCaseDependencyEdge dependency) {
            return new Appearance(LineStyle.DASHED, ArrowHead.NONE, ArrowHead.OPEN, "",
                    dependency.getType().getStereotype(), "");
        }
        if (edge instanceof UseCaseGeneralizationEdge) {
            return Appearance.of(LineStyle.SOLID, ArrowHead.NONE, ArrowHead.TRIANGLE);
        }
        return Appearance.of(LineStyle.SOLID, ArrowHead.NONE, ArrowHead.NONE);
    }

    public void draw(Edge edge, List<Point> path, GraphicsAdapter g) {
        Theme theme = context.getTheme();
        Appearance appearance = appearanceOf(edge);
        for (int i = 1; i < path.size(); i++) {
            Point a = path.get(i - 1);
            Point b = path.get(i);
            g.drawLine(a.x(), a.y(), b.x(), b.y(), theme.getStroke(), appearance.line());
        }
        int last = path.size() - 1;
        appearance.startHead().draw(g, path.get(1), path.get(0), theme.getStroke(), theme.getBackground());
        appearance.endHead().draw(g, path.get(last - 1), path.get(last), theme.getStroke(), theme.getBackground());
        drawEndLabel(g, appearance.startLabel(), path.get(0), path.get(1));
        drawEndLabel(g, appearance.endLabel(), path.get(last), path.get(last - 1));
        drawMiddleLabel(g, appearance.middleLabel(), path);
    }

    /**
     * Draws a label next to an end of the edge, on the side of the line.
     */
    private void drawEndLabel(GraphicsAdapter g, String label, Point end, Point next) {
        if (label.isEmpty()) {
            return;
        }
        double dx = next.x() - end.x();
        double dy = next.y() - end.y();
        double length = Math.max(1, Math.hypot(dx, dy));
        Dimension size = context.measure(label, TextStyle.PLAIN);
        double x = end.x() + dx / length * LABEL_DISTANCE;
        double y = end.y() + dy / length * LABEL_DISTANCE;
        // Keep the text on the side opposite to the direction of the line.
        double textX = dy * dx >= 0 ? x + 4 : x - size.width() - 4;
        if (Math.abs(dy) < 1) {
            textX = dx >= 0 ? x : x - size.width();
        }
        g.drawText(label, textX, y - size.height() - 2, TextStyle.PLAIN, context.getTheme().getText());
    }

    private void drawMiddleLabel(GraphicsAdapter g, String label, List<Point> path) {
        if (label.isEmpty()) {
            return;
        }
        Point middle = middleOf(path);
        Dimension size = context.measure(label, TextStyle.PLAIN);
        g.drawText(label, middle.x() - size.width() / 2, middle.y() - size.height() - 2, TextStyle.PLAIN,
                context.getTheme().getText());
    }

    /**
     * @return the point halfway along the path
     */
    static Point middleOf(List<Point> path) {
        double total = 0;
        for (int i = 1; i < path.size(); i++) {
            total += distance(path.get(i - 1), path.get(i));
        }
        double remaining = total / 2;
        for (int i = 1; i < path.size(); i++) {
            Point a = path.get(i - 1);
            Point b = path.get(i);
            double length = distance(a, b);
            if (length >= remaining && length > 0) {
                double t = remaining / length;
                return new Point(a.x() + t * (b.x() - a.x()), a.y() + t * (b.y() - a.y()));
            }
            remaining -= length;
        }
        return path.get(path.size() - 1);
    }

    private static double distance(Point a, Point b) {
        return Math.hypot(b.x() - a.x(), b.y() - a.y());
    }
}
