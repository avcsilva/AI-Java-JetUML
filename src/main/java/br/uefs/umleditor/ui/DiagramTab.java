package br.uefs.umleditor.ui;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.ToolType;
import br.uefs.umleditor.render.Theme;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

/**
 * A tab holding one open diagram: its tool bar and its drawing area.
 */
public class DiagramTab extends Tab {

    private final Diagram diagram;
    private final ToolPalette toolPalette;
    private final DiagramCanvas diagramCanvas;
    private ContextMenu contextMenu;

    public DiagramTab(Diagram diagram, String title, Theme theme, boolean showGrid) {
        super(title);
        this.diagram = diagram;
        this.toolPalette = new ToolPalette(diagram.getType(), theme, this::runActionTool);
        this.diagramCanvas = new DiagramCanvas(diagram, theme, showGrid);

        ScrollPane paletteScroll = new ScrollPane(toolPalette);
        paletteScroll.setFitToWidth(true);
        paletteScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        paletteScroll.getStyleClass().add("tool-palette-scroll");

        BorderPane layout = new BorderPane(diagramCanvas);
        layout.setLeft(paletteScroll);
        layout.addEventHandler(KeyEvent.KEY_PRESSED, this::handleToolShortcut);
        setContent(layout);

        diagramCanvas.getCanvas().addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> diagramCanvas.getCanvas().requestFocus());
        diagramCanvas.getCanvas().setOnContextMenuRequested(event -> {
            if (contextMenu != null) {
                contextMenu.hide();
            }
            contextMenu = toolPalette.createContextMenu();
            contextMenu.show(diagramCanvas.getCanvas(), event.getScreenX(), event.getScreenY());
            event.consume();
        });
        setOnSelectionChanged(event -> {
            if (isSelected()) {
                diagramCanvas.getCanvas().requestFocus();
            }
        });
    }

    public Diagram getDiagram() {
        return diagram;
    }

    public ToolPalette getToolPalette() {
        return toolPalette;
    }

    public DiagramCanvas getDiagramCanvas() {
        return diagramCanvas;
    }

    /**
     * Activates a tool by its key: 1-9, 0, A, B... without modifiers (RF08).
     */
    private void handleToolShortcut(KeyEvent event) {
        if (event.isControlDown() || event.isAltDown() || event.isMetaDown() || event.isShortcutDown()
                || event.isShiftDown() || event.getText().length() != 1) {
            return;
        }
        int index = diagram.getType().toolIndexForShortcut(event.getText().charAt(0));
        if (index >= 0) {
            toolPalette.selectTool(index);
            event.consume();
        }
    }

    private void runActionTool(ToolType tool) {
        // To Clipboard is delivered together with image export (phase 6).
    }

    public void setTheme(Theme theme) {
        toolPalette.setTheme(theme);
        diagramCanvas.setTheme(theme);
    }
}
