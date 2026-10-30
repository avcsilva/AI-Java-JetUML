package br.uefs.umleditor.ui;

import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.layout.GridSnap;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.FxGraphicsAdapter;
import br.uefs.umleditor.render.Theme;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * Scrollable drawing area of a diagram, with a fixed size (RF38), an optional
 * grid (RF36) and zoom (RF37).
 *
 * <p>Only the visible part of the diagram is backed by a canvas: the canvas
 * follows the viewport, which keeps memory use low for large diagrams (RNF01).</p>
 */
public class DiagramCanvas extends ScrollPane {

    /** Zoom factors: two levels below and two levels above the normal size. */
    private static final double[] ZOOM_LEVELS = {0.5, 0.75, 1, 1.5, 2};
    private static final int NORMAL_ZOOM = 2;
    private static final double LASSO_OPACITY = 0.15;

    private final Diagram diagram;
    private final DiagramRenderer renderer;
    private final SelectionModel selection;
    private final Pane content = new Pane();
    private final Canvas canvas = new Canvas();
    private int zoomLevel = NORMAL_ZOOM;
    private boolean showGrid;
    private Rectangle lasso;

    public DiagramCanvas(Diagram diagram, DiagramRenderer renderer, SelectionModel selection, boolean showGrid) {
        this.diagram = diagram;
        this.renderer = renderer;
        this.selection = selection;
        this.showGrid = showGrid;
        getStyleClass().add("diagram-canvas");
        content.getStyleClass().add("diagram-outside");
        content.getChildren().add(canvas);
        canvas.setFocusTraversable(true);
        setContent(content);
        setPannable(false);

        viewportBoundsProperty().addListener(observable -> refresh());
        hvalueProperty().addListener(observable -> refresh());
        vvalueProperty().addListener(observable -> refresh());
        updateContentSize();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Converts the position of a mouse event on the canvas to diagram coordinates.
     */
    public Point toDiagramPoint(MouseEvent event) {
        double zoom = getZoom();
        return new Point((event.getX() + canvas.getLayoutX()) / zoom, (event.getY() + canvas.getLayoutY()) / zoom);
    }

    /**
     * @param lasso the selection region being dragged, or null when there is none
     */
    public void setLasso(Rectangle lasso) {
        this.lasso = lasso;
    }

    public double getZoom() {
        return ZOOM_LEVELS[zoomLevel];
    }

    public boolean canZoomIn() {
        return zoomLevel < ZOOM_LEVELS.length - 1;
    }

    public boolean canZoomOut() {
        return zoomLevel > 0;
    }

    public void zoomIn() {
        if (canZoomIn()) {
            setZoomLevel(zoomLevel + 1);
        }
    }

    public void zoomOut() {
        if (canZoomOut()) {
            setZoomLevel(zoomLevel - 1);
        }
    }

    public void resetZoom() {
        setZoomLevel(NORMAL_ZOOM);
    }

    private void setZoomLevel(int level) {
        zoomLevel = level;
        updateContentSize();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        refresh();
    }

    private void updateContentSize() {
        double width = diagram.getWidth() * getZoom();
        double height = diagram.getHeight() * getZoom();
        content.setMinSize(width, height);
        content.setPrefSize(width, height);
        content.setMaxSize(width, height);
        layout();
        refresh();
    }

    /**
     * Moves the canvas over the visible part of the diagram and repaints it.
     */
    public void refresh() {
        Bounds viewport = getViewportBounds();
        double zoom = getZoom();
        double diagramWidth = diagram.getWidth() * zoom;
        double diagramHeight = diagram.getHeight() * zoom;
        double offsetX = Math.max(0, getHvalue() * (diagramWidth - viewport.getWidth()));
        double offsetY = Math.max(0, getVvalue() * (diagramHeight - viewport.getHeight()));
        double width = Math.min(viewport.getWidth(), diagramWidth);
        double height = Math.min(viewport.getHeight(), diagramHeight);

        canvas.setLayoutX(offsetX);
        canvas.setLayoutY(offsetY);
        canvas.setWidth(Math.max(0, width));
        canvas.setHeight(Math.max(0, height));
        paint(offsetX, offsetY, zoom);
    }

    private void paint(double offsetX, double offsetY, double zoom) {
        Theme theme = renderer.getContext().getTheme();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.setFill(theme.getBackground());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.translate(-offsetX, -offsetY);
        gc.scale(zoom, zoom);
        if (showGrid) {
            paintGrid(gc, theme, offsetX / zoom, offsetY / zoom, canvas.getWidth() / zoom, canvas.getHeight() / zoom);
        }
        gc.setLineWidth(1);
        renderer.draw(diagram, new FxGraphicsAdapter(gc, renderer.getContext()), selection.getSelected());
        if (lasso != null) {
            gc.setGlobalAlpha(LASSO_OPACITY);
            gc.setFill(theme.getSelection());
            gc.fillRect(lasso.x(), lasso.y(), lasso.width(), lasso.height());
            gc.setGlobalAlpha(1);
            gc.setStroke(theme.getSelection());
            gc.strokeRect(lasso.x(), lasso.y(), lasso.width(), lasso.height());
        }
    }

    private void paintGrid(GraphicsContext gc, Theme theme, double x, double y, double width, double height) {
        gc.setStroke(theme.getGrid());
        gc.setLineWidth(1 / getZoom());
        double right = Math.min(x + width, diagram.getWidth());
        double bottom = Math.min(y + height, diagram.getHeight());
        int grid = GridSnap.GRID_SIZE;
        for (double gridX = Math.floor(x / grid) * grid; gridX <= right; gridX += grid) {
            gc.strokeLine(gridX, y, gridX, bottom);
        }
        for (double gridY = Math.floor(y / grid) * grid; gridY <= bottom; gridY += grid) {
            gc.strokeLine(x, gridY, right, gridY);
        }
    }
}
