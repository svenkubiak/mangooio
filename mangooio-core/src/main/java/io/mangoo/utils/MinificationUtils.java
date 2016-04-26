package io.mangoo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import com.google.common.base.Charsets;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
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
    private static final int HUNDRED_PERCENT = 100;
    private static final String JS = "js";
    private static final String CSS = "css";
    private static final String LESS = "less";
    private static final String SASS = "sass";
    private static final String MIN = "min";
    private static String basePath;
    private static volatile Config config; //NOSONAR

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
            config = new Config(basePath + Default.CONFIG_PATH.toString(), Mode.DEV);
        }

        if (config.isMinifyCSS() && absolutePath.endsWith(JS)) {
            minifyJS(new File(absolutePath));
        } else if (config.isMinifyJS() && absolutePath.endsWith(CSS)) {
            minifyCSS(new File(absolutePath));
        }
    }
    
    /**
     * Compiles a LESS or SASS file to a corresponding CSS file
     * 
     * @param absolutePath The absolute path to the file
     */
    public static void compile(String absolutePath) {
        if (absolutePath == null) {
            return;
        }
        
        if (config == null) {
            System.setProperty(Key.APPLICATION_CONFIG.toString(), basePath + Default.CONFIG_PATH.toString());
            config = new Config(basePath + Default.CONFIG_PATH.toString(), Mode.DEV);
        }

        if (absolutePath.endsWith(LESS)) {
            lessify(new File(absolutePath));
        } else if (absolutePath.endsWith(SASS)) {
            sassify(new File(absolutePath));
        }
    }

    private static void lessify(File lessFile) {
        LessCompiler compiler = new DefaultLessCompiler();
        try {
            File outputFile = getCompiledFile(lessFile);
            CompilationResult compilationResult = compiler.compile(lessFile);
            FileUtils.writeStringToFile(outputFile, compilationResult.getCss(), Default.ENCODING.toString());
            logCompile(lessFile, outputFile);
        } catch (Less4jException | IOException e) {
            LOG.error("Failed to compile less file", e);
        }
    }
    
    private static void sassify(File sassFile) {
        File outputFile = getCompiledFile(sassFile);
        
        URI inputURI = sassFile.toURI();
        URI outputURI = getCompiledFile(sassFile).toURI();

        Compiler compiler = new Compiler();
        try {
          Output output = compiler.compileFile(inputURI, outputURI, new Options());
          FileUtils.writeStringToFile(outputFile, output.getCss(), Default.ENCODING.toString());
          logCompile(sassFile, outputFile);
        } catch (CompilationException | IOException e) {
            LOG.error("Failed to compile sass file", e);
        }
    }

    private static void minifyJS(File inputFile) {
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            File outputFile = getMinifiedFile(inputFile, JS);
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
    
    private static void logCompile(File inputFile, File outputFile) {
        LOG.info(String.format("Compiled asset %s -> %s", inputFile.getName(), outputFile.getName()));
    }

    private static void minifyCSS(File inputFile) {
        try {
            File outputFile = getMinifiedFile(inputFile, CSS);

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

    private static File getMinifiedFile(File file, String suffix) {
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
    
    private static File getCompiledFile(File file) {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        
        return new File(file.getParent() + "/" + fileName + ".css");
    }

    private static long ratioOfSize(File inputFile, File outputFile) {
        long inFile = Math.max(inputFile.length(), 1);
        long outFile = Math.max(outputFile.length(), 1);
        
        return (outFile * HUNDRED_PERCENT) / inFile;
    }
}