package io.mangoo.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.enums.Header;
import io.undertow.util.HttpString;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class ServerTest {
    
    @Test
    public void testAdditionalHeader() {
        //then
        List<Entry<HttpString, String>> collect = Server.headers()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() == Header.FEATURE_POLICY.toHttpString()).collect(Collectors.toList());
        
        assertThat(collect, not(nullValue()));
        assertThat(collect.get(0).getValue(), equalTo("myFeaturePolicy"));
    }
}