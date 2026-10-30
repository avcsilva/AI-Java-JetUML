package br.uefs.umleditor.persistence.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads JSON text into plain Java values: objects become {@link Map} (keeping
 * the order of the keys), arrays become {@link List}, and the other values
 * become {@link String}, {@link Long} (integers), {@link Double}, {@link Boolean}
 * or null.
 */
public final class JsonParser {

    private final String text;
    private int position;

    private JsonParser(String text) {
        this.text = text;
    }

    public static Object parse(String text) throws JsonException {
        JsonParser parser = new JsonParser(text);
        parser.skipWhitespace();
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.position < text.length()) {
            throw parser.error("Unexpected content after the end of the document");
        }
        return value;
    }

    private JsonException error(String message) {
        return new JsonException(message + " at position " + position);
    }

    private void skipWhitespace() {
        while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
            position++;
        }
    }

    private char peek() throws JsonException {
        if (position >= text.length()) {
            throw error("Unexpected end of the document");
        }
        return text.charAt(position);
    }

    private void expect(char expected) throws JsonException {
        if (peek() != expected) {
            throw error("Expected '" + expected + "'");
        }
        position++;
    }

    private Object readValue() throws JsonException {
        char c = peek();
        return switch (c) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> {
                if (c == '-' || Character.isDigit(c)) {
                    yield readNumber();
                }
                throw error("Unexpected character '" + c + "'");
            }
        };
    }

    private Map<String, Object> readObject() throws JsonException {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            position++;
            return result;
        }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            result.put(key, readValue());
            skipWhitespace();
            if (peek() == ',') {
                position++;
            } else {
                expect('}');
                return result;
            }
        }
    }

    private List<Object> readArray() throws JsonException {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            position++;
            return result;
        }
        while (true) {
            skipWhitespace();
            result.add(readValue());
            skipWhitespace();
            if (peek() == ',') {
                position++;
            } else {
                expect(']');
                return result;
            }
        }
    }

    private String readString() throws JsonException {
        expect('"');
        StringBuilder result = new StringBuilder();
        while (true) {
            char c = peek();
            position++;
            if (c == '"') {
                return result.toString();
            }
            if (c != '\\') {
                result.append(c);
                continue;
            }
            char escaped = peek();
            position++;
            switch (escaped) {
                case '"', '\\', '/' -> result.append(escaped);
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'u' -> {
                    if (position + 4 > text.length()) {
                        throw error("Incomplete unicode escape");
                    }
                    try {
                        result.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                    } catch (NumberFormatException e) {
                        throw error("Invalid unicode escape");
                    }
                    position += 4;
                }
                default -> throw error("Invalid escape '\\" + escaped + "'");
            }
        }
    }

    private Object readLiteral(String literal, Object value) throws JsonException {
        if (!text.startsWith(literal, position)) {
            throw error("Expected " + literal);
        }
        position += literal.length();
        return value;
    }

    private Object readNumber() throws JsonException {
        int start = position;
        if (peek() == '-') {
            position++;
        }
        boolean integer = true;
        while (position < text.length()) {
            char c = text.charAt(position);
            if (Character.isDigit(c)) {
                position++;
            } else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                integer = false;
                position++;
            } else {
                break;
            }
        }
        String number = text.substring(start, position);
        try {
            return integer ? (Object) Long.parseLong(number) : (Object) Double.parseDouble(number);
        } catch (NumberFormatException e) {
            position = start;
            throw error("Invalid number '" + number + "'");
        }
    }
}
