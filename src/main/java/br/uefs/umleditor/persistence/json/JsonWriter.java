package br.uefs.umleditor.persistence.json;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Writes plain Java values ({@link Map}, {@link List}, {@link String},
 * {@link Number}, {@link Boolean} and null) as indented JSON text.
 */
public final class JsonWriter {

    private static final String INDENT = "  ";

    private JsonWriter() {
    }

    public static String write(Object value) {
        StringBuilder result = new StringBuilder();
        write(value, result, 0);
        return result.append('\n').toString();
    }

    private static void write(Object value, StringBuilder out, int depth) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String text) {
            writeString(text, out);
        } else if (value instanceof Boolean bool) {
            out.append(bool);
        } else if (value instanceof Number number) {
            writeNumber(number, out);
        } else if (value instanceof Map<?, ?> map) {
            writeObject(map, out, depth);
        } else if (value instanceof List<?> list) {
            writeArray(list, out, depth);
        } else {
            throw new IllegalArgumentException("Cannot write " + value.getClass().getSimpleName() + " as JSON");
        }
    }

    private static void writeObject(Map<?, ?> map, StringBuilder out, int depth) {
        if (map.isEmpty()) {
            out.append("{}");
            return;
        }
        out.append("{\n");
        Iterator<? extends Map.Entry<?, ?>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<?, ?> entry = entries.next();
            out.append(INDENT.repeat(depth + 1));
            writeString(String.valueOf(entry.getKey()), out);
            out.append(": ");
            write(entry.getValue(), out, depth + 1);
            out.append(entries.hasNext() ? ",\n" : "\n");
        }
        out.append(INDENT.repeat(depth)).append('}');
    }

    /**
     * Arrays of simple values are written on one line; arrays of objects or
     * arrays get one element per line.
     */
    private static void writeArray(List<?> list, StringBuilder out, int depth) {
        if (list.isEmpty()) {
            out.append("[]");
            return;
        }
        boolean simple = list.stream().noneMatch(item -> item instanceof Map || item instanceof List);
        if (simple) {
            out.append('[');
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    out.append(", ");
                }
                write(list.get(i), out, depth);
            }
            out.append(']');
            return;
        }
        out.append("[\n");
        for (int i = 0; i < list.size(); i++) {
            out.append(INDENT.repeat(depth + 1));
            write(list.get(i), out, depth + 1);
            out.append(i < list.size() - 1 ? ",\n" : "\n");
        }
        out.append(INDENT.repeat(depth)).append(']');
    }

    private static void writeNumber(Number number, StringBuilder out) {
        double value = number.doubleValue();
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("JSON cannot represent " + value);
        }
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            out.append((long) value);
        } else {
            out.append(value);
        }
    }

    private static void writeString(String text, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
