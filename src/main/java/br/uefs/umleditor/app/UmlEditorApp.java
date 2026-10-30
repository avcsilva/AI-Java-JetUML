package br.uefs.umleditor.app;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point of the UML diagram editor.
 */
public class UmlEditorApp extends Application {

    @Override
    public void start(Stage stage) {
        new MainWindow(stage, new UserPreferences()).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
