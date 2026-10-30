package br.uefs.umleditor.render;

/**
 * Style of a piece of text drawn in a diagram.
 */
public record TextStyle(boolean bold, boolean italic, boolean underline) {

    public static final TextStyle PLAIN = new TextStyle(false, false, false);
    public static final TextStyle BOLD = new TextStyle(true, false, false);
    public static final TextStyle UNDERLINED = new TextStyle(false, false, true);

    /**
     * @return this style with the italic and underline marks of a styled line added
     */
    public TextStyle with(StyledText.StyledLine line) {
        return new TextStyle(bold, italic || line.italic(), underline || line.underline());
    }
}
