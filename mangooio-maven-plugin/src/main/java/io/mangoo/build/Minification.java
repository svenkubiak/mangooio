package io.mangoo.build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.enums.Default;
import io.mangoo.enums.Suffix;
import io.mangoo.utils.MangooUtils;
import net.jawr.web.minification.CSSMinifier;
import net.jawr.web.minification.JSMin;
import net.jawr.web.minification.JSMin.JSMinException;

/**
 * Convenient class for minification of CSS and JS files
 *
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("all")
public final class Minification {
    private static final Logger LOG = LogManager.getLogger(Minification.class);
    private static final int HUNDRED_PERCENT = 100;
    private static final String JS = "js";
    private static final String CSS = "css";
    private static final String MIN = "min";
    private static String assetPath = "src" + File.separator
                                       + "main" + File.separator + "resources" 
                                       + File.separator + "files" + File.separator + "assets" + File.separator;
    private static String basePath;

    private Minification() {
    }

    public static void setBasePath(String path) {
        synchronized (Minification.class) {
            basePath = path;
        }
    }
    
    public static void setAssetPath(String path) {
        synchronized (Minification.class) {
            assetPath = path;
        }
    }

    /**
     * Minifies a JS or CSS file to a corresponding JS or CSS file
     *
     * @param absolutePath The absolute path to the file
     */
    public static void minify(String absolutePath) {
        if (absolutePath == null || absolutePath.contains(MIN)) {
            return;
        }
        
        if (!absolutePath.contains(File.separator + "stylesheet" + File.separator)
                && !absolutePath.contains(File.separator + "javascript" + File.separator)) {
            return;
        }

        if (absolutePath.endsWith(CSS)) {
            minifyCSS(new File(absolutePath)); //NOSONAR
        } else if (absolutePath.endsWith(JS)) {
            minifyJS(new File(absolutePath)); //NOSONAR
        }
    }

    private static void minifyJS(File inputFile) {
        InputStream fileInputStream = null;
        OutputStream fileOutputStream = null;
        try {
            final File outputFile = getOutputFile(inputFile, Suffix.JS_MIN);
            
            fileInputStream = Files.newInputStream(inputFile.toPath());
            fileOutputStream = Files.newOutputStream(outputFile.toPath());

            final JSMin jsMin = new JSMin(fileInputStream, fileOutputStream);
            jsMin.jsmin();

            logMinification(inputFile, outputFile);
        } catch (IOException | JSMinException e) {
            LOG.error("Failed to minify JS", e);
        } finally {
            MangooUtils.closeQuietly(fileInputStream);
            MangooUtils.closeQuietly(fileOutputStream);
        }
    }

    private static void minifyCSS(File inputFile) {
        try {
            final File outputFile = getOutputFile(inputFile, Suffix.CSS_MIN);

            final StringBuffer stringBuffer = new StringBuffer();
            final CSSMinifier cssMinifier = new CSSMinifier();

            stringBuffer.append(FileUtils.readFileToString(inputFile, Default.ENCODING.toString()));
            final StringBuffer minifyCSS = cssMinifier.minifyCSS(stringBuffer);

            FileUtils.write(outputFile, minifyCSS.toString(), Default.ENCODING.toString());

            logMinification(inputFile, outputFile);
        } catch (final IOException e) {
            LOG.error("Failed to minify CSS", e);
        }
    }

    private static File getOutputFile(File inputfile, Suffix targetSuffix) {
        String fileName = inputfile.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        fileName = fileName + targetSuffix.toString();

        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }

        if (assetPath.startsWith("/")) {
            assetPath = assetPath.substring(1); //NOSONAR
        }

        if (!assetPath.endsWith("/")) {
            assetPath = assetPath + "/";
        }

        String subpath = null;
        if (Suffix.CSS == targetSuffix || Suffix.CSS_MIN == targetSuffix) {
            subpath = Default.STYLESHEET_FOLDER.toString() + "/" + fileName;
        } else if (Suffix.JS == targetSuffix || Suffix.JS_MIN == targetSuffix) {
            subpath = Default.JAVASCRIPT_FOLDER.toString() + "/" + fileName;
        }

        return new File(basePath + assetPath + subpath); //NOSONAR
    }

    private static void logMinification(File inputFile, File outputFile) {
        LOG.info(String.format("Minified asset %s (%db) -> %s (%db) [compressed to %d%% of original size]", inputFile.getName(), inputFile.length(), outputFile.getName(), outputFile.length(), ratioOfSize(inputFile, outputFile)));
    }

    private static long ratioOfSize(File inputFile, File outputFile) {
        final long inFile = Math.max(inputFile.length(), 1);
        final long outFile = Math.max(outputFile.length(), 1);

        return (outFile * HUNDRED_PERCENT) / inFile;
    }
}