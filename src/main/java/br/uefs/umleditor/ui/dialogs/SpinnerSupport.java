package br.uefs.umleditor.ui.dialogs;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Helpers shared by the settings dialogs.
 */
final class SpinnerSupport {

    static final ButtonType RESTORE_DEFAULTS = new ButtonType("Restore Defaults", ButtonBar.ButtonData.LEFT);
    static final ButtonType RESTORE_DEFAULT = new ButtonType("Restore Default", ButtonBar.ButtonData.LEFT);

    private SpinnerSupport() {
    }

    static Spinner<Integer> integerSpinner(int min, int max, int value) {
        Spinner<Integer> spinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value));
        spinner.setEditable(true);
        spinner.setPrefWidth(110);
        spinner.focusedProperty().addListener((observable, wasFocused, focused) -> {
            if (!focused) {
                commit(spinner);
            }
        });
        return spinner;
    }

    /**
     * Applies the text typed in the spinner editor, limiting it to the allowed range.
     * Text that is not a number restores the previous value.
     */
    static int commit(Spinner<Integer> spinner) {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
        String text = spinner.getEditor().getText().trim();
        try {
            int value = Integer.parseInt(text);
            factory.setValue(Math.max(factory.getMin(), Math.min(factory.getMax(), value)));
        } catch (NumberFormatException e) {
            // keeps the previous value
        }
        spinner.getEditor().setText(String.valueOf(factory.getValue()));
        return factory.getValue();
    }

    /**
     * Makes a button run an action without closing the dialog.
     */
    static void keepOpenOn(Dialog<?> dialog, ButtonType buttonType, Runnable action) {
        dialog.getDialogPane().lookupButton(buttonType).addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            action.run();
            event.consume();
        });
    }
}
