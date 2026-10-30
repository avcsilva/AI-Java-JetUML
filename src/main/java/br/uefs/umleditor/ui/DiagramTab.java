package br.uefs.umleditor.ui;

import br.uefs.umleditor.command.DiagramOperation;
import br.uefs.umleditor.command.UndoManager;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.ToolType;
import br.uefs.umleditor.model.builder.DiagramBuilder;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.FontSettings;
import br.uefs.umleditor.render.RenderContext;
import br.uefs.umleditor.render.Theme;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

/**
 * A tab holding one open diagram: its tool bar, its drawing area, its
 * selection and the history of operations performed on it.
 */
public class DiagramTab extends Tab {

    private final Diagram diagram;
    private final RenderContext renderContext;
    private final DiagramBuilder builder;
    private final SelectionModel selection = new SelectionModel();
    private final UndoManager undoManager = new UndoManager();
    private final ToolPalette toolPalette;
    private final DiagramCanvas diagramCanvas;
    private ContextMenu contextMenu;

    public DiagramTab(Diagram diagram, String title, RenderContext renderContext, boolean showGrid) {
        super(title);
        this.diagram = diagram;
        this.renderContext = renderContext;
        DiagramRenderer renderer = new DiagramRenderer(renderContext);
        this.builder = new DiagramBuilder(diagram, renderer);
        this.toolPalette = new ToolPalette(diagram.getType(), renderContext.getTheme(), this::runActionTool);
        this.diagramCanvas = new DiagramCanvas(diagram, renderer, selection, showGrid);
        new CanvasController(this, builder, selection, diagramCanvas, toolPalette);

        ScrollPane paletteScroll = new ScrollPane(toolPalette);
        paletteScroll.setFitToWidth(true);
        paletteScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        paletteScroll.getStyleClass().add("tool-palette-scroll");

        BorderPane layout = new BorderPane(diagramCanvas);
        layout.setLeft(paletteScroll);
        layout.addEventHandler(KeyEvent.KEY_PRESSED, this::handleToolShortcut);
        setContent(layout);

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
     * Executes an operation on the diagram and keeps it in the history.
     */
    public void perform(DiagramOperation operation) {
        undoManager.execute(operation);
        diagramCanvas.refresh();
    }

    /**
     * Keeps in the history an operation whose effect was already applied.
     */
    public void record(DiagramOperation operation) {
        undoManager.record(operation);
    }

    /**
     * Edit > Select All: selects every element of the diagram (RF33).
     */
    public void selectAll() {
        selection.setAll(diagram.getAllNodes());
        diagramCanvas.refresh();
    }

    /**
     * Edit > Delete: removes the selected elements (RF35).
     */
    public void deleteSelection() {
        builder.removeNodes(selection.getSelected()).ifPresent(this::perform);
        selection.clear();
        diagramCanvas.refresh();
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
        renderContext.setTheme(theme);
        toolPalette.setTheme(theme);
        diagramCanvas.refresh();
    }

    /**
     * Changes the font of the diagram text (RF39).
     */
    public void setFontSettings(FontSettings fontSettings) {
        renderContext.setFontSettings(fontSettings);
        diagramCanvas.refresh();
    }
}
