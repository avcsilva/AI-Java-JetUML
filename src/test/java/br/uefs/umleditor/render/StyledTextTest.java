package br.uefs.umleditor.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class StyledTextTest {

    @Test
    void slashesMakeLineItalic() {
        StyledText.StyledLine line = StyledText.parse("/Shape/");
        assertEquals("Shape", line.text());
        assertTrue(line.italic());
        assertFalse(line.underline());
    }

    @Test
    void underscoresMakeLineUnderlined() {
        StyledText.StyledLine line = StyledText.parse("_count : int_");
        assertEquals("count : int", line.text());
        assertTrue(line.underline());
        assertFalse(line.italic());
    }

    @Test
    void marksCanBeCombinedInAnyOrder() {
        StyledText.StyledLine first = StyledText.parse("/_draw()_/");
        StyledText.StyledLine second = StyledText.parse("_/draw()/_");
        assertEquals("draw()", first.text());
        assertEquals("draw()", second.text());
        assertTrue(first.italic() && first.underline());
        assertTrue(second.italic() && second.underline());
    }

    @Test
    void textWithoutMatchingDelimitersIsPlain() {
        StyledText.StyledLine line = StyledText.parse("/path/to");
        assertEquals("/path/to", line.text());
        assertFalse(line.italic());
        assertEquals("_", StyledText.parse("_").text());
        assertEquals("//", StyledText.parse("//").text());
    }

    @Test
    void splitsLinesAndIgnoresEmptyText() {
        assertEquals(List.of("a", "b"), StyledText.lines("a\nb"));
        assertEquals(List.of(), StyledText.lines(""));
    }
}
