package br.uefs.umleditor.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.value.ObservableIntegerValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Temporary notifications shown in the lower-left corner of the window (RF41):
 * errors in red and confirmations in green. They disappear after the duration
 * configured by the user, and a duration of 0 disables them (RF42).
 */
public class NotificationArea extends VBox {

    private static final Duration FADE_DURATION = Duration.millis(400);

    private final ObservableIntegerValue durationInSeconds;

    public NotificationArea(ObservableIntegerValue durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
        setSpacing(6);
        setPadding(new Insets(12));
        setAlignment(Pos.BOTTOM_LEFT);
        setMouseTransparent(true);
        setPickOnBounds(false);
    }

    public void showError(String message) {
        show(message, "notification-error");
    }

    public void showConfirmation(String message) {
        show(message, "notification-confirmation");
    }

    private void show(String message, String styleClass) {
        int seconds = durationInSeconds.get();
        if (seconds <= 0) {
            return;
        }
        Label notification = new Label(message);
        notification.getStyleClass().addAll("notification", styleClass);
        notification.setWrapText(true);
        notification.setMaxWidth(400);
        getChildren().add(notification);

        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, notification);
        fadeOut.setToValue(0);
        SequentialTransition lifetime =
                new SequentialTransition(new PauseTransition(Duration.seconds(seconds)), fadeOut);
        lifetime.setOnFinished(event -> getChildren().remove(notification));
        lifetime.play();
    }
}
