package controllers;

import io.mangoo.routing.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileController {
    private static final Path FILE = createFile();
    private static final Path MISSING_FILE = Path.of(System.getProperty("java.io.tmpdir"), "mangoo-bodyfile-missing.png");
    private static final int SIZE = 128;

    /**
     * @return The path of the temporary file which is served by this controller
     */
    public static Path getFile() {
        return FILE;
    }

    /**
     * @return The path of a file which intentionally does not exist
     */
    public static Path getMissingFile() {
        return MISSING_FILE;
    }

    private static Path createFile() {
        var image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        for (var x = 0; x < SIZE; x++) {
            for (var y = 0; y < SIZE; y++) {
                image.setRGB(x, y, (x * SIZE + y) % 0xFFFFFF);
            }
        }

        try {
            var path = Files.createTempFile("mangoo-bodyfile", ".png"); //NOSONAR
            path.toFile().deleteOnExit();
            ImageIO.write(image, "png", path.toFile());

            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Response file() {
        return Response.ok().bodyFile(FILE);
    }

    public Response fileWithContentType() {
        return Response.ok().contentType("application/zip").bodyFile(FILE);
    }

    public Response missingFile() {
        return Response.ok().bodyFile(MISSING_FILE);
    }
}
