package br.uefs.umleditor.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StereotypeInsertionTest {

    @Test
    void insertsDelimitersAndPlacesCaretBetweenThem() {
        StereotypeInsertion.Result result = StereotypeInsertion.insert("Shape", 0, 0);
        assertEquals("«»Shape", result.text());
        assertEquals(1, result.caret());
    }

    @Test
    void replacesSelectedText() {
        StereotypeInsertion.Result result = StereotypeInsertion.insert("a xyz b", 5, 2);
        assertEquals("a «» b", result.text());
        assertEquals(3, result.caret());
    }
}
