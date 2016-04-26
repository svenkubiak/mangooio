package io.mangoo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;

import io.advantageous.boon.core.Sys;
import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;
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
    
    public static void setConfig(Config configuration) {
        synchronized (Config.class) {
            config = configuration;
        }       
    }

    /**
     * Minifies a JS or CSS file
     * 
     * @param absolutePath The absolute path to the file
     */
    public static void minify(String absolutePath) {
        if (absolutePath == null || absolutePath.contains(MIN)) {
            return;
        }

        if (config == null) {
            System.setProperty(Key.APPLICATION_CONFIG.toString(), basePath + Default.CONFIG_PATH.toString());
            config = new Config(basePath + Default.CONFIG_PATH.toString(), Mode.DEV);
        }

        if (config.isMinifyCSS() && absolutePath.endsWith(JS)) {
            minifyJS(new File(absolutePath));
        } else if (config.isMinifyJS() && absolutePath.endsWith(CSS)) {
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

            JSMin jsMin = new JSMin(new FileInputStream(inputFile), new FileOutputStream(outputFile));            
            jsMin.jsmin();
            
            outputStreamWriter.flush();
            logMinification(inputFile, outputFile);
        } catch (IOException | JSMinException e) {
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
        try {
            File outputFile = getOutputFile(inputFile, CSS);

            StringBuffer stringBuffer = new StringBuffer();
            CSSMinifier cssMinifier = new CSSMinifier();
            
            stringBuffer.append(FileUtils.readFileToString(inputFile, Default.ENCODING.toString()));
            StringBuffer minifyCSS = cssMinifier.minifyCSS(stringBuffer);
            
            FileUtils.write(outputFile, minifyCSS.toString(), Default.ENCODING.toString());
            
            logMinification(inputFile, outputFile);
        } catch (IOException e) {
            LOG.error("Failed to minify CSS", e);
        }
    }

    private static File getOutputFile(File file, String suffix) {
        String path = file.getAbsolutePath().split("target")[0];
        
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        String folder = null;
        if (CSS.equals(suffix)) {
            folder = config.getMinifyCSSFolder();
        } else if (JS.equals(suffix)) {
            folder = config.getMinifyJSFolder();
        }

        if (StringUtils.isBlank(folder)) {
            return new File(file.getParent() + "/" + fileName + ".min." + suffix);
        } 
        
        return new File(path + config.getAssetsPath() + "/" + folder + "/" + fileName + ".min." + suffix);
    }

    private static long ratioOfSize(File inputFile, File outputFile) {
        long inFile = Math.max(inputFile.length(), 1);
        long outFile = Math.max(outputFile.length(), 1);
        return (outFile * HUNDRET_PERCENT) / inFile;
    }
}