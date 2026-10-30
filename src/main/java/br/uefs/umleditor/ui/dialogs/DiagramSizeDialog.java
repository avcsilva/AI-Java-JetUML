package br.uefs.umleditor.ui.dialogs;

import br.uefs.umleditor.model.Diagram;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;

/**
 * View > Set Diagram Size: width and height in pixels, from 250 to 4000,
 * with Restore Defaults (RF38).
 */
public class DiagramSizeDialog extends Dialog<Dimension2D> {

    public DiagramSizeDialog(int width, int height, int defaultWidth, int defaultHeight) {
        setTitle("Set Diagram Size");
        setHeaderText("Size of new diagrams, in pixels (" + Diagram.MIN_SIZE + " to " + Diagram.MAX_SIZE + ")");

        Spinner<Integer> widthSpinner = SpinnerSupport.integerSpinner(Diagram.MIN_SIZE, Diagram.MAX_SIZE, width);
        Spinner<Integer> heightSpinner = SpinnerSupport.integerSpinner(Diagram.MIN_SIZE, Diagram.MAX_SIZE, height);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Width:"), widthSpinner);
        grid.addRow(1, new Label("Height:"), heightSpinner);
        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(SpinnerSupport.RESTORE_DEFAULTS, ButtonType.OK, ButtonType.CANCEL);

        SpinnerSupport.keepOpenOn(this, SpinnerSupport.RESTORE_DEFAULTS, () -> {
            widthSpinner.getValueFactory().setValue(defaultWidth);
            heightSpinner.getValueFactory().setValue(defaultHeight);
        });
        setResultConverter(button -> button == ButtonType.OK
                ? new Dimension2D(SpinnerSupport.commit(widthSpinner), SpinnerSupport.commit(heightSpinner))
                : null);
    }
}
