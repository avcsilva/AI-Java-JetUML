package br.uefs.umleditor.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import br.uefs.umleditor.command.DiagramOperation;
import br.uefs.umleditor.command.UndoManager;
import br.uefs.umleditor.export.DiagramImage;
import br.uefs.umleditor.geom.Point;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramElement;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.ElementCompatibility;
import br.uefs.umleditor.model.InvalidDiagramException;
import br.uefs.umleditor.model.ToolType;
import br.uefs.umleditor.model.builder.DiagramBuilder;
import br.uefs.umleditor.persistence.DiagramContent;
import br.uefs.umleditor.persistence.DiagramDeserializer;
import br.uefs.umleditor.persistence.DiagramSerializer;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.FontSettings;
import br.uefs.umleditor.render.RenderContext;
import br.uefs.umleditor.render.Theme;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

/**
 * A tab holding one open diagram: its tool bar, its drawing area, its
 * selection, the history of operations performed on it and the file it is
 * saved in, if any.
 */
public class DiagramTab extends Tab {

    private final Diagram diagram;
    private final RenderContext renderContext;
    private final DiagramBuilder builder;
    private final SelectionModel selection = new SelectionModel();
    private final UndoManager undoManager = new UndoManager();
    private final ToolPalette toolPalette;
    private final DiagramCanvas diagramCanvas;
    private final NotificationArea notifications;
    private ContextMenu contextMenu;
    private boolean autoEditNode;
    private Path file;
    private boolean modified;

