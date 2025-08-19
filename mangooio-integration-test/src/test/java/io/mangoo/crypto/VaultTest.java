package io.mangoo.crypto;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.utils.CodecUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
public class VaultTest {

    @Test
    void testPut() {
        //given
        Vault vault = Application.getInstance(Vault.class);
        String key = "key";
        String value = CodecUtils.uuidV6();

        //when
        vault.put(key, value);
        String actual = vault.get(key);

        //then
        assertThat(actual, not(nullValue()));
        assertThat(actual, equalTo(value));
    }
}
