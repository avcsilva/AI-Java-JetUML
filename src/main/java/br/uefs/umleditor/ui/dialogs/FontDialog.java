package br.uefs.umleditor.ui.dialogs;

import br.uefs.umleditor.render.FontSettings;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

/**
 * View > Change Font: font family and size of the diagram text, with
 * Restore Default (RF39).
 */
public class FontDialog extends Dialog<FontSettings> {

    public FontDialog(FontSettings current, FontSettings defaults) {
        setTitle("Change Font");
        setHeaderText("Font used to draw diagram text");

        ComboBox<String> family = new ComboBox<>();
        family.getItems().setAll(Font.getFamilies());
        family.setValue(current.family());
        family.setPrefWidth(220);
        Spinner<Integer> size = SpinnerSupport.integerSpinner((int) FontSettings.MIN_SIZE, (int) FontSettings.MAX_SIZE,
                (int) current.size());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Font:"), family);
        grid.addRow(1, new Label("Size:"), size);
        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(SpinnerSupport.RESTORE_DEFAULT, ButtonType.OK, ButtonType.CANCEL);

        SpinnerSupport.keepOpenOn(this, SpinnerSupport.RESTORE_DEFAULT, () -> {
            family.setValue(defaults.family());
            size.getValueFactory().setValue((int) defaults.size());
        });
        setResultConverter(button -> button == ButtonType.OK && family.getValue() != null
                ? new FontSettings(family.getValue(), SpinnerSupport.commit(size))
                : null);
    }
}
