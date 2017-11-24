package io.mangoo.utils;

import java.text.DecimalFormat;

/**
 * 
 * @author svenkubiak
 *
 */
public final class MetricsUtils {
    private static final String [] UNITS = new String[] { "B", "kB", "MB", "GB", "TB" };
    private static final int CONVERTION = 1024;
    
    private MetricsUtils() {
    }
    
    /**
     * Converts a given file size into a readable file size including unit
     * 
     * @param size The size in bytes to convert
     * @return Readable files size, e.g. 24 MB
     */
    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        
        int index = (int) (Math.log10(size) / Math.log10(CONVERTION));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(CONVERTION, index)) + " " + UNITS[index];
    }
}