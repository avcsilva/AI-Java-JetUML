package br.uefs.umleditor.render;

import java.util.ArrayList;
import java.util.List;

/**
 * Text formatting of RF24: a line between slashes (/text/) is shown in italics
 * and a line between underscores (_text_) is shown underlined. Both marks can
 * be combined, in any order, and the delimiters are not shown.
 */
public final class StyledText {

    /**
     * A line of text without its delimiters, and the styles they indicate.
     */
    public record StyledLine(String text, boolean italic, boolean underline) {
    }

    private StyledText() {
    }

    public static StyledLine parse(String line) {
        String text = line;
        boolean italic = false;
        boolean underline = false;
        boolean changed = true;
        while (changed) {
            changed = false;
            if (!italic && isDelimited(text, '/')) {
                italic = true;
                text = text.substring(1, text.length() - 1);
                changed = true;
            } else if (!underline && isDelimited(text, '_')) {
                underline = true;
                text = text.substring(1, text.length() - 1);
                changed = true;
            }
        }
        return new StyledLine(text, italic, underline);
    }

    private static boolean isDelimited(String text, char delimiter) {
        return text.length() >= 3 && text.charAt(0) == delimiter && text.charAt(text.length() - 1) == delimiter;
    }

    /**
     * @return the lines of a multi-line text, or no lines if the text is empty
     */
    public static List<String> lines(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }
        for (String line : text.split("\r?\n", -1)) {
            result.add(line);
        }
        return result;
    }
}
