package io.mangoo.core;

import io.mangoo.TestExtension;
import io.mangoo.enums.Header;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class ServerTest {
    
    @Test
    void testAdditionalHeader() {
        //then
        List<Entry<Header, String>> collect = Server.headers()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() == Header.FEATURE_POLICY).collect(Collectors.toList());
        
        assertThat(collect, not(nullValue()));
        assertThat(collect.get(0).getValue(), equalTo("myFeaturePolicy"));
    }
}