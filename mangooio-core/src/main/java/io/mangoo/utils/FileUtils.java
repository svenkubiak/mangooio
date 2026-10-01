package io.mangoo.utils;

import io.mangoo.constants.Required;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.apache.tika.Tika;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Objects;

public final class FileUtils {
    private static final Logger LOG = LogManager.getLogger(FileUtils.class);
    private static final String [] UNITS = new String[] { "B", "kB", "MB", "GB", "TB" };
    private static final int CONVERSION = 1024;
    private static final Tika TIKA = new Tika();

    private FileUtils() {}

    public static String getMimeType(byte[] data) {
        Objects.requireNonNull(data, Required.DATA);
        return TIKA.detect(data);
    }

    public static String getMimeType(InputStream data) {
        Objects.requireNonNull(data, Required.DATA);
        try {
            return TIKA.detect(data);
        } catch (IOException e) {
            return null;
        }
    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException e) {
            LOG.error("Failed to close resource quietly", e);
        }
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }

        int index = (int) (Math.log10(size) / Math.log10(CONVERSION));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(CONVERSION, index)) + " " + UNITS[index];
    }

    /**
     * Returns an empty string if the file cannot be read.
     */
    public static String readFileToString(Path path) {
        Objects.requireNonNull(path, Required.PATH);

        var content = Strings.EMPTY;
        try {
            content = Files.readString(path);
        } catch (IOException e) {
            // An unreadable file yields an empty string
        }

        return content;
    }

    /**
     * Returns an empty string if the file cannot be read.
     */
    public static String readFileToString(String path) {
        Argument.requireNonBlank(path, Required.PATH);

        return readFileToString(Path.of(path));
    }
}
