package br.uefs.umleditor.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;

/**
 * View > Notification Duration: from 0 to 10 seconds, where 0 disables
 * notifications, with Restore Default (RF42).
 */
public class NotificationDurationDialog extends Dialog<Integer> {

    public NotificationDurationDialog(int duration, int defaultDuration, int maxDuration) {
        setTitle("Notification Duration");
        setHeaderText("Time notifications stay visible (0 disables notifications)");

        Spinner<Integer> spinner = SpinnerSupport.integerSpinner(0, maxDuration, duration);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Seconds:"), spinner);
        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(SpinnerSupport.RESTORE_DEFAULT, ButtonType.OK, ButtonType.CANCEL);

        SpinnerSupport.keepOpenOn(this, SpinnerSupport.RESTORE_DEFAULT,
                () -> spinner.getValueFactory().setValue(defaultDuration));
        setResultConverter(button -> button == ButtonType.OK ? SpinnerSupport.commit(spinner) : null);
    }
}
