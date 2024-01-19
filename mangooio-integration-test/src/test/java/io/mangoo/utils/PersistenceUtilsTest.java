package io.mangoo.utils;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
class PersistenceUtilsTest {

    @Test
    void testCollection() throws InterruptedException {
        //given
        String key = this.getClass().getName();
        String value = UUID.randomUUID().toString();

        //when
        PersistenceUtils.addCollection(key, value);

        //then
        assertThat(PersistenceUtils.getCollectionName(this.getClass()), equalTo(value));
    }
}