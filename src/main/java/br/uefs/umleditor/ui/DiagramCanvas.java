package br.uefs.umleditor.ui;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.render.Theme;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

/**
 * Scrollable drawing area of a diagram, with a fixed size (RF38), an optional
 * grid (RF36) and zoom (RF37).
 *
 * <p>Only the visible part of the diagram is backed by a canvas: the canvas
 * follows the viewport, which keeps memory use low for large diagrams (RNF01).</p>
 */
public class DiagramCanvas extends ScrollPane {

    public static final int GRID_SIZE = 10;

    /** Zoom factors: two levels below and two levels above the normal size. */
    private static final double[] ZOOM_LEVELS = {0.5, 0.75, 1, 1.5, 2};
    private static final int NORMAL_ZOOM = 2;

    private final Diagram diagram;
    private final Pane content = new Pane();
    private final Canvas canvas = new Canvas();
    private int zoomLevel = NORMAL_ZOOM;
    private boolean showGrid;
    private Theme theme;

    public DiagramCanvas(Diagram diagram, Theme theme, boolean showGrid) {
        this.diagram = diagram;
        this.theme = theme;
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

    public void setTheme(Theme theme) {
        this.theme = theme;
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
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.setFill(theme.getBackground());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.translate(-offsetX, -offsetY);
        gc.scale(zoom, zoom);
        if (showGrid) {
            paintGrid(gc, offsetX / zoom, offsetY / zoom, canvas.getWidth() / zoom, canvas.getHeight() / zoom);
        }
    }

    private void paintGrid(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setStroke(theme.getGrid());
        gc.setLineWidth(1 / getZoom());
        double right = Math.min(x + width, diagram.getWidth());
        double bottom = Math.min(y + height, diagram.getHeight());
        for (double gridX = Math.floor(x / GRID_SIZE) * GRID_SIZE; gridX <= right; gridX += GRID_SIZE) {
            gc.strokeLine(gridX, y, gridX, bottom);
        }
        for (double gridY = Math.floor(y / GRID_SIZE) * GRID_SIZE; gridY <= bottom; gridY += GRID_SIZE) {
            gc.strokeLine(x, gridY, right, gridY);
        }
    }
}
