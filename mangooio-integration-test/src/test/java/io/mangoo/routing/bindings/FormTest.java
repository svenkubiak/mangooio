package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class FormTest {
    private static final String INVALID_IPV6_ADDRESS = "001:db8:85a3:8d3:1319:8a2e:7348";
    private static final String VALID_IPV6_ADDRESS = "001:db8:85a3:8d3:1319:8a2e:370:7348";
    private static final String INVALID_IPV4_ADDRESS = "501.15.1.2.1";
    private static final String VALID_IPV4_ADDRESS = "192.168.2.1";
    private static final String VALID_URL = "https://mangoo.io";
    private static final String INVALID_URL = "https:/mangoo.io";
    private static final String BAR = "bar";
    private static final String FOO = "foo";

    private Form getNewForm() {
        Form form = Application.getInstance(Form.class);
        form.setSubmitted(true);

        return form;
    }

    @Test
    void testValidExactMatch() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "BlA");
        form.addValue(BAR, "BlA");
        form.expectExactMatch(FOO, BAR);

        //then
        assertThat(form.hasErrors(), equalTo(false));
    }
    
    @Test
    void testInvalidExactMatch() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "BlA");
        form.addValue(BAR, "Bla");
        form.expectExactMatch(FOO, BAR);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }

    @Test
    void testValidMatch() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "BLA");
        form.addValue(BAR, "bla");
        form.expectMatch(FOO, BAR);

        //then
        assertThat(form.hasErrors(), equalTo(false));
    }
    
    @Test
    void testInvalidMatch() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "BLA");
        form.addValue(BAR, "bla2");
        form.expectMatch(FOO, BAR);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }

    @Test
    void testValidRequired() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        form.expectValue(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(false));
    }
    
    @Test
    void testInvalidRequired() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "");
        form.expectValue(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }

    @Test
    void testValidMin() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        form.expectMinLength(FOO, 3);

        //then
        assertThat(form.hasErrors(), equalTo(false));
    }
    
    @Test
    void testInvalidMin() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "ba");
        form.expectMinLength(FOO, 4);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }

    @Test
    void testValidMax() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        form.expectMaxLength(FOO, 3);

        //then
        assertThat(form.hasErrors(), equalTo(false));
    }
    
    @Test
    void testInvalidMax() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "bars");
        form.expectMaxLength(FOO, 3);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }

    @Test
    void testValidEmail() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "foo@bar.com");
        form.expectEmail(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(false));
        
        form = getNewForm();
        form.addValue(FOO, "foobar");
        form.expectEmail(FOO);

        assertThat(form.hasErrors(), equalTo(true));
    }
    
    @Test
    void testInvalidEmail() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "foobar");
        form.expectEmail(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }

    @Test
    void testValidUrl() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, VALID_URL);
        form.expectUrl(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(false));
    }
    
    @Test
    void testInvalidUrl() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, INVALID_URL);
        form.expectUrl(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }

    @Test
    void testValidIpv4Address() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, VALID_IPV4_ADDRESS);
        form.expectIpv4(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(false));
    }
    
    @Test
    void testInvalidIpv4Address() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, INVALID_IPV4_ADDRESS);
        form.expectIpv4(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }

    @Test
    void testValidIpv6Address() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, VALID_IPV6_ADDRESS);
        form.expectIpv6(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(false));
    }
    
    @Test
    void testInvalidIpv6Address() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, INVALID_IPV6_ADDRESS);
        form.expectIpv6(FOO);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }

    @Test
    void testValidRange() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        form.expectRangeLength(FOO, 1, 3);

        //then
        assertThat(form.hasErrors(), equalTo(false));
    }
    
    @Test
    void testInvalidRange() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "barddddd");
        form.expectRangeLength(FOO, 1, 4);

        //then
        assertThat(form.hasErrors(), equalTo(true));
    }
    
    @Test
    void testValidGetValue() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        
        //then
        assertThat(form.get(FOO), not(nullValue()));
        assertThat(form.get(FOO), equalTo(BAR));
    }
    
    @Test
    void testInvalidGetValue() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        
        //then
        assertThat(form.get("fnu"), equalTo(null));
    }
    
    @Test
    void testGetString() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);

        //then
        assertThat(form.getString(FOO), not(nullValue()));
        assertThat(form.getString(FOO).get(), equalTo(BAR));
    }
    
    @Test
    void testGetBoolean() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue("foo-true", "true");
        form.addValue("foo-false", "false");
        form.addValue("foo-1", "1");
        form.addValue("foo-0", "0");
        
        //then
        assertThat(form.getBoolean("foo-true").get(), equalTo(true));
        assertThat(form.getBoolean("foo-1").get(), equalTo(true));
        assertThat(form.getBoolean("foo-false").get(), equalTo(false));
        assertThat(form.getBoolean("foo-0").get(), equalTo(false));
    }
    
    @Test
    void testGetInteger() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "1");
        
        //then
        assertThat(form.getInteger(FOO).get(), equalTo(1));
    }
    
    @Test
    void testGetDouble() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "1.234");

        //then
        assertThat(form.getDouble(FOO).get(), equalTo(1.234));
    }
    
    @Test
    void testGetFloat() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "1.0");

        //then
        assertThat(form.getFloat(FOO).get(), equalTo(1.0F));
    }
    
    @Test
    void testFile() throws IOException {
        //given
        Form form = getNewForm();
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        
        //when
        InputStream stream = new FileInputStream(file);
        form.addFile(stream);
        
        //then
        assertThat(form.getFile(), not(nullValue()));
        assertThat(form.getFile().isPresent(), equalTo(true));
        assertThat(form.getFiles().size(), equalTo(1));
        
        file.delete();
    }
}