package io.mangoo.enums;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class EnumsTest {
    
    @Test
    public void testBinding() {
        //then
        assertThat(Binding.fromString("io.mangoo.routing.bindings.Authentication"), equalTo(Binding.AUTHENTICATION));
        assertThat(Binding.fromString("io.mangoo.routing.bindings.Body"), equalTo(Binding.BODY));
        assertThat(Binding.fromString("java.lang.Double"), equalTo(Binding.DOUBLE));
        assertThat(Binding.fromString("double"), equalTo(Binding.DOUBLE_PRIMITIVE));
        assertThat(Binding.fromString("io.mangoo.routing.bindings.Flash"), equalTo(Binding.FLASH));
        assertThat(Binding.fromString("java.lang.Float"), equalTo(Binding.FLOAT));
        assertThat(Binding.fromString("float"), equalTo(Binding.FLOAT_PRIMITIVE));
        assertThat(Binding.fromString("io.mangoo.routing.bindings.Form"), equalTo(Binding.FORM));
        assertThat(Binding.fromString("int"), equalTo(Binding.INT_PRIMITIVE));
        assertThat(Binding.fromString("java.lang.Integer"), equalTo(Binding.INTEGER));
        assertThat(Binding.fromString("java.time.LocalDate"), equalTo(Binding.LOCALDATE));
        assertThat(Binding.fromString("java.time.LocalDateTime"), equalTo(Binding.LOCALDATETIME));
        assertThat(Binding.fromString("java.lang.Long"), equalTo(Binding.LONG));
        assertThat(Binding.fromString("long"), equalTo(Binding.LONG_PRIMITIVE));
        assertThat(Binding.fromString("java.util.Optional"), equalTo(Binding.OPTIONAL));
        assertThat(Binding.fromString("io.mangoo.routing.bindings.Request"), equalTo(Binding.REQUEST));
        assertThat(Binding.fromString("io.mangoo.routing.bindings.Session"), equalTo(Binding.SESSION));
        assertThat(Binding.fromString("java.lang.String"), equalTo(Binding.STRING));
        assertThat(Binding.fromString("undefined"), equalTo(Binding.UNDEFINED));
    }
}