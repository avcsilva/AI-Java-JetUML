package br.uefs.umleditor.app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import br.uefs.umleditor.export.DiagramImage;
import br.uefs.umleditor.export.ImageFormat;
import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.InvalidDiagramException;
import br.uefs.umleditor.persistence.DiagramDeserializer;
import br.uefs.umleditor.persistence.DiagramSerializer;
import br.uefs.umleditor.persistence.RecentFiles;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.FontSettings;
import br.uefs.umleditor.render.RenderContext;
import br.uefs.umleditor.render.Theme;
import br.uefs.umleditor.ui.DiagramCanvas;
import br.uefs.umleditor.ui.DiagramTab;
import br.uefs.umleditor.ui.ElementClipboard;
import br.uefs.umleditor.ui.NotificationArea;
import br.uefs.umleditor.ui.dialogs.DiagramSizeDialog;
import br.uefs.umleditor.ui.dialogs.FontDialog;
import br.uefs.umleditor.ui.dialogs.NotificationDurationDialog;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Main window: menu bar, diagram tabs and notification area. It also
 * carries out the file operations (RF02-RF06) and the element clipboard
 * shared by all diagrams (RF34).
 */
public class MainWindow {

    private static final String BASE_STYLESHEET = "/br/uefs/umleditor/base.css";
    private static final String DARK_STYLESHEET = "/br/uefs/umleditor/dark.css";
    private static final String DIAGRAM_EXTENSION = ".json";
    private static final double CONTENT_MARGIN = 20;

    private final Stage stage;
    private final UserPreferences preferences;
    private final TabPane tabPane = new TabPane();
    private final Tab welcomeTab;
    private final NotificationArea notificationArea;
    private final Scene scene;
    private final RecentFiles recentFiles = new RecentFiles(Preferences.userNodeForPackage(MainWindow.class));
    private final ElementClipboard elementClipboard = new ElementClipboard();
    private final Menu recentMenu = new Menu("Recent Files");
    private File lastDirectory;
    private MenuItem zoomInItem;
    private MenuItem zoomOutItem;
    private MenuItem resetZoomItem;

