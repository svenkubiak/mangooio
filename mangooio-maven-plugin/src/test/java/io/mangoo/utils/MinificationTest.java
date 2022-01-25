package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.mangoo.build.Minification;
import io.mangoo.enums.Default;
import io.undertow.util.FileUtils;

/**
 * 
 * @author svenkubiak
 *
 */
class MinificationTest {
    private static final String CSS = "p{font:normal 14px/20px helvetica, arial, sans-serif;color:#333;}.woot{font-weight:bold;}";
    private static final String TEMP = System.getProperty("java.io.tmpdir") + File.separator + "stylesheet" + File.separator;
    private static final String ASSET_PATH = "assets" + File.separator;
    
    @BeforeEach
    void init() {
        Minification.setBasePath(TEMP);
        Minification.setAssetPath(ASSET_PATH);
        
        File dir1 = new File(TEMP + ASSET_PATH + Default.JAVASCRIPT_FOLDER.toString());
        File dir2 = new File(TEMP + ASSET_PATH + Default.STYLESHEET_FOLDER.toString());
        dir1.mkdir();
        dir2.mkdir();
    }
    
    @Test
    void testMinifyCSS() throws IOException {
        //given
        String uuid = UUID.randomUUID().toString();
        StringBuilder buffer = new StringBuilder();
        buffer.append("p {");
        buffer.append("    font: normal 14px/20px helvetica, arial, sans-serif;");
        buffer.append("    color: #333;");
        buffer.append("}");
        buffer.append(".woot {");
        buffer.append("    font-weight: bold;");
        buffer.append("}");
        
        //when
        if (!Files.exists(Paths.get(TEMP))) {
            Files.createDirectory(Paths.get(TEMP));   
        }
        
        Path inputFile = Files.createFile(Paths.get(TEMP + uuid + ".css"));
        Files.writeString(inputFile, buffer.toString(), StandardOpenOption.TRUNCATE_EXISTING);
        Minification.minify(inputFile.toAbsolutePath().toString());
        Path outputFile = Paths.get(TEMP + ASSET_PATH + Default.STYLESHEET_FOLDER.toString() + File.separator + uuid + ".min.css");
        
        //then
        assertThat(Files.readString(outputFile), equalTo(CSS));
        assertThat(Files.size(outputFile), lessThan(Files.size(inputFile)));
        assertThat(Files.deleteIfExists(inputFile), equalTo(true));
        assertThat(Files.deleteIfExists(outputFile), equalTo(true));
        FileUtils.deleteRecursive(Paths.get(TEMP));
    }
}