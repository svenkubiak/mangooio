package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.build.Minification;
import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;

/**
 * 
 * @author svenkubiak
 *
 */
public class MinificationTest {
    private static final String COMPILED_LESS = "background-color: #143352;";
    private static final String COMPILED_SASS = "font: 100% Helvetica, sans-serif;";
    private static final String CSS = "p{font:normal 14px/20px helvetica, arial, sans-serif;color:#333;}.woot{font-weight:bold;}";
    private static final String JS = "$(document).ready(function(){$('#username').focus();});$('.btn-success').click(function(){var btn=$(this);btn.button('loading');});";
    private static final String TEMP = System.getProperty("java.io.tmpdir") + "/";
    private static final String ASSET_PATH = "assets/";
    private Config config;
    
    @Before
    public void init() {
        config = Mockito.mock(Config.class);
        when(config.isApplicationMinifyCSS()).thenReturn(true);
        when(config.isApplicationMinifyJS()).thenReturn(true);
        when(config.isApplicationPreprocessLess()).thenReturn(true);
        when(config.isApplicationPreprocessSass()).thenReturn(true);
        Minification.setConfig(config);
        Minification.setAssetPath(ASSET_PATH);
        Minification.setBasePath(TEMP);
        
        File dir1 = new File(TEMP + ASSET_PATH + Default.JAVASCRIPT_FOLDER.toString());
        File dir2 = new File(TEMP + ASSET_PATH + Default.STYLESHEET_FOLDER.toString());
        dir1.mkdir();
        dir2.mkdir();
    }
    
    @Test
    public void testMinifyCSS() throws IOException {
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
        File file = new File(TEMP + uuid + ".css");
        FileUtils.writeStringToFile(file, buffer.toString(), Default.ENCODING.toString());
        Minification.minify(file.getAbsolutePath());
        File outputfile = new File(TEMP + ASSET_PATH + Default.STYLESHEET_FOLDER.toString() + "/" + uuid + ".min.css");

        //then
        assertThat(FileUtils.readFileToString(outputfile, Default.ENCODING.toString()), equalTo(CSS));
        assertThat(outputfile.exists(), equalTo(true));
        assertThat(outputfile.length(), lessThan(file.length()));
        assertThat(file.delete(), equalTo(true));
        assertThat(outputfile.delete(), equalTo(true));
    }
    
    @Test
    public void testMinifyJS() throws IOException {
        //given
        String uuid = UUID.randomUUID().toString();
        StringBuilder buffer = new StringBuilder();
        buffer.append("$(document).ready(function() {");
        buffer.append("    $('#username').focus();");
        buffer.append("});");
        buffer.append("$('.btn-success').click(function() {");
        buffer.append("    var btn = $(this);");
        buffer.append("    btn.button('loading');");
        buffer.append(" });");
        
        //when
        File file = new File(TEMP + uuid + ".js");
        FileUtils.writeStringToFile(file, buffer.toString(), Default.ENCODING.toString());
        Minification.minify(file.getAbsolutePath());
        File outputfile = new File(TEMP + ASSET_PATH + Default.JAVASCRIPT_FOLDER.toString() + "/" + uuid + ".min.js");

        //then
        assertThat(outputfile.exists(), equalTo(true));
        assertThat(FileUtils.readFileToString(outputfile, Default.ENCODING.toString()), equalTo(JS));
        assertThat(outputfile.length(), lessThan(file.length()));
        assertThat(file.delete(), equalTo(true));
        assertThat(outputfile.delete(), equalTo(true));
    }
    
    @Test
    public void testPreprocessLess() throws IOException {
        //given
        String uuid = UUID.randomUUID().toString();
        StringBuilder buffer = new StringBuilder();
        buffer.append("@meineFarbe: #143352;");
        buffer.append("    ");
        buffer.append("#header {");
        buffer.append("  background-color: @meineFarbe;");
        buffer.append("}");
        buffer.append("h2 {");
        buffer.append("  color: @meineFarbe;");
        buffer.append("}");
        
        //when
        File file = new File(TEMP + uuid + ".less");
        FileUtils.writeStringToFile(file, buffer.toString(), Default.ENCODING.toString());
        Minification.preprocess(file.getAbsolutePath());
        File outputfile = new File(TEMP + ASSET_PATH + Default.STYLESHEET_FOLDER.toString() + "/" + uuid + ".css");

        //then
        assertThat(outputfile.exists(), equalTo(true));
        assertThat(FileUtils.readFileToString(outputfile, Default.ENCODING.toString()), containsString(COMPILED_LESS));
        assertThat(file.delete(), equalTo(true));
        assertThat(outputfile.delete(), equalTo(true));
    }
    
    @Test
    public void testPreprocessSass() throws IOException {
        //given
        String uuid = UUID.randomUUID().toString();
        StringBuilder buffer = new StringBuilder();
        buffer.append("$font-stack: Helvetica, sans-serif;");
        buffer.append("$primary-color: #333;");
        buffer.append("    ");
        buffer.append("body {");
        buffer.append("  font: 100% $font-stack;");
        buffer.append("  color: $primary-color;");
        buffer.append("}");
        
        //when
        File file = new File(TEMP + uuid + ".sass");
        FileUtils.writeStringToFile(file, buffer.toString(), Default.ENCODING.toString());
        Minification.preprocess(file.getAbsolutePath());
        File outputfile = new File(TEMP + ASSET_PATH + Default.STYLESHEET_FOLDER.toString() + "/" + uuid + ".css");

        //then
        assertThat(outputfile.exists(), equalTo(true));
        assertThat(FileUtils.readFileToString(outputfile, Default.ENCODING.toString()), containsString(COMPILED_SASS));
        assertThat(file.delete(), equalTo(true));
        assertThat(outputfile.delete(), equalTo(true));
    }
}