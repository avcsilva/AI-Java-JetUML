package br.uefs.umleditor.app;

import java.util.prefs.Preferences;

import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.render.FontSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 * User settings shared by all diagram tabs and persisted between executions.
 */
public class UserPreferences {

    public static final int DEFAULT_NOTIFICATION_DURATION = 5;
    public static final int MAX_NOTIFICATION_DURATION = 10;

    private static final String SHOW_GRID = "showGrid";
    private static final String SHOW_TOOL_HINT = "showToolHint";
    private static final String VERBOSE_TOOLTIPS = "verboseTooltips";
    private static final String DARK_MODE = "darkMode";
    private static final String NOTIFICATION_DURATION = "notificationDuration";
    private static final String DIAGRAM_WIDTH = "diagramWidth";
    private static final String DIAGRAM_HEIGHT = "diagramHeight";
    private static final String FONT_FAMILY = "fontFamily";
    private static final String FONT_SIZE = "fontSize";

    private final Preferences store = Preferences.userNodeForPackage(UserPreferences.class);

    private final BooleanProperty showGrid = booleanSetting(SHOW_GRID, true);
    private final BooleanProperty showToolHint = booleanSetting(SHOW_TOOL_HINT, false);
    private final BooleanProperty verboseTooltips = booleanSetting(VERBOSE_TOOLTIPS, false);
    private final BooleanProperty darkMode = booleanSetting(DARK_MODE, false);
    private final IntegerProperty notificationDuration =
            integerSetting(NOTIFICATION_DURATION, DEFAULT_NOTIFICATION_DURATION);
    private final ObjectProperty<FontSettings> fontSettings = fontSetting();

    private BooleanProperty booleanSetting(String key, boolean defaultValue) {
        BooleanProperty property = new SimpleBooleanProperty(store.getBoolean(key, defaultValue));
        property.addListener((observable, oldValue, newValue) -> store.putBoolean(key, newValue));
        return property;
    }

    private IntegerProperty integerSetting(String key, int defaultValue) {
        IntegerProperty property = new SimpleIntegerProperty(store.getInt(key, defaultValue));
        property.addListener((observable, oldValue, newValue) -> store.putInt(key, newValue.intValue()));
        return property;
    }

    private ObjectProperty<FontSettings> fontSetting() {
        FontSettings defaults = FontSettings.defaults();
        ObjectProperty<FontSettings> property = new SimpleObjectProperty<>(new FontSettings(
                store.get(FONT_FAMILY, defaults.family()), store.getDouble(FONT_SIZE, defaults.size())));
        property.addListener((observable, oldValue, newValue) -> {
            store.put(FONT_FAMILY, newValue.family());
            store.putDouble(FONT_SIZE, newValue.size());
        });
        return property;
    }

    public BooleanProperty showGridProperty() {
        return showGrid;
    }

    public BooleanProperty showToolHintProperty() {
        return showToolHint;
    }

    public BooleanProperty verboseTooltipsProperty() {
        return verboseTooltips;
    }

    public BooleanProperty darkModeProperty() {
        return darkMode;
    }

    /**
     * @return the time, in seconds, that notifications stay visible (RF42).
     */
    public IntegerProperty notificationDurationProperty() {
        return notificationDuration;
    }

    /**
     * @return the font family and size of diagram text (RF39).
     */
    public ObjectProperty<FontSettings> fontSettingsProperty() {
        return fontSettings;
    }

    public int getDiagramWidth() {
        return Diagram.clampSize(store.getInt(DIAGRAM_WIDTH, getDefaultDiagramWidth()));
    }

    public int getDiagramHeight() {
        return Diagram.clampSize(store.getInt(DIAGRAM_HEIGHT, getDefaultDiagramHeight()));
    }

    public void setDiagramSize(int width, int height) {
        store.putInt(DIAGRAM_WIDTH, Diagram.clampSize(width));
        store.putInt(DIAGRAM_HEIGHT, Diagram.clampSize(height));
    }

    /**
     * The default diagram size is the size of the visible area of the primary
     * screen, so that it is proportional to the screen (RF38, RNF01).
     */
    public static int getDefaultDiagramWidth() {
        return Diagram.clampSize((int) screenBounds().getWidth());
    }

    public static int getDefaultDiagramHeight() {
        return Diagram.clampSize((int) screenBounds().getHeight());
    }

    private static Rectangle2D screenBounds() {
        return Screen.getPrimary().getVisualBounds();
    }
}
