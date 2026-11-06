package io.mangoo.filters;

import io.mangoo.TestExtension;
import io.mangoo.constants.Default;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

// Started like every other test, as loading AdminFilter would otherwise initialise Log4j before the application
@ExtendWith({TestExtension.class})
class AdminFilterTest {

    @Test
    void testNameIsApplicationName() {
        //given
        String name = "household";

        //then
        assertThat(AdminFilter.getName(name), equalTo(name));
    }

    @Test
    void testNameFallsBackToFrameworkName() {
        //then the default application name is not shown
        assertThat(AdminFilter.getName(Default.APPLICATION_NAME), equalTo("mangoo I/O"));
        assertThat(AdminFilter.getName(null), equalTo("mangoo I/O"));
        assertThat(AdminFilter.getName(" "), equalTo("mangoo I/O"));
    }
}
