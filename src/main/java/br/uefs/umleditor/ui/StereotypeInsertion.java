package br.uefs.umleditor.ui;

/**
 * Inserts the UML stereotype delimiters « » in a text and places the caret
 * between them (RF23). The selected text, if any, is replaced.
 */
public final class StereotypeInsertion {

    public static final String OPEN = "«";
    public static final String CLOSE = "»";

    /**
     * The text after the insertion and where the caret goes.
     */
    public record Result(String text, int caret) {
    }

    private StereotypeInsertion() {
    }

    public static Result insert(String text, int selectionStart, int selectionEnd) {
        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        String result = text.substring(0, start) + OPEN + CLOSE + text.substring(end);
        return new Result(result, start + OPEN.length());
    }
}
