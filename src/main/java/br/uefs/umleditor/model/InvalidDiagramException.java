package br.uefs.umleditor.model;

/**
 * Thrown when diagram content read from a file or from the clipboard does
 * not form a valid diagram. The message explains the problem to the user.
 */
public class InvalidDiagramException extends Exception {

    public InvalidDiagramException(String message) {
        super(message);
    }
}
