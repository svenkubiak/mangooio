package io.mangoo.utils;

import java.io.Closeable;
import java.io.IOException;

public final class IOUtils {

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException e) {
            //intentionally left blank
        }
    }
}
