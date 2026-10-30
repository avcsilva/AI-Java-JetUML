package br.uefs.umleditor.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class ImageFormatTest {

    @Test
    void formatComesFromTheExtension() {
        assertEquals(Optional.of(ImageFormat.SVG), ImageFormat.fromFileName("diagram.svg"));
        assertEquals(Optional.of(ImageFormat.PNG), ImageFormat.fromFileName("Diagram.PNG"));
        assertEquals(Optional.of(ImageFormat.JPEG), ImageFormat.fromFileName("a.jpeg"));
        assertEquals(Optional.of(ImageFormat.JPEG), ImageFormat.fromFileName("a.jpg"));
        assertEquals(Optional.of(ImageFormat.GIF), ImageFormat.fromFileName("a.gif"));
        assertEquals(Optional.of(ImageFormat.BMP), ImageFormat.fromFileName("a.bmp"));
        assertTrue(ImageFormat.fromFileName("diagram").isEmpty());
    }
}
