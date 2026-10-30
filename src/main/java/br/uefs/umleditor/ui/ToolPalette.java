package br.uefs.umleditor.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import br.uefs.umleditor.model.DiagramType;
import br.uefs.umleditor.model.ToolType;
import br.uefs.umleditor.render.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Side tool bar with the tools of a diagram type, in the order of RF07.
 * Supports tool hints and verbose tooltips (RF10) and builds the equivalent
 * popup menu (RF09).
 */
public class ToolPalette extends VBox {

    private final DiagramType diagramType;
    private final Consumer<ToolType> actionHandler;
    private final List<ButtonBase> buttons = new ArrayList<>();
    private final List<Canvas> icons = new ArrayList<>();
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final ObjectProperty<ToolType> selectedTool = new SimpleObjectProperty<>(ToolType.SELECT);
    private Theme theme;

    /**
     * @param actionHandler receives action tools (such as To Clipboard) when they are chosen
     */
    public ToolPalette(DiagramType diagramType, Theme theme, Consumer<ToolType> actionHandler) {
        this.diagramType = diagramType;
        this.actionHandler = actionHandler;
        this.theme = theme;
        getStyleClass().add("tool-palette");
        setFillWidth(true);

        List<ToolType> tools = diagramType.getTools();
        for (int i = 0; i < tools.size(); i++) {
            ToolType tool = tools.get(i);
            Canvas icon = ToolIconPainter.createIcon(tool, theme);
            ButtonBase button = tool.isAction() ? new Button() : new ToggleButton();
            button.setGraphic(icon);
            button.setText(tool.getDisplayName());
            button.setAlignment(Pos.CENTER_LEFT);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setFocusTraversable(false);
            button.getStyleClass().add("tool-button");
            int index = i;
            if (button instanceof ToggleButton toggle) {
                toggle.setToggleGroup(toggleGroup);
                toggle.setSelected(tool == ToolType.SELECT);
            }
            button.setOnAction(event -> selectTool(index));
            buttons.add(button);
            icons.add(icon);
        }
        getChildren().addAll(buttons);
        // A toggle button must never stay unselected: clicking the active tool keeps it active.
        toggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }
        });
        setShowToolHint(false);
        setVerboseTooltips(false);
    }

    public ReadOnlyObjectProperty<ToolType> selectedToolProperty() {
        return selectedTool;
    }

    public ToolType getSelectedTool() {
        return selectedTool.get();
    }

    /**
     * Chooses the tool at the given position of the bar, as done by a click,
     * a keyboard shortcut (RF08) or the popup menu (RF09).
     */
    public void selectTool(int index) {
        ToolType tool = diagramType.getTools().get(index);
        if (tool.isAction()) {
            actionHandler.accept(tool);
            return;
        }
        ((ToggleButton) buttons.get(index)).setSelected(true);
        selectedTool.set(tool);
    }

    /**
     * Shows the name of each tool next to its icon (View > Show Tool Hint).
     */
    public void setShowToolHint(boolean show) {
        for (ButtonBase button : buttons) {
            button.setContentDisplay(show ? ContentDisplay.LEFT : ContentDisplay.GRAPHIC_ONLY);
        }
    }

    /**
     * Switches tooltips between the element type and an extended UML description
     * (View > Verbose Tooltips).
     */
    public void setVerboseTooltips(boolean verbose) {
        List<ToolType> tools = diagramType.getTools();
        for (int i = 0; i < tools.size(); i++) {
            ToolType tool = tools.get(i);
            String text = verbose ? tool.getDisplayName() + ": " + tool.getDescription() : tool.getDisplayName();
            Tooltip tooltip = new Tooltip(text);
            tooltip.setShowDelay(Duration.millis(400));
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(320);
            buttons.get(i).setTooltip(tooltip);
        }
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        List<ToolType> tools = diagramType.getTools();
        for (int i = 0; i < tools.size(); i++) {
            ToolIconPainter.paint(icons.get(i), tools.get(i), theme);
        }
    }

    /**
     * Builds a popup menu with the same tools as the bar (RF09).
     */
    public ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();
        List<ToolType> tools = diagramType.getTools();
        for (int i = 0; i < tools.size(); i++) {
            ToolType tool = tools.get(i);
            MenuItem item = new MenuItem(tool.getDisplayName(), ToolIconPainter.createIcon(tool, theme));
            int index = i;
            item.setOnAction(event -> selectTool(index));
            menu.getItems().add(item);
        }
        return menu;
    }
}
