package io.mangoo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.google.common.base.Charsets;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

/**
 * Convenient class for minification of CSS and JS files
 *
 * Based on
 * https://github.com/davidB/yuicompressor-maven-plugin/blob/master/src/main/java/net_alchim31_maven_yuicompressor/YuiCompressorMojo.java
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("all")
public final class MinificationUtils {
    private static final Logger LOG = LogManager.getLogger(MinificationUtils.class);
    private static final int HUNDRET_PERCENT = 100;
    private static String basePath;
    private static volatile Config config; //NOSONAR
    private static final String JS = "js";
    private static final String CSS = "css";
    private static final String MIN = "min";
    private static final boolean DISABLEOPTIMIZATION = false;
    private static final boolean PRESERVESEMICOLONS = false;
    private static final boolean VERBOSE = false;
    private static final boolean MUNGE = true;
    private static final int LINEBREAK = -1;

    private MinificationUtils() {
        config = new Config(basePath + Default.CONFIG_PATH.toString(), Mode.DEV);
    }

    public static void setBasePath(String path) {
        synchronized (MinificationUtils.class) {
            basePath = path;
        }
    }

    public static void minify(String absolutePath) {
        if (absolutePath == null || absolutePath.contains(MIN)) {
            return;
        }

        if (config == null) {
            System.setProperty(Key.APPLICATION_CONFIG.toString(), basePath + Default.CONFIG_PATH.toString());
            config = new Config(basePath + Default.CONFIG_PATH.toString(), Mode.DEV);
        }

        if (config.getBoolean(Key.APPLICATION_MINIFY_JS.toString(), false) && absolutePath.endsWith(JS)) {
            minifyJS(new File(absolutePath));
        } else if (config.getBoolean(Key.APPLICATION_MINIFY_CSS.toString(), false) && absolutePath.endsWith(CSS)) {
            minifyCSS(new File(absolutePath));
        }
    }

    private static void minifyJS(File inputFile) {
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            File outputFile = getOutputFile(inputFile, JS);
            inputStreamReader = new InputStreamReader(new FileInputStream(inputFile), Charsets.UTF_8);
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile), Charsets.UTF_8);

            JavaScriptCompressor compressor = new JavaScriptCompressor(inputStreamReader, new MinificationErrorReporter());
            compressor.compress(outputStreamWriter, LINEBREAK, MUNGE, VERBOSE, PRESERVESEMICOLONS, DISABLEOPTIMIZATION);

            outputStreamWriter.flush();
            logMinification(inputFile, outputFile);

            if (config.getBoolean(Key.APPLICATION_GZIP_JS.toString(), false)) {
                createGzipFile(outputFile);
            }
        } catch (IOException e) {
            LOG.error("Failed to minify JS", e);
        } finally {
            IOUtils.closeQuietly(inputStreamReader);
            IOUtils.closeQuietly(outputStreamWriter);
        }
    }

    private static void logMinification(File inputFile, File outputFile) {
        LOG.info(String.format("Minified asset %s (%db) -> %s (%db) [compressed to %d%% of original size]", inputFile.getName(), inputFile.length(), outputFile.getName(), outputFile.length(), ratioOfSize(inputFile, outputFile)));
    }

    private static void minifyCSS(File inputFile) {
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            File outputFile = getOutputFile(inputFile, CSS);
            inputStreamReader = new InputStreamReader(new FileInputStream(inputFile), Charsets.UTF_8);
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFile), Charsets.UTF_8);

            CssCompressor compressor = new CssCompressor(inputStreamReader);
            compressor.compress(outputStreamWriter, LINEBREAK);

            outputStreamWriter.flush();
            logMinification(inputFile, outputFile);

            if (config.getBoolean(Key.APPLICATION_GZIP_CSS.toString(), false)) {
                createGzipFile(outputFile);
            }
        } catch (IOException e) {
            LOG.error("Failed to minify CSS", e);
        } finally {
            IOUtils.closeQuietly(inputStreamReader);
            IOUtils.closeQuietly(outputStreamWriter);
        }
    }

    private static File getOutputFile(File file, String suffix) {
        String path = file.getAbsolutePath().split("target")[0];

        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        String folder = null;
        if (CSS.equals(suffix)) {
            folder = config.getString(Key.APPLICATION_MINIFY_CSSFOLDER, Default.STYLESHEET_FOLDER.toString());
        } else if (JS.equals(suffix)) {
            folder = config.getString(Key.APPLICATION_MINIFY_JSFOLDER, Default.JAVSCRIPT_FOLDER.toString());
        }

        return new File(path + Default.ASSETS_PATH.toString() + "/" + folder + "/" + fileName + ".min." + suffix);
    }

    private static void createGzipFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        File gzipped = new File(file.getAbsolutePath() + ".gz");
        GZIPOutputStream outpuStream = null;
        FileInputStream inputStream = null;
        try {
            outpuStream = new GZIPOutputStream(new FileOutputStream(gzipped));
            inputStream = new FileInputStream(file);
            IOUtils.copy(inputStream, outpuStream);
            LOG.info("Created gzipped asset " + gzipped.getName());
        } catch (IOException e) {
            LOG.error("Failed to create gzipped file", e);
        } finally {
            IOUtils.closeQuietly(outpuStream);
            IOUtils.closeQuietly(inputStream);
        }
    }

    private static long ratioOfSize(File inputFile, File outputFile) {
        long inFile = Math.max(inputFile.length(), 1);
        long outFile = Math.max(outputFile.length(), 1);
        return (outFile * HUNDRET_PERCENT) / inFile;
    }

    private static class MinificationErrorReporter implements ErrorReporter {
        @Override
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            if (line < 0) {
                LOG.warn(message);
            } else {
                LOG.warn(line + ':' + lineOffset + ':' + message);
            }
        }

        @Override
        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
            if (line < 0) {
                LOG.error(message);
            } else {
                LOG.error(line + ':' + lineOffset + ':' + message);
            }
        }

        @Override
        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }
}