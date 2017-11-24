package io.mangoo.utils;

import java.text.DecimalFormat;

/**
 * 
 * @author svenkubiak
 *
 */
public final class MetricsUtils {
    
    /**
     * COnverts a given file size into a readable file size including unit
     * 
     * @param size the size in byte to convert
     * @return Readable files size, e.g. 24 MB
     */
    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        
        String [] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
 
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}