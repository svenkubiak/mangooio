package io.mangoo.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.routing.bindings.Form;

/**
 * 
 * @author svenkubiak
 *
 */
public class FormTest {
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
    public void testValidExactMatch() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "BlA");
        form.addValue(BAR, "BlA");
        form.validation().exactMatch(FOO, BAR);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
    }
    
    @Test
    public void testInvalidExactMatch() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "BlA");
        form.addValue(BAR, "Bla");
        form.validation().exactMatch(FOO, BAR);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }

    @Test
    public void testValidMatch() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "BLA");
        form.addValue(BAR, "bla");
        form.validation().match(FOO, BAR);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
    }
    
    @Test
    public void testInvalidMatch() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "BLA");
        form.addValue(BAR, "bla2");
        form.validation().match(FOO, BAR);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }

    @Test
    public void testValidRequired() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        form.validation().required(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
    }
    
    @Test
    public void testInvalidRequired() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "");
        form.validation().required(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }

    @Test
    public void testValidMin() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        form.validation().min(FOO, 3);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
    }
    
    @Test
    public void testInvalidMin() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "ba");
        form.validation().min(FOO, 4);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }

    @Test
    public void testValidMax() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        form.validation().max(FOO, 3);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
    }
    
    @Test
    public void testInvalidMax() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "bars");
        form.validation().max(FOO, 3);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }

    @Test
    public void testValidEmail() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "foo@bar.com");
        form.validation().email(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
        
        form = getNewForm();
        form.addValue(FOO, "foobar");
        form.validation().email(FOO);

        assertTrue(form.validation().hasErrors());
    }
    
    @Test
    public void testInvalidEmail() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "foobar");
        form.validation().email(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }

    @Test
    public void testValidUrl() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, VALID_URL);
        form.validation().url(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
    }
    
    @Test
    public void testInvalidUrl() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, INVALID_URL);
        form.validation().url(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }

    @Test
    public void testValidIpv4Address() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, VALID_IPV4_ADDRESS);
        form.validation().ipv4(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
    }
    
    @Test
    public void testInvalidIpv4Address() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, INVALID_IPV4_ADDRESS);
        form.validation().ipv4(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }

    @Test
    public void testValidIpv6Address() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, VALID_IPV6_ADDRESS);
        form.validation().ipv6(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
    }
    
    @Test
    public void testInvalidIpv6Address() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, INVALID_IPV6_ADDRESS);
        form.validation().ipv6(FOO);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }

    @Test
    public void testValidRange() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        form.validation().range(FOO, 1, 3);

        //then
        assertThat(form.validation().hasErrors(), equalTo(false));
    }
    
    @Test
    public void testInvalidRange() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "barddddd");
        form.validation().range(FOO, 1, 4);

        //then
        assertThat(form.validation().hasErrors(), equalTo(true));
    }
    
    @Test
    public void testValidGetValue() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        
        //then
        assertThat(form.get(FOO), not(nullValue()));
        assertThat(form.get(FOO), equalTo(BAR));
    }
    
    @Test
    public void testInvalidGetValue() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);
        
        //then
        assertThat(form.get("fnu"), equalTo(null));
    }
    
    @Test
    public void testGetString() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, BAR);

        //then
        assertThat(form.getString(FOO), not(nullValue()));
        assertThat(form.getString(FOO).get(), equalTo(BAR));
    }
    
    @Test
    public void testGetBoolean() {
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
    public void testGetInteger() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "1");
        
        //then
        assertThat(form.getInteger(FOO).get(), equalTo(1));
    }
    
    @Test
    public void testGetDouble() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "1.234");

        //then
        assertThat(form.getDouble(FOO).get(), equalTo(1.234));
    }
    
    @Test
    public void testGetFloat() {
        //given
        Form form = getNewForm();
        
        //when
        form.addValue(FOO, "1.0");

        //then
        assertThat(form.getFloat(FOO).get(), equalTo(1.0F));
    }
    
    @Test
    public void testFile() {
        //given
        Form form = getNewForm();
        File file = new File(UUID.randomUUID().toString());
        
        //when
        form.addFile(file);
        
        //then
        assertThat(form.getFile(), not(nullValue()));
        assertThat(form.getFile().isPresent(), equalTo(true));
        assertThat(form.getFiles().size(), equalTo(1));
        
        file.delete();
    }
}