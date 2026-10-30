package br.uefs.umleditor.render.nodes;

import br.uefs.umleditor.geom.Dimension;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.GraphicsAdapter;
import br.uefs.umleditor.render.LineStyle;
import br.uefs.umleditor.render.TextStyle;

/**
 * Draws a note as a sheet with a folded corner (RF13).
 */
public class NoteNodeRenderer extends NodeRenderer<NoteNode> {

    private static final double MIN_WIDTH = 60;
    private static final double MIN_HEIGHT = 40;
    private static final double FOLD = 8;

    public NoteNodeRenderer(DiagramRenderer diagramRenderer) {
        super(diagramRenderer);
    }

    @Override
    public Rectangle getBounds(NoteNode node) {
        Dimension text = measure(node.getText(), TextStyle.PLAIN);
        return new Rectangle(node.getX(), node.getY(), Math.max(MIN_WIDTH, text.width() + 2 * PADDING + FOLD),
                Math.max(MIN_HEIGHT, text.height() + 2 * PADDING));
    }

    @Override
    public void draw(NoteNode node, GraphicsAdapter g) {
        Rectangle b = getBounds(node);
        double foldX = b.maxX() - FOLD;
        double foldY = b.y() + FOLD;
        g.drawPolygon(new double[] {b.x(), foldX, b.maxX(), b.maxX(), b.x()},
                new double[] {b.y(), b.y(), foldY, b.maxY(), b.maxY()}, theme().getNoteFill(), theme().getStroke());
        g.drawLine(foldX, b.y(), foldX, foldY, theme().getStroke(), LineStyle.SOLID);
        g.drawLine(foldX, foldY, b.maxX(), foldY, theme().getStroke(), LineStyle.SOLID);
        drawText(g, node.getText(), TextStyle.PLAIN, b, false, false);
    }
}
