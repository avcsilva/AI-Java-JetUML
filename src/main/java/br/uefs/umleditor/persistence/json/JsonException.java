package br.uefs.umleditor.persistence.json;

/**
 * Thrown when a text is not valid JSON.
 */
public class JsonException extends Exception {

    public JsonException(String message) {
        super(message);
    }
}
