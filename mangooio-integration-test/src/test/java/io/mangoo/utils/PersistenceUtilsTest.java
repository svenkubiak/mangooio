package io.mangoo.utils;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
class PersistenceUtilsTest {

    @Test
    void testCollection() {
        //given
        String key = this.getClass().getName();
        String value = CodecUtils.uuid();

        //when
        PersistenceUtils.addCollection(key, value);

        //then
        assertThat(PersistenceUtils.getCollectionName(this.getClass()), equalTo(value));
    }
}