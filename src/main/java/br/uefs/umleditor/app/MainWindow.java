package br.uefs.umleditor.app;

import java.util.Optional;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.render.FontSettings;
import br.uefs.umleditor.render.RenderContext;
import br.uefs.umleditor.render.Theme;
import br.uefs.umleditor.ui.DiagramCanvas;
import br.uefs.umleditor.ui.DiagramTab;
import br.uefs.umleditor.ui.NotificationArea;
import br.uefs.umleditor.ui.dialogs.DiagramSizeDialog;
import br.uefs.umleditor.ui.dialogs.FontDialog;
import br.uefs.umleditor.ui.dialogs.NotificationDurationDialog;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Scene;
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
import javafx.stage.Stage;

/**
 * Main window: menu bar, diagram tabs and notification area.
 */
public class MainWindow {

    private static final String BASE_STYLESHEET = "/br/uefs/umleditor/base.css";
    private static final String DARK_STYLESHEET = "/br/uefs/umleditor/dark.css";

    private final Stage stage;
    private final UserPreferences preferences;
    private final TabPane tabPane = new TabPane();
    private final Tab welcomeTab;
    private final NotificationArea notificationArea;
    private final Scene scene;
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
        tabPane.getTabs().addListener((javafx.collections.ListChangeListener<Tab>) change -> showWelcomeIfEmpty());
        tabPane.getSelectionModel().selectedItemProperty().addListener(observable -> updateZoomItems());

        StackPane center = new StackPane(tabPane, notificationArea);
        StackPane.setAlignment(notificationArea, javafx.geometry.Pos.BOTTOM_LEFT);

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
        preferences.fontSettingsProperty().addListener(observable -> forEachDiagramTab(
                tab -> tab.setFontSettings(preferences.fontSettingsProperty().get())));

        stage.setTitle("UML Editor");
        stage.setScene(scene);
        updateZoomItems();
    }

    public void show() {
        stage.show();
    }

    private MenuBar createMenuBar() {
        return new MenuBar(createFileMenu(), createEditMenu(), createViewMenu());
    }

    private Menu createFileMenu() {
        Menu newMenu = new Menu("New");
        for (DiagramType type : DiagramType.values()) {
            MenuItem item = new MenuItem(type.getDisplayName());
            item.setOnAction(event -> newDiagram(type));
            newMenu.getItems().add(item);
        }
        MenuItem exit = new MenuItem("Exit");
        exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
        exit.setOnAction(event -> Platform.exit());
        return new Menu("File", null, newMenu, new SeparatorMenuItem(), exit);
    }

    private Menu createEditMenu() {
        MenuItem selectAll = new MenuItem("Select All");
        selectAll.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN));
        selectAll.setOnAction(event -> currentDiagramTab().ifPresent(DiagramTab::selectAll));
        MenuItem delete = new MenuItem("Delete");
        delete.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        delete.setOnAction(event -> currentDiagramTab().ifPresent(DiagramTab::deleteSelection));
        return new Menu("Edit", null, selectAll, delete);
    }

    private Menu createViewMenu() {
        CheckMenuItem showGrid = checkItem("Show Grid", preferences.showGridProperty());
        CheckMenuItem showToolHint = checkItem("Show Tool Hint", preferences.showToolHintProperty());
        CheckMenuItem verboseTooltips = checkItem("Verbose Tooltips", preferences.verboseTooltipsProperty());

        zoomInItem = new MenuItem("Zoom In");
        zoomInItem.setAccelerator(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN));
        zoomInItem.setOnAction(event -> zoom(DiagramCanvas::zoomIn));
        zoomOutItem = new MenuItem("Zoom Out");
        zoomOutItem.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN));
        zoomOutItem.setOnAction(event -> zoom(DiagramCanvas::zoomOut));
        resetZoomItem = new MenuItem("Reset Zoom");
        resetZoomItem.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN));
        resetZoomItem.setOnAction(event -> zoom(DiagramCanvas::resetZoom));

        MenuItem diagramSize = new MenuItem("Set Diagram Size...");
        diagramSize.setOnAction(event -> editDiagramSize());
        MenuItem changeFont = new MenuItem("Change Font...");
        changeFont.setOnAction(event -> editFont());
        CheckMenuItem darkMode = checkItem("Dark Mode", preferences.darkModeProperty());
        MenuItem notificationDuration = new MenuItem("Notification Duration...");
        notificationDuration.setOnAction(event -> editNotificationDuration());

        return new Menu("View", null, showGrid, showToolHint, verboseTooltips, new SeparatorMenuItem(),
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
        RenderContext renderContext = new RenderContext(currentTheme(), preferences.fontSettingsProperty().get());
        DiagramTab tab = new DiagramTab(diagram, type.getDisplayName(), renderContext,
                preferences.showGridProperty().get());
        tab.getToolPalette().setShowToolHint(preferences.showToolHintProperty().get());
        tab.getToolPalette().setVerboseTooltips(preferences.verboseTooltipsProperty().get());
        tabPane.getTabs().remove(welcomeTab);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
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

    private void forEachDiagramTab(java.util.function.Consumer<DiagramTab> action) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof DiagramTab diagramTab) {
                action.accept(diagramTab);
            }
        }
    }

    private void zoom(java.util.function.Consumer<DiagramCanvas> action) {
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

    public NotificationArea getNotificationArea() {
        return notificationArea;
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
