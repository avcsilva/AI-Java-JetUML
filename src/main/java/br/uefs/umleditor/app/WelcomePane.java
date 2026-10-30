package br.uefs.umleditor.app;

import java.util.function.Consumer;

import br.uefs.umleditor.model.DiagramType;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Welcome screen, where the user chooses the type of a new diagram (RF01).
 */
public class WelcomePane extends VBox {

    public WelcomePane(Consumer<DiagramType> onNewDiagram) {
        setAlignment(Pos.CENTER);
        setSpacing(12);
        getStyleClass().add("welcome-pane");

        Label title = new Label("UML Editor");
        title.getStyleClass().add("welcome-title");
        Label subtitle = new Label("Create a new diagram:");
        getChildren().addAll(title, subtitle);

        for (DiagramType type : DiagramType.values()) {
            Button button = new Button(type.getDisplayName());
            button.getStyleClass().add("welcome-button");
            button.setPrefWidth(220);
            button.setOnAction(event -> onNewDiagram.accept(type));
            getChildren().add(button);
        }
    }
}
