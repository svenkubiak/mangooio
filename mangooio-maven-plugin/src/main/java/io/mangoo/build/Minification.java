package io.mangoo.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import biz.gabrys.lesscss.compiler.CompilerException;
import biz.gabrys.lesscss.compiler.LessCompiler;
import biz.gabrys.lesscss.compiler.LessCompilerImpl;
import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Suffix;
import io.mangoo.utils.IOUtils;
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
    private static final String LESS = "less";
    private static final String SASS = "sass";
    private static final String MIN = "min";
    private static String basePath;
    private static String assetPath;
    private static volatile Config config; //NOSONAR

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

    public static void setConfig(Config configuration) {
        synchronized (Config.class) {
            config = configuration;
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

        if (config == null) {
            System.setProperty(Key.APPLICATION_CONFIG.toString(), basePath + Default.CONFIG_PATH.toString());
            config = new Config();
        }

        if (config.isApplicationMinifyCSS() && absolutePath.endsWith(JS)) {
            minifyJS(new File(absolutePath));
        } else if (config.isApplicationMinifyJS() && absolutePath.endsWith(CSS)) {
            minifyCSS(new File(absolutePath));
        }
    }

    /**
     * Compiles a LESS or SASS file to a corresponding CSS file
     *
     * @param absolutePath The absolute path to the file
     */
    public static void preprocess(String absolutePath) {
        if (absolutePath == null) {
            return;
        }

        if (config == null) {
            System.setProperty(Key.APPLICATION_CONFIG.toString(), basePath + Default.CONFIG_PATH.toString());
            config = new Config();
        }

        if (config.isApplicationPreprocessLess() && absolutePath.endsWith(LESS)) {
            lessify(new File(absolutePath));
        } else if (config.isApplicationPreprocessSass() && absolutePath.endsWith(SASS)) {
            sassify(new File(absolutePath));
        }
    }

    private static void lessify(File lessFile) {
        LessCompiler compiler = new LessCompilerImpl();
        final File outputFile = getOutputFile(lessFile, Suffix.CSS);
        try {
            String css = compiler.compile(lessFile);
            FileUtils.writeStringToFile(outputFile, css, Default.ENCODING.toString());
            logPreprocess(lessFile, outputFile);
        } catch (IOException | CompilerException e) {
            LOG.error("Failed to preprocess LESS file", e);
        }
    }

    private static void sassify(File sassFile) {
        final File outputFile = getOutputFile(sassFile, Suffix.CSS);

        final URI inputURI = sassFile.toURI();
        final URI outputURI = outputFile.toURI();

        final Compiler compiler = new Compiler();
        try {
          final Output output = compiler.compileFile(inputURI, outputURI, new Options());
          FileUtils.writeStringToFile(outputFile, output.getCss(), Default.ENCODING.toString());
          logPreprocess(sassFile, outputFile);
        } catch (CompilationException | IOException e) {
            LOG.error("Failed to preprocess SASS file", e);
        }
    }

    private static void minifyJS(File inputFile) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            final File outputFile = getOutputFile(inputFile, Suffix.JS_MIN);
            fileInputStream = new FileInputStream(inputFile);
            fileOutputStream = new FileOutputStream(outputFile);

            final JSMin jsMin = new JSMin(fileInputStream, fileOutputStream);
            jsMin.jsmin();

            logMinification(inputFile, outputFile);
        } catch (IOException | JSMinException e) {
            LOG.error("Failed to minify JS", e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    private static void logMinification(File inputFile, File outputFile) {
        LOG.info(String.format("Minified asset %s (%db) -> %s (%db) [compressed to %d%% of original size]", inputFile.getName(), inputFile.length(), outputFile.getName(), outputFile.length(), ratioOfSize(inputFile, outputFile)));
    }

    private static void logPreprocess(File inputFile, File outputFile) {
        LOG.info(String.format("Preprocessed asset %s -> %s", inputFile.getName(), outputFile.getName()));
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
            assetPath = assetPath.substring(1);
        }

        if (!assetPath.endsWith("/")) {
            assetPath = assetPath + "/";
        }

        String subpath = null;
        if (Suffix.CSS.equals(targetSuffix) || Suffix.CSS_MIN.equals(targetSuffix)) {
            subpath = Default.STYLESHEET_FOLDER.toString() + "/" + fileName;
        } else if (Suffix.JS.equals(targetSuffix) || Suffix.JS_MIN.equals(targetSuffix)) {
            subpath = Default.JAVASCRIPT_FOLDER.toString() + "/" + fileName;
        }

        return new File(basePath + assetPath + subpath);
    }

    private static long ratioOfSize(File inputFile, File outputFile) {
        final long inFile = Math.max(inputFile.length(), 1);
        final long outFile = Math.max(outputFile.length(), 1);

        return (outFile * HUNDRED_PERCENT) / inFile;
    }
}