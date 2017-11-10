package io.mangoo.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * 
 * @author svenkubiak
 *
 */
public final class IOUtils {
    
    private IOUtils() {
    }

    @SuppressWarnings("all")
    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException e) {
            //NOSONAR
            //intentionally left blank
        }
    }
}