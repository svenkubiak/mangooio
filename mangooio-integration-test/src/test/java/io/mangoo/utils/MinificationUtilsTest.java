package io.mangoo.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.when;

/**
 * 
 * @author svenkubiak
 *
 */
public class MinificationUtilsTest {
    private static final String CSS = "p{font:normal 14px/20px helvetica, arial, sans-serif;color:#333;}.woot{font-weight:bold;}";
    private static final String JS = "$(document).ready(function(){$('#username').focus();});$('.btn-success').click(function(){var btn=$(this);btn.button('loading');});";
    private static final String TEMP = System.getProperty("java.io.tmpdir");
    private Config config;
    
    @Before
    public void init() {
        config = Mockito.mock(Config.class);
        when(config.isMinifyCSS()).thenReturn(true);
        when(config.isMinifyJS()).thenReturn(true);
        when(config.getMinifyCSSFolder()).thenReturn("");
        when(config.getMinifyJSFolder()).thenReturn("");
        MinificationUtils.setConfig(config);
        MinificationUtils.setBasePath(TEMP);
    }
    
    @Test
    public void minifyCSS() throws IOException {
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
        MinificationUtils.minify(file.getAbsolutePath());
        File outputfile = new File(TEMP + uuid + ".min.css");

        //then
        assertThat(FileUtils.readFileToString(outputfile, Default.ENCODING.toString()), equalTo(CSS));
        assertThat(outputfile.length(), lessThan(file.length()));
        assertThat(outputfile.exists(), equalTo(true));
        assertThat(file.delete(), equalTo(true));
        assertThat(outputfile.delete(), equalTo(true));
    }
    
    @Test
    public void minifyJS() throws IOException {
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
        MinificationUtils.minify(file.getAbsolutePath());
        File outputfile = new File(TEMP + uuid + ".min.js");

        //then
        assertThat(FileUtils.readFileToString(outputfile, Default.ENCODING.toString()), equalTo(JS));
        assertThat(outputfile.length(), lessThan(file.length()));
        assertThat(outputfile.exists(), equalTo(true));
        assertThat(file.delete(), equalTo(true));
        assertThat(outputfile.delete(), equalTo(true));
    }
}