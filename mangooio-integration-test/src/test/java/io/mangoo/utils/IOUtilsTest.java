package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class IOUtilsTest {
    @Test
    public void testCloseQuietly() throws IOException {
        //given
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        InputStream inputStream = new FileInputStream(file);
        
        //when
        IOUtils.closeQuietly(inputStream);
        
        //then
        file.delete();
        assertThat(file.exists(), equalTo(false));
    } 
}