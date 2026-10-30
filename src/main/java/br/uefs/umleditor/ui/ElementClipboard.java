package br.uefs.umleditor.ui;

import java.util.Optional;

import br.uefs.umleditor.layout.GridSnap;

/**
 * Elements copied or cut with Ctrl+C or Ctrl+X, kept in the JSON format of
 * the files so that they can be pasted in the same diagram or in another
 * one (RF34). Each paste of the same content is shifted a little further.
 */
public class ElementClipboard {

    private String content;
    private int pasteCount;

    public void setContent(String json) {
        content = json;
        pasteCount = 0;
    }

    public Optional<String> getContent() {
        return Optional.ofNullable(content);
    }

    /**
     * @return how far the next paste is shifted from the copied position
     */
    public double nextPasteOffset() {
        pasteCount++;
        return pasteCount * (double) GridSnap.GRID_SIZE;
    }
}
