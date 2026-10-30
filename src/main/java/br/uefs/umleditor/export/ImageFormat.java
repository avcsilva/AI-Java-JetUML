package br.uefs.umleditor.export;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Image formats offered by File > Export Image, chosen by the extension in
 * "Save as type" (RF04).
 */
public enum ImageFormat {
    PNG("PNG image", "png", List.of("png"), "png"),
    JPEG("JPEG image", "jpg", List.of("jpg", "jpeg"), "jpeg"),
    GIF("GIF image", "gif", List.of("gif"), "gif"),
    BMP("BMP image", "bmp", List.of("bmp"), "bmp"),
    SVG("SVG image", "svg", List.of("svg"), null);

    private final String description;
    private final String defaultExtension;
    private final List<String> extensions;
    private final String writerName;

    ImageFormat(String description, String defaultExtension, List<String> extensions, String writerName) {
        this.description = description;
        this.defaultExtension = defaultExtension;
        this.extensions = extensions;
        this.writerName = writerName;
    }

    /**
     * @return the format name used by javax.imageio, or null for SVG, which is written as text
     */
    public String getWriterName() {
        return writerName;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * @return the format whose extension ends the file name, if any
     */
    public static Optional<ImageFormat> fromFileName(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        for (ImageFormat format : values()) {
            for (String extension : format.extensions) {
                if (lower.endsWith("." + extension)) {
                    return Optional.of(format);
                }
            }
        }
        return Optional.empty();
    }
}
