package br.uefs.umleditor.render;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.layout.EdgePathCalculator;
import br.uefs.umleditor.model.BoundsCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramElement;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.model.nodes.ActorNode;
import br.uefs.umleditor.model.nodes.ClassNode;
import br.uefs.umleditor.model.nodes.FieldNode;
import br.uefs.umleditor.model.nodes.FinalStateNode;
import br.uefs.umleditor.model.nodes.ImplicitParameterNode;
import br.uefs.umleditor.model.nodes.InitialStateNode;
import br.uefs.umleditor.model.nodes.InterfaceNode;
import br.uefs.umleditor.model.nodes.NoteNode;
import br.uefs.umleditor.model.nodes.ObjectNode;
import br.uefs.umleditor.model.nodes.PackageDescriptionNode;
import br.uefs.umleditor.model.nodes.PackageNode;
import br.uefs.umleditor.model.nodes.PointNode;
import br.uefs.umleditor.model.nodes.StateNode;
import br.uefs.umleditor.model.nodes.UseCaseNode;
import br.uefs.umleditor.render.nodes.ClassNodeRenderer;
import br.uefs.umleditor.render.nodes.FieldNodeRenderer;
import br.uefs.umleditor.render.nodes.ImplicitParameterNodeRenderer;
import br.uefs.umleditor.render.nodes.NodeRenderer;
import br.uefs.umleditor.render.nodes.NoteNodeRenderer;
import br.uefs.umleditor.render.nodes.ObjectNodeRenderer;
import br.uefs.umleditor.render.nodes.PackageNodeRenderer;
import br.uefs.umleditor.render.nodes.PointNodeRenderer;
import br.uefs.umleditor.render.nodes.StateNodeRenderer;
import br.uefs.umleditor.render.nodes.UseCaseNodeRenderer;

/**
 * Draws a whole diagram and computes the bounds of its nodes, delegating
 * each node type to its renderer. Edges are drawn over the nodes.
 */
public class DiagramRenderer implements BoundsCalculator {

    private static final double HANDLE_SIZE = 6;

    private final RenderContext context;
    private final Map<Class<? extends Node>, NodeRenderer<?>> renderers = new HashMap<>();
    private final FieldNodeRenderer fieldRenderer;
    private final EdgeRenderer edgeRenderer;

    public DiagramRenderer(RenderContext context) {
        this.context = context;
        ClassNodeRenderer classRenderer = new ClassNodeRenderer(this);
        PackageNodeRenderer packageRenderer = new PackageNodeRenderer(this);
        StateNodeRenderer stateRenderer = new StateNodeRenderer(this);
        ObjectNodeRenderer objectRenderer = new ObjectNodeRenderer(this);
        UseCaseNodeRenderer useCaseRenderer = new UseCaseNodeRenderer(this);
        renderers.put(ClassNode.class, classRenderer);
        renderers.put(InterfaceNode.class, classRenderer);
        renderers.put(PackageNode.class, packageRenderer);
        renderers.put(PackageDescriptionNode.class, packageRenderer);
        renderers.put(NoteNode.class, new NoteNodeRenderer(this));
        renderers.put(StateNode.class, stateRenderer);
        renderers.put(InitialStateNode.class, stateRenderer);
        renderers.put(FinalStateNode.class, stateRenderer);
        renderers.put(ObjectNode.class, objectRenderer);
        fieldRenderer = new FieldNodeRenderer(this, objectRenderer);
        renderers.put(FieldNode.class, fieldRenderer);
        renderers.put(ActorNode.class, useCaseRenderer);
        renderers.put(UseCaseNode.class, useCaseRenderer);
        renderers.put(ImplicitParameterNode.class, new ImplicitParameterNodeRenderer(this));
        renderers.put(PointNode.class, new PointNodeRenderer(this));
        edgeRenderer = new EdgeRenderer(context);
    }

    public RenderContext getContext() {
        return context;
    }

    @SuppressWarnings("unchecked")
    private <N extends Node> NodeRenderer<N> rendererFor(N node) {
        NodeRenderer<?> renderer = renderers.get(node.getClass());
        if (renderer == null) {
            throw new IllegalArgumentException("No renderer for " + node.getClass().getSimpleName());
        }
        return (NodeRenderer<N>) renderer;
    }

    @Override
    public Rectangle getBounds(Node node) {
        return rendererFor(node).getBounds(node);
    }

    @Override
    public Rectangle getConnectionBounds(Node node) {
        return node instanceof FieldNode field ? fieldRenderer.getValueBounds(field) : getBounds(node);
    }

    /**
     * Updates the stored position of nodes whose position derives from their
     * children or parent (packages with children and fields), so that the
     * model matches what is drawn.
     */
    public void layout(Diagram diagram) {
        List<Node> nodes = diagram.getAllNodes();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            if ((node instanceof PackageNode && !node.getChildren().isEmpty()) || node instanceof FieldNode) {
                Rectangle bounds = getBounds(node);
                node.setPosition(bounds.x(), bounds.y());
            }
        }
    }

    /**
     * Draws all nodes, parents before children, then the edges, then marks the
     * selected elements: the corners of selected nodes and the ends of selected edges.
     */
    public void draw(Diagram diagram, GraphicsAdapter g, Collection<DiagramElement> selection) {
        layout(diagram);
        for (Node node : diagram.getAllNodes()) {
            rendererFor(node).draw(node, g);
        }
        EdgePathCalculator paths = new EdgePathCalculator(diagram, this);
        for (Edge edge : diagram.getEdges()) {
            edgeRenderer.draw(edge, paths.getPath(edge), g);
        }
        for (DiagramElement element : selection) {
            if (element instanceof Node node) {
                Rectangle bounds = getBounds(node);
                drawHandle(g, bounds.x(), bounds.y());
                drawHandle(g, bounds.maxX(), bounds.y());
                drawHandle(g, bounds.x(), bounds.maxY());
                drawHandle(g, bounds.maxX(), bounds.maxY());
            } else if (element instanceof Edge edge) {
                List<Point> path = paths.getPath(edge);
                drawHandle(g, path.get(0).x(), path.get(0).y());
                drawHandle(g, path.get(path.size() - 1).x(), path.get(path.size() - 1).y());
            }
        }
    }

    private void drawHandle(GraphicsAdapter g, double x, double y) {
        double half = HANDLE_SIZE / 2;
        g.drawRectangle(new Rectangle(x - half, y - half, HANDLE_SIZE, HANDLE_SIZE),
                context.getTheme().getSelection(), null);
    }
}
