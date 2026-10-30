package br.uefs.umleditor.model;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An editable property of a diagram element, shown as a field of the
 * properties panel (RF20, RF22).
 */
public final class Property {

    /**
     * How the value is edited.
     */
    public enum Kind {
        /** A single line of text. */
        TEXT,
        /** Text with several lines. */
        MULTILINE_TEXT,
        /** One value from a fixed list. */
        CHOICE,
        /** A yes/no option. */
        BOOLEAN
    }

    private final String name;
    private final Kind kind;
    private final Supplier<Object> getter;
    private final Consumer<Object> setter;
    private final List<?> choices;

    private Property(String name, Kind kind, Supplier<Object> getter, Consumer<Object> setter, List<?> choices) {
        this.name = name;
        this.kind = kind;
        this.getter = getter;
        this.setter = setter;
        this.choices = List.copyOf(choices);
    }

    public static Property text(String name, Supplier<String> getter, Consumer<String> setter) {
        return new Property(name, Kind.TEXT, getter::get, value -> setter.accept((String) value), List.of());
    }

    public static Property multilineText(String name, Supplier<String> getter, Consumer<String> setter) {
        return new Property(name, Kind.MULTILINE_TEXT, getter::get, value -> setter.accept((String) value),
                List.of());
    }

    public static Property bool(String name, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return new Property(name, Kind.BOOLEAN, getter::get, value -> setter.accept((Boolean) value), List.of());
    }

    public static <T> Property choice(String name, List<T> choices, Supplier<T> getter, Consumer<T> setter) {
        @SuppressWarnings("unchecked")
        Consumer<Object> typedSetter = value -> setter.accept((T) value);
        return new Property(name, Kind.CHOICE, getter::get, typedSetter, choices);
    }

    public String getName() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * @return the values a choice property accepts, in display order
     */
    public List<?> getChoices() {
        return choices;
    }

    public Object get() {
        return getter.get();
    }

    public void set(Object value) {
        setter.accept(value);
    }
}
