package br.uefs.umleditor.export;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import br.uefs.umleditor.geom.Rectangle;
import br.uefs.umleditor.layout.EdgePathCalculator;
import br.uefs.umleditor.model.Diagram;
import br.uefs.umleditor.model.Edge;
import br.uefs.umleditor.model.Node;
import br.uefs.umleditor.render.DiagramRenderer;
import br.uefs.umleditor.render.FontSettings;
import br.uefs.umleditor.render.FxGraphicsAdapter;
import br.uefs.umleditor.render.RenderContext;
import br.uefs.umleditor.render.SvgGraphicsAdapter;
import br.uefs.umleditor.render.Theme;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Images of a whole diagram, used by Export Image (RF04) and Copy to
 * Clipboard (RF05): the image is cropped to the bounds of the content, has
 * no grid and always uses the light colors, with the font of the diagram.
 */
public final class DiagramImage {

    private static final double MARGIN = 20;

    private DiagramImage() {
    }

    /**
     * @return the area covered by the nodes and edges, or empty if the diagram is empty
     */
    public static Optional<Rectangle> contentBounds(Diagram diagram, DiagramRenderer renderer) {
        renderer.layout(diagram);
        Rectangle result = null;
        for (Node node : diagram.getAllNodes()) {
            result = union(result, renderer.getBounds(node));
        }
        EdgePathCalculator paths = new EdgePathCalculator(diagram, renderer);
        for (Edge edge : diagram.getEdges()) {
            result = union(result, paths.getBounds(edge));
        }
        return Optional.ofNullable(result);
    }

    private static Rectangle union(Rectangle current, Rectangle other) {
        return current == null ? other : current.union(other);
    }

    private static DiagramRenderer lightRenderer(FontSettings font) {
        return new DiagramRenderer(new RenderContext(Theme.LIGHT, font));
    }

    /**
     * @return the image of the diagram, or empty if the diagram is empty
     */
    public static Optional<WritableImage> toImage(Diagram diagram, FontSettings font) {
        DiagramRenderer renderer = lightRenderer(font);
        Optional<Rectangle> content = contentBounds(diagram, renderer);
        if (content.isEmpty()) {
            return Optional.empty();
        }
        Rectangle area = content.get().expanded(MARGIN);
        Canvas canvas = new Canvas(Math.ceil(area.width()), Math.ceil(area.height()));
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Theme.LIGHT.getBackground());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.translate(-area.x(), -area.y());
        renderer.draw(diagram, new FxGraphicsAdapter(gc, renderer.getContext()), List.of());
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Theme.LIGHT.getBackground());
        return Optional.of(canvas.snapshot(parameters, null));
    }

    /**
     * @return the SVG document of the diagram, or empty if the diagram is empty
     */
    public static Optional<String> toSvg(Diagram diagram, FontSettings font) {
        DiagramRenderer renderer = lightRenderer(font);
        Optional<Rectangle> content = contentBounds(diagram, renderer);
        if (content.isEmpty()) {
            return Optional.empty();
        }
        SvgGraphicsAdapter svg = new SvgGraphicsAdapter(renderer.getContext());
        renderer.draw(diagram, svg, List.of());
        return Optional.of(svg.toSvg(content.get().expanded(MARGIN), Theme.LIGHT.getBackground()));
    }

    /**
     * Writes the image of the diagram in the format (RF04).
     *
     * @return false if the diagram is empty and nothing was written
     */
    public static boolean export(Diagram diagram, FontSettings font, ImageFormat format, Path file)
            throws IOException {
        if (format == ImageFormat.SVG) {
            Optional<String> svg = toSvg(diagram, font);
            if (svg.isEmpty()) {
                return false;
            }
            Files.writeString(file, svg.get(), StandardCharsets.UTF_8);
            return true;
        }
        Optional<WritableImage> image = toImage(diagram, font);
        if (image.isEmpty()) {
            return false;
        }
        BufferedImage buffered = SwingFXUtils.fromFXImage(image.get(), null);
        if (format != ImageFormat.PNG) {
            // JPEG, GIF and BMP are written without transparency.
            BufferedImage opaque = new BufferedImage(buffered.getWidth(), buffered.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            opaque.createGraphics().drawImage(buffered, 0, 0, Color.WHITE, null);
            buffered = opaque;
        }
        if (!ImageIO.write(buffered, format.getWriterName(), file.toFile())) {
            throw new IOException("No writer available for " + format.getDescription());
        }
        return true;
    }

    /**
     * Puts the image of the diagram in the system clipboard (RF05).
     *
     * @return false if the diagram is empty and nothing was copied
     */
    public static boolean copyToClipboard(Diagram diagram, FontSettings font) {
        Optional<WritableImage> image = toImage(diagram, font);
        if (image.isEmpty()) {
            return false;
        }
        ClipboardContent content = new ClipboardContent();
        content.putImage(image.get());
        Clipboard.getSystemClipboard().setContent(content);
        return true;
    }
}
