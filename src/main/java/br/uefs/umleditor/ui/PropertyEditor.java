package br.uefs.umleditor.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.uefs.umleditor.command.ChangePropertyOperation;
import br.uefs.umleditor.command.CompoundOperation;
import br.uefs.umleditor.command.DiagramOperation;
import br.uefs.umleditor.model.DiagramElement;
import br.uefs.umleditor.model.Property;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Properties panel of a diagram element (RF20, RF22). The fields depend on
 * the element; changes are shown in the diagram as they are typed. Tab and
 * Shift+Tab move between the fields, Esc closes the panel, and Ctrl+Q inserts
 * the stereotype delimiters « » in a text field (RF23).
 */
public final class PropertyEditor {

    private static final int TEXT_COLUMNS = 28;
    private static final int TEXT_ROWS = 4;

    private PropertyEditor() {
    }

    /**
     * Opens the panel and waits until it is closed.
     *
     * @param anchor where to place the panel, in screen coordinates, or null to center it on the owner
     * @param onChange called after each change, to redraw the diagram
     * @return one operation with every change made while the panel was open, or empty if nothing changed
     */
    public static Optional<DiagramOperation> edit(DiagramElement element, Window owner, Point2D anchor,
                                                  List<String> stylesheets, Runnable onChange) {
        List<Property> properties = element.getProperties();
        if (properties.isEmpty()) {
            return Optional.empty();
        }
        List<Object> initialValues = new ArrayList<>();
        for (Property property : properties) {
            initialValues.add(property.get());
        }

        Stage stage = new Stage(StageStyle.UTILITY);
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Properties");
        stage.setResizable(false);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.getStyleClass().add("property-editor");
        List<Control> inputs = new ArrayList<>();
        for (int row = 0; row < properties.size(); row++) {
            Property property = properties.get(row);
            Control input = createInput(property, onChange);
            inputs.add(input);
            grid.addRow(row, new Label(property.getName() + ":"), input);
        }
        for (Control input : inputs) {
            if (input instanceof TextArea area) {
                area.addEventFilter(KeyEvent.KEY_PRESSED, event -> moveFocusOnTab(event, inputs, area));
            }
            if (input instanceof TextInputControl text) {
                text.addEventFilter(KeyEvent.KEY_PRESSED, event -> insertStereotypeOnCtrlQ(event, text));
            }
        }

        Scene scene = new Scene(grid);
        scene.getStylesheets().setAll(stylesheets);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                stage.close();
            }
        });
        stage.setScene(scene);
        stage.setOnShown(event -> {
            place(stage, owner, anchor);
            inputs.get(0).requestFocus();
        });
        stage.showAndWait();

        List<DiagramOperation> changes = new ArrayList<>();
        for (int i = 0; i < properties.size(); i++) {
            Object current = properties.get(i).get();
            if (!Objects.equals(initialValues.get(i), current)) {
                changes.add(new ChangePropertyOperation(properties.get(i), initialValues.get(i), current));
            }
        }
        return changes.isEmpty() ? Optional.empty() : Optional.of(new CompoundOperation(changes));
    }

    private static Control createInput(Property property, Runnable onChange) {
        return switch (property.getKind()) {
            case TEXT -> {
                TextField field = new TextField((String) property.get());
                field.setPrefColumnCount(TEXT_COLUMNS);
                field.textProperty().addListener((observable, oldValue, newValue) -> update(property, newValue,
                        onChange));
                yield field;
            }
            case MULTILINE_TEXT -> {
                TextArea area = new TextArea((String) property.get());
                area.setPrefColumnCount(TEXT_COLUMNS);
                area.setPrefRowCount(TEXT_ROWS);
                area.textProperty().addListener((observable, oldValue, newValue) -> update(property, newValue,
                        onChange));
                yield area;
            }
            case CHOICE -> {
                ComboBox<Object> box = new ComboBox<>();
                box.getItems().setAll(property.getChoices());
                box.setValue(property.get());
                box.valueProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        update(property, newValue, onChange);
                    }
                });
                yield box;
            }
            case BOOLEAN -> {
                CheckBox box = new CheckBox();
                box.setSelected((Boolean) property.get());
                box.selectedProperty().addListener((observable, oldValue, newValue) -> update(property, newValue,
                        onChange));
                yield box;
            }
        };
    }

    private static void update(Property property, Object value, Runnable onChange) {
        property.set(value);
        onChange.run();
    }

    /**
     * Tab inside a multi-line field moves to the next field instead of
     * inserting a tab character; Shift+Tab moves to the previous one.
     */
    private static void moveFocusOnTab(KeyEvent event, List<Control> inputs, Control current) {
        if (event.getCode() != KeyCode.TAB || event.isControlDown()) {
            return;
        }
        event.consume();
        int step = event.isShiftDown() ? -1 : 1;
        int index = (inputs.indexOf(current) + step + inputs.size()) % inputs.size();
        inputs.get(index).requestFocus();
    }

    private static void insertStereotypeOnCtrlQ(KeyEvent event, TextInputControl text) {
        if (event.getCode() != KeyCode.Q || !event.isShortcutDown()) {
            return;
        }
        event.consume();
        StereotypeInsertion.Result result = StereotypeInsertion.insert(text.getText(),
                text.getSelection().getStart(), text.getSelection().getEnd());
        text.setText(result.text());
        text.positionCaret(result.caret());
    }

    /**
     * Places the panel next to the element, keeping it on the screen.
     */
    private static void place(Stage stage, Window owner, Point2D anchor) {
        double x = anchor != null ? anchor.getX() : owner.getX() + (owner.getWidth() - stage.getWidth()) / 2;
        double y = anchor != null ? anchor.getY() : owner.getY() + (owner.getHeight() - stage.getHeight()) / 2;
        List<Screen> screens = Screen.getScreensForRectangle(x, y, 1, 1);
        Rectangle2D area = (screens.isEmpty() ? Screen.getPrimary() : screens.get(0)).getVisualBounds();
        stage.setX(Math.max(area.getMinX(), Math.min(x, area.getMaxX() - stage.getWidth())));
        stage.setY(Math.max(area.getMinY(), Math.min(y, area.getMaxY() - stage.getHeight())));
    }
}
