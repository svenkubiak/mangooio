package io.mangoo.utils;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.mangoo.build.Minification;
import io.mangoo.core.Config;
import io.mangoo.enums.Default;

/**
 * 
 * @author svenkubiak
 *
 */
public class MinificationTest {
    private static final String CSS = "p{font:normal 14px/20px helvetica, arial, sans-serif;color:#333;}.woot{font-weight:bold;}";
    private static final String JS = "$(document).ready(function(){$('#username').focus();});$('.btn-success').click(function(){var btn=$(this);btn.button('loading');});";
    private static final String TEMP = System.getProperty("java.io.tmpdir") + "/";
    private static final String ASSET_PATH = "assets/";
    private Config config;
    
    @BeforeEach
    public void init() {
        config = Mockito.mock(Config.class);
        when(config.isApplicationMinifyCSS()).thenReturn(true);
        when(config.isApplicationMinifyJS()).thenReturn(true);
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
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(outputfile.exists(), equalTo(true)));
        assertThat(FileUtils.readFileToString(outputfile, Default.ENCODING.toString()), equalTo(JS));
        assertThat(outputfile.length(), lessThan(file.length()));
        assertThat(file.delete(), equalTo(true));
        assertThat(outputfile.delete(), equalTo(true));
    }
}