    public MainWindow(Stage stage, UserPreferences preferences) {
        this.stage = stage;
        this.preferences = preferences;
        this.notificationArea = new NotificationArea(preferences.notificationDurationProperty());

        welcomeTab = new Tab("Welcome", new WelcomePane(this::newDiagram));
        welcomeTab.setClosable(false);
        tabPane.getTabs().add(welcomeTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> showWelcomeIfEmpty());
        tabPane.getSelectionModel().selectedItemProperty().addListener(observable -> updateZoomItems());

        StackPane center = new StackPane(tabPane, notificationArea);
        StackPane.setAlignment(notificationArea, Pos.BOTTOM_LEFT);

        BorderPane root = new BorderPane(center);
        root.setTop(createMenuBar());

        scene = new Scene(root, 1100, 750);
        applyStylesheets();
        preferences.darkModeProperty().addListener(observable -> applyTheme());
        preferences.showGridProperty().addListener(observable -> forEachDiagramTab(
                tab -> tab.getDiagramCanvas().setShowGrid(preferences.showGridProperty().get())));
        preferences.showToolHintProperty().addListener(observable -> forEachDiagramTab(
                tab -> tab.getToolPalette().setShowToolHint(preferences.showToolHintProperty().get())));
        preferences.verboseTooltipsProperty().addListener(observable -> forEachDiagramTab(
                tab -> tab.getToolPalette().setVerboseTooltips(preferences.verboseTooltipsProperty().get())));
        preferences.autoEditNodeProperty().addListener(observable -> forEachDiagramTab(
                tab -> tab.setAutoEditNode(preferences.autoEditNodeProperty().get())));
        preferences.fontSettingsProperty().addListener(observable -> forEachDiagramTab(
                tab -> tab.setFontSettings(preferences.fontSettingsProperty().get())));

        stage.setTitle("UML Editor");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            if (!confirmCloseAll()) {
                event.consume();
            }
        });
        updateZoomItems();
    }

    public void show() {
        stage.show();
    }

    private MenuBar createMenuBar() {
        return new MenuBar(createFileMenu(), createEditMenu(), createViewMenu());
    }

    private static MenuItem item(String text, KeyCombination accelerator, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setAccelerator(accelerator);
        item.setOnAction(event -> action.run());
        return item;
    }

    private static KeyCombination shortcut(KeyCode key) {
        return new KeyCodeCombination(key, KeyCombination.SHORTCUT_DOWN);
    }

    private Menu createFileMenu() {
        Menu newMenu = new Menu("New");
        for (DiagramType type : DiagramType.values()) {
            newMenu.getItems().add(item(type.getDisplayName(), null, () -> newDiagram(type)));
        }
        recentMenu.setOnShowing(event -> updateRecentMenu());
        updateRecentMenu();
        return new Menu("File", null, newMenu,
                item("Open...", shortcut(KeyCode.O), this::open),
                recentMenu,
                new SeparatorMenuItem(),
                item("Close", shortcut(KeyCode.W), this::closeCurrent),
                item("Save", shortcut(KeyCode.S), () -> currentDiagramTab().ifPresent(this::save)),
                item("Save As...", null, () -> currentDiagramTab().ifPresent(this::saveAs)),
                item("Duplicate", shortcut(KeyCode.D), this::duplicate),
                new SeparatorMenuItem(),
                item("Export Image...", null, this::exportImage),
                item("Copy to Clipboard", shortcut(KeyCode.B),
                        () -> currentDiagramTab().ifPresent(DiagramTab::copyImageToClipboard)),
                new SeparatorMenuItem(),
                item("Exit", shortcut(KeyCode.Q), this::exit));
    }

    private Menu createEditMenu() {
        return new Menu("Edit", null,
                item("Undo", shortcut(KeyCode.Z), () -> currentDiagramTab().ifPresent(DiagramTab::undo)),
                item("Redo", shortcut(KeyCode.Y), () -> currentDiagramTab().ifPresent(DiagramTab::redo)),
                new SeparatorMenuItem(),
                item("Cut", shortcut(KeyCode.X),
                        () -> currentDiagramTab().ifPresent(tab -> tab.cutSelection(elementClipboard))),
                item("Copy", shortcut(KeyCode.C),
                        () -> currentDiagramTab().ifPresent(tab -> tab.copySelection(elementClipboard))),
                item("Paste", shortcut(KeyCode.V),
                        () -> currentDiagramTab().ifPresent(tab -> tab.paste(elementClipboard))),
                item("Delete", new KeyCodeCombination(KeyCode.DELETE),
                        () -> currentDiagramTab().ifPresent(DiagramTab::deleteSelection)),
                item("Select All", shortcut(KeyCode.A), () -> currentDiagramTab().ifPresent(DiagramTab::selectAll)),
                new SeparatorMenuItem(),
                item("Properties", shortcut(KeyCode.ENTER),
                        () -> currentDiagramTab().ifPresent(DiagramTab::editSelectedProperties)));
    }

    private Menu createViewMenu() {
        CheckMenuItem showGrid = checkItem("Show Grid", preferences.showGridProperty());
        CheckMenuItem showToolHint = checkItem("Show Tool Hint", preferences.showToolHintProperty());
        CheckMenuItem verboseTooltips = checkItem("Verbose Tooltips", preferences.verboseTooltipsProperty());
        CheckMenuItem autoEditNode = checkItem("Auto Edit Node", preferences.autoEditNodeProperty());

        zoomInItem = item("Zoom In", shortcut(KeyCode.EQUALS), () -> zoom(DiagramCanvas::zoomIn));
        zoomOutItem = item("Zoom Out", shortcut(KeyCode.MINUS), () -> zoom(DiagramCanvas::zoomOut));
        resetZoomItem = item("Reset Zoom", shortcut(KeyCode.DIGIT0), () -> zoom(DiagramCanvas::resetZoom));

        MenuItem diagramSize = item("Set Diagram Size...", null, this::editDiagramSize);
        MenuItem changeFont = item("Change Font...", null, this::editFont);
        CheckMenuItem darkMode = checkItem("Dark Mode", preferences.darkModeProperty());
        MenuItem notificationDuration = item("Notification Duration...", null, this::editNotificationDuration);

        return new Menu("View", null, showGrid, showToolHint, verboseTooltips, autoEditNode, new SeparatorMenuItem(),
                zoomInItem, zoomOutItem, resetZoomItem, new SeparatorMenuItem(),
                diagramSize, changeFont, darkMode, notificationDuration);
    }

    private static CheckMenuItem checkItem(String text, BooleanProperty setting) {
        CheckMenuItem item = new CheckMenuItem(text);
        item.selectedProperty().bindBidirectional(setting);
        return item;
    }

    /**
     * Creates an empty diagram of the given type in a new tab (RF01).
     */
    public void newDiagram(DiagramType type) {
        Diagram diagram = new Diagram(type, preferences.getDiagramWidth(), preferences.getDiagramHeight());
        addTab(diagram, type.getDisplayName());
    }

    private DiagramTab addTab(Diagram diagram, String title) {
        RenderContext renderContext = new RenderContext(currentTheme(), preferences.fontSettingsProperty().get());
        DiagramTab tab = new DiagramTab(diagram, title, renderContext, preferences.showGridProperty().get(),
                notificationArea);
        tab.getToolPalette().setShowToolHint(preferences.showToolHintProperty().get());
        tab.getToolPalette().setVerboseTooltips(preferences.verboseTooltipsProperty().get());
        tab.setAutoEditNode(preferences.autoEditNodeProperty().get());
        tab.setOnCloseRequest(event -> {
            if (!confirmClose(tab)) {
                event.consume();
            }
        });
        tabPane.getTabs().remove(welcomeTab);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }

    private void showWelcomeIfEmpty() {
        if (tabPane.getTabs().isEmpty()) {
            Platform.runLater(() -> {
                if (tabPane.getTabs().isEmpty()) {
                    tabPane.getTabs().add(welcomeTab);
                }
            });
        }
    }

    private Optional<DiagramTab> currentDiagramTab() {
        return tabPane.getSelectionModel().getSelectedItem() instanceof DiagramTab tab
                ? Optional.of(tab) : Optional.empty();
    }

    private List<DiagramTab> diagramTabs() {
        List<DiagramTab> result = new ArrayList<>();
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof DiagramTab diagramTab) {
                result.add(diagramTab);
            }
        }
        return result;
    }

    private void forEachDiagramTab(Consumer<DiagramTab> action) {
        diagramTabs().forEach(action);
    }

    private FileChooser chooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (lastDirectory != null && lastDirectory.isDirectory()) {
            chooser.setInitialDirectory(lastDirectory);
        }
        return chooser;
    }

    private static FileChooser.ExtensionFilter diagramFilter() {
        return new FileChooser.ExtensionFilter("UML diagrams (*" + DIAGRAM_EXTENSION + ")", "*" + DIAGRAM_EXTENSION);
    }

    /**
     * File > Open (RF02).
     */
    private void open() {
        FileChooser chooser = chooser("Open Diagram");
        chooser.getExtensionFilters().add(diagramFilter());
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            lastDirectory = file.getParentFile();
            openFile(file.toPath());
        }
    }

    /**
     * Opens a diagram file in a new tab, or selects its tab if it is already open.
     * The diagram gets the configured size, enlarged when its content is larger (RF38).
     */
    private void openFile(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        for (DiagramTab tab : diagramTabs()) {
            if (normalized.equals(tab.getFile())) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }
        try {
            Diagram diagram = DiagramDeserializer.readDiagram(Files.readString(normalized, StandardCharsets.UTF_8));
            fitSize(diagram);
            DiagramTab tab = addTab(diagram, normalized.getFileName().toString());
            tab.setFile(normalized);
            recentFiles.add(normalized);
        } catch (NoSuchFileException e) {
            recentFiles.remove(normalized);
            notificationArea.showError("File not found: " + normalized);
        } catch (IOException e) {
            notificationArea.showError("Could not read " + normalized.getFileName() + ": " + e.getMessage());
        } catch (InvalidDiagramException e) {
            notificationArea.showError("Could not open " + normalized.getFileName() + ": " + e.getMessage());
        }
    }

    private void fitSize(Diagram diagram) {
        DiagramRenderer renderer = new DiagramRenderer(new RenderContext(currentTheme(),
                preferences.fontSettingsProperty().get()));
        int width = preferences.getDiagramWidth();
        int height = preferences.getDiagramHeight();
        Optional<Rectangle> content = DiagramImage.contentBounds(diagram, renderer);
        if (content.isPresent()) {
            width = Math.max(width, (int) Math.ceil(content.get().maxX() + CONTENT_MARGIN));
            height = Math.max(height, (int) Math.ceil(content.get().maxY() + CONTENT_MARGIN));
        }
        diagram.setSize(width, height);
    }

    private void updateRecentMenu() {
        recentMenu.getItems().clear();
        for (Path path : recentFiles.getFiles()) {
            MenuItem item = new MenuItem(path.toString());
            item.setMnemonicParsing(false);
            item.setOnAction(event -> openFile(path));
            recentMenu.getItems().add(item);
        }
        if (recentMenu.getItems().isEmpty()) {
            MenuItem empty = new MenuItem("(no recent files)");
            empty.setDisable(true);
            recentMenu.getItems().add(empty);
        }
    }

    /**
     * File > Save (RF02): writes the diagram to its file, or asks for one.
     *
     * @return true if the diagram was saved
     */
    private boolean save(DiagramTab tab) {
        return tab.getFile() == null ? saveAs(tab) : write(tab, tab.getFile());
    }

    /**
     * File > Save As (RF02).
     *
     * @return true if the diagram was saved
     */
    private boolean saveAs(DiagramTab tab) {
        FileChooser chooser = chooser("Save Diagram");
        chooser.getExtensionFilters().add(diagramFilter());
        String name = tab.getFile() != null ? tab.getFile().getFileName().toString() : tab.getText();
        chooser.setInitialFileName(name.endsWith(DIAGRAM_EXTENSION) ? name : name + DIAGRAM_EXTENSION);
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return false;
        }
        lastDirectory = file.getParentFile();
        Path path = file.toPath();
        if (!path.getFileName().toString().toLowerCase().endsWith(DIAGRAM_EXTENSION)) {
            path = path.resolveSibling(path.getFileName() + DIAGRAM_EXTENSION);
        }
        return write(tab, path.toAbsolutePath().normalize());
    }

    private boolean write(DiagramTab tab, Path path) {
        try {
            Files.writeString(path, DiagramSerializer.toJson(tab.getDiagram()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            notificationArea.showError("Could not save " + path.getFileName() + ": " + e.getMessage());
            return false;
        }
        tab.setFile(path);
        tab.setModified(false);
        recentFiles.add(path);
        notificationArea.showConfirmation("Saved " + path.getFileName());
        return true;
    }

    /**
     * File > Close (RF02).
     */
    private void closeCurrent() {
        currentDiagramTab().ifPresent(tab -> {
            if (confirmClose(tab)) {
                tabPane.getTabs().remove(tab);
            }
        });
    }

    /**
     * Asks whether to save a modified diagram before it is closed.
     *
     * @return true if the tab may be closed
     */
    private boolean confirmClose(DiagramTab tab) {
        if (!tab.isModified()) {
            return true;
        }
        tabPane.getSelectionModel().select(tab);
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType discardButton = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Save the changes before closing?", saveButton,
                discardButton, ButtonType.CANCEL);
        alert.setHeaderText(tab.getText() + " has unsaved changes");
        prepare(alert);
        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.isEmpty() || answer.get() == ButtonType.CANCEL) {
            return false;
        }
        return answer.get() == discardButton || save(tab);
    }

    private boolean confirmCloseAll() {
        for (DiagramTab tab : diagramTabs()) {
            if (!confirmClose(tab)) {
                return false;
            }
        }
        return true;
    }

    /**
     * File > Exit (RF06).
     */
    private void exit() {
        if (confirmCloseAll()) {
            Platform.exit();
        }
    }

    /**
     * File > Duplicate (RF03): opens a copy of the current diagram in a new tab.
     */
    private void duplicate() {
        currentDiagramTab().ifPresent(tab -> {
            try {
                Diagram copy = DiagramDeserializer.readDiagram(DiagramSerializer.toJson(tab.getDiagram()));
                DiagramTab copyTab = addTab(copy, "Copy of " + tab.getText());
                copyTab.setModified(true);
            } catch (InvalidDiagramException e) {
                notificationArea.showError("Could not duplicate the diagram: " + e.getMessage());
            }
        });
    }

    /**
     * File > Export Image (RF04): the format is the one chosen in "Save as
     * type", or the one given by the extension typed in the file name.
     */
    private void exportImage() {
        Optional<DiagramTab> current = currentDiagramTab();
        if (current.isEmpty()) {
            return;
        }
        DiagramTab tab = current.get();
        FileChooser chooser = chooser("Export Image");
        for (ImageFormat format : ImageFormat.values()) {
            List<String> patterns = format.getExtensions().stream().map(extension -> "*." + extension).toList();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    format.getDescription() + " (" + String.join(", ", patterns) + ")", patterns));
        }
        String baseName = tab.getText().replaceFirst("\\.json$", "");
        chooser.setInitialFileName(baseName);
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        lastDirectory = file.getParentFile();
        int selectedFilter = chooser.getExtensionFilters().indexOf(chooser.getSelectedExtensionFilter());
        ImageFormat chosen = selectedFilter >= 0 ? ImageFormat.values()[selectedFilter] : ImageFormat.PNG;
        Path path = file.toPath();
        Optional<ImageFormat> typed = ImageFormat.fromFileName(path.getFileName().toString());
        ImageFormat format = typed.orElse(chosen);
        if (typed.isEmpty()) {
            path = path.resolveSibling(path.getFileName() + "." + format.getDefaultExtension());
        }
        try {
            if (DiagramImage.export(tab.getDiagram(), tab.getRenderContext().getFontSettings(), format, path)) {
                notificationArea.showConfirmation("Image exported to " + path.getFileName());
            } else {
                notificationArea.showError("The diagram is empty");
            }
        } catch (IOException e) {
            notificationArea.showError("Could not export the image: " + e.getMessage());
        }
    }

    private void zoom(Consumer<DiagramCanvas> action) {
        currentDiagramTab().ifPresent(tab -> action.accept(tab.getDiagramCanvas()));
        updateZoomItems();
    }

    private void updateZoomItems() {
        Optional<DiagramCanvas> canvas = currentDiagramTab().map(DiagramTab::getDiagramCanvas);
        zoomInItem.setDisable(canvas.map(c -> !c.canZoomIn()).orElse(true));
        zoomOutItem.setDisable(canvas.map(c -> !c.canZoomOut()).orElse(true));
        resetZoomItem.setDisable(canvas.isEmpty());
    }

    private void editDiagramSize() {
        DiagramSizeDialog dialog = new DiagramSizeDialog(preferences.getDiagramWidth(),
                preferences.getDiagramHeight(), UserPreferences.getDefaultDiagramWidth(),
                UserPreferences.getDefaultDiagramHeight());
        prepare(dialog).showAndWait().ifPresent(size -> {
            preferences.setDiagramSize((int) size.getWidth(), (int) size.getHeight());
            notificationArea.showConfirmation("Diagram size set to " + preferences.getDiagramWidth() + " x "
                    + preferences.getDiagramHeight() + " pixels");
        });
    }

    private void editFont() {
        FontDialog dialog = new FontDialog(preferences.fontSettingsProperty().get(), FontSettings.defaults());
        prepare(dialog).showAndWait().ifPresent(font -> {
            preferences.fontSettingsProperty().set(font);
            notificationArea.showConfirmation("Font set to " + font.family() + " " + (int) font.size());
        });
    }

    private void editNotificationDuration() {
        NotificationDurationDialog dialog = new NotificationDurationDialog(
                preferences.notificationDurationProperty().get(), UserPreferences.DEFAULT_NOTIFICATION_DURATION,
                UserPreferences.MAX_NOTIFICATION_DURATION);
        prepare(dialog).showAndWait().ifPresent(seconds -> {
            preferences.notificationDurationProperty().set(seconds);
            notificationArea.showConfirmation("Notification duration set to " + seconds + " seconds");
        });
    }

    private <T> Dialog<T> prepare(Dialog<T> dialog) {
        dialog.initOwner(stage);
        dialog.getDialogPane().getStylesheets().setAll(scene.getStylesheets());
        return dialog;
    }

    private Theme currentTheme() {
        return Theme.of(preferences.darkModeProperty().get());
    }

    private void applyTheme() {
        applyStylesheets();
        Theme theme = currentTheme();
        forEachDiagramTab(tab -> tab.setTheme(theme));
    }

    /**
     * Switches the interface between light and dark mode (RF40).
     */
    private void applyStylesheets() {
        scene.getStylesheets().setAll(getClass().getResource(BASE_STYLESHEET).toExternalForm());
        if (preferences.darkModeProperty().get()) {
            scene.getStylesheets().add(getClass().getResource(DARK_STYLESHEET).toExternalForm());
        }
    }
}
