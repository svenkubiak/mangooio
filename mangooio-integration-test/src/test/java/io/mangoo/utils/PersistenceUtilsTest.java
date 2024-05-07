package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.test.concurrent.ConcurrentRunner;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
class PersistenceUtilsTest {

    @Test
    void testCollection() {
        //given
        String key = this.getClass().getName();
        String value = MangooUtils.uuid();

        //when
        PersistenceUtils.addCollection(key, value);

        //then
        assertThat(PersistenceUtils.getCollectionName(this.getClass()), equalTo(value));
    }

    @Test
    void testCollectionConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String key = this.getClass().getName();
            String value = MangooUtils.uuid();

            //when
            PersistenceUtils.addCollection(key, value);

            // then
            return PersistenceUtils.getCollectionName(this.getClass()).equals(value);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
}