    /**
     * @param notifications shows errors and confirmations to the user (RF41)
     */
    public DiagramTab(Diagram diagram, String title, RenderContext renderContext, boolean showGrid,
                      NotificationArea notifications) {
        super(title);
        this.diagram = diagram;
        this.notifications = notifications;
        this.renderContext = renderContext;
        DiagramRenderer renderer = new DiagramRenderer(renderContext);
        this.builder = DiagramBuilder.forDiagram(diagram, renderer);
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

    public RenderContext getRenderContext() {
        return renderContext;
    }

    /**
     * @return the file of the diagram, or null if it was never saved
     */
    public Path getFile() {
        return file;
    }

    /**
     * Associates the diagram with a file; the tab shows the file name.
     */
    public void setFile(Path file) {
        this.file = file;
        setText(file.getFileName().toString());
    }

    /**
     * @return true if the diagram changed since it was last saved
     */
    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Executes an operation on the diagram and keeps it in the history.
     */
    public void perform(DiagramOperation operation) {
        undoManager.execute(operation);
        modified = true;
        diagramCanvas.refresh();
    }

    /**
     * Keeps in the history an operation whose effect was already applied.
     */
    public void record(DiagramOperation operation) {
        undoManager.record(operation);
        modified = true;
    }

    /**
     * Edit > Undo: reverts the last action (RF21).
     */
    public void undo() {
        undoManager.undo();
        modified = true;
        keepExistingSelection();
        diagramCanvas.refresh();
    }

    /**
     * Edit > Redo: applies again the last undone action (RF21).
     */
    public void redo() {
        undoManager.redo();
        modified = true;
        keepExistingSelection();
        diagramCanvas.refresh();
    }

    private void keepExistingSelection() {
        List<DiagramElement> existing = builder.getAllElements();
        selection.setAll(selection.getSelected().stream().filter(existing::contains).toList());
    }

    /**
     * Edit > Properties (Ctrl+Enter): edits the selected element, when exactly one is selected (RF22).
     */
    public void editSelectedProperties() {
        List<DiagramElement> selected = selection.getSelected();
        if (selected.size() == 1) {
            editProperties(selected.get(0));
        }
    }

    /**
     * Opens the properties panel of the element next to it (RF22). All the
     * changes made while the panel is open are undone as one action.
     */
    public void editProperties(DiagramElement element) {
        if (element.getProperties().isEmpty() || getTabPane() == null) {
            return;
        }
        Rectangle area = builder.getElementBounds(element);
        Point2D anchor = diagramCanvas.toScreen(new Point(area.maxX() + 10, area.y()));
        var scene = getTabPane().getScene();
        PropertyEditor.edit(element, scene.getWindow(), anchor, scene.getStylesheets(), diagramCanvas::refresh)
                .ifPresent(this::record);
        diagramCanvas.refresh();
    }

    public boolean isAutoEditNode() {
        return autoEditNode;
    }

    /**
     * View > Auto Edit Node: opens the properties panel whenever a node is created (RF12).
     */
    public void setAutoEditNode(boolean autoEditNode) {
        this.autoEditNode = autoEditNode;
    }

    /**
     * Edit > Select All: selects every element of the diagram (RF33).
     */
    public void selectAll() {
        selection.setAll(builder.getAllElements());
        diagramCanvas.refresh();
    }

    /**
     * Edit > Delete: removes the selected elements (RF35).
     */
    public void deleteSelection() {
        builder.removeElements(selection.getSelected()).ifPresent(this::perform);
        selection.clear();
        diagramCanvas.refresh();
    }

    /**
     * Edit > Copy: keeps the selected elements in the clipboard (RF34).
     *
     * @return false if nothing in the selection can be copied
     */
    public boolean copySelection(ElementClipboard clipboard) {
        DiagramBuilder.Fragment fragment = builder.copyFragment(selection.getSelected());
        if (fragment.isEmpty()) {
            return false;
        }
        clipboard.setContent(DiagramSerializer.toJson(diagram.getType(), fragment.roots(), fragment.edges()));
        return true;
    }

    /**
     * Edit > Cut: copies the selected elements and removes them (RF34).
     */
    public void cutSelection(ElementClipboard clipboard) {
        if (copySelection(clipboard)) {
            deleteSelection();
        }
    }

    /**
     * Edit > Paste: adds a copy of the clipboard elements and selects them
     * (RF34). Pasting is refused in sequence diagrams, and in other diagrams
     * when an element does not belong to the diagram type.
     */
    public void paste(ElementClipboard clipboard) {
        Optional<String> json = clipboard.getContent();
        if (json.isEmpty()) {
            return;
        }
        if (diagram.getType() == DiagramType.SEQUENCE) {
            reportError("Pasting is not allowed in sequence diagrams");
            return;
        }
        DiagramContent content;
        try {
            content = DiagramDeserializer.read(json.get());
        } catch (InvalidDiagramException e) {
            reportError("The copied elements cannot be pasted: " + e.getMessage());
            return;
        }
        boolean compatible = content.allNodes().stream()
                .allMatch(node -> ElementCompatibility.accepts(diagram.getType(), node))
                && content.edges().stream().allMatch(edge -> ElementCompatibility.accepts(diagram.getType(), edge));
        if (!compatible) {
            reportError("The copied elements are not compatible with a " + diagram.getType().getDisplayName());
            return;
        }
        DiagramBuilder.Paste paste = builder.paste(content.roots(), content.edges(), clipboard.nextPasteOffset());
        perform(paste.operation());
        selection.setAll(paste.elements());
        diagramCanvas.refresh();
    }

    /**
     * File > Copy to Clipboard, the To Clipboard tool and Ctrl+B: copies an
     * image of the whole diagram (RF05).
     */
    public void copyImageToClipboard() {
        if (DiagramImage.copyToClipboard(diagram, renderContext.getFontSettings())) {
            notifications.showConfirmation("Diagram image copied to the clipboard");
        } else {
            reportError("The diagram is empty");
        }
    }

    /**
     * Shows an error notification, such as an invalid connection (RF18).
     */
    public void reportError(String message) {
        notifications.showError(message);
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
        if (tool == ToolType.TO_CLIPBOARD) {
            copyImageToClipboard();
        }
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
