package io.mangoo.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.regex.Pattern;

import org.junit.Test;

import io.mangoo.routing.bindings.Validator;
import io.mangoo.test.Mangoo;

/**
 * 
 * @author svenkubiak
 *
 */
public class ValidatorTest {
    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9_.]+$");
    private static final String URL = "url";
    private static final String REGEX = "regex";
    private static final String RANGE = "range";
    private static final String IPV6 = "ipv6";
    private static final String IPV4 = "ipv4";
    private static final String EMAIL = "email";
    private static final String MATCH = "match";
    private static final String MATCH2 = "match2";
    private static final String EXACT_MATCH = "exactMatch";
    private static final String EXACT_MATCH2 = "exactMatch2";
    private static final String MAX = "max";
    private static final String NUMERIC = "numeric";
    private static final String MIN = "min";
    private static final String REQUIRED = "required";
    private static final String CUSTOM_ERROR_MESSAGE = "Custom error message";

    @Test
    public void testDefaultErrorMessages() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(URL, "");
        validator.add(REGEX, "");
        validator.add(RANGE, "");
        validator.add(IPV6, "");
        validator.add(IPV4, "");
        validator.add(EMAIL, "");
        validator.add(MATCH, "");
        validator.add(EXACT_MATCH, "");
        validator.add(MAX, "abc");
        validator.add(MIN, "");
        validator.add(REQUIRED, "");
        validator.add(NUMERIC, "");
        validator.url(URL);
        validator.regex(REGEX, PATTERN);
        validator.range(RANGE, 23, 42);
        validator.ipv6(IPV6);
        validator.ipv4(IPV4);
        validator.email(EMAIL);
        validator.match(MATCH, MATCH2);
        validator.exactMatch(EXACT_MATCH, EXACT_MATCH2);
        validator.max(MAX, 1);
        validator.min(MIN, 42);
        validator.required(REQUIRED);
        validator.url(URL);
        validator.numeric(NUMERIC);
        
        //then
        assertThat(validator.getError(URL), containsString("must be a valid URL"));
        assertThat(validator.getError(REGEX), containsString("is invalid"));
        assertThat(validator.getError(RANGE), containsString("must have a size between"));
        assertThat(validator.getError(IPV6), containsString("must be a valid IPv6 address"));
        assertThat(validator.getError(IPV4), containsString("must be a valid IPv4 address"));
        assertThat(validator.getError(EMAIL), containsString("must be a valid eMail address"));
        assertThat(validator.getError(MATCH), containsString("must match"));
        assertThat(validator.getError(EXACT_MATCH), containsString("must exactly match"));
        assertThat(validator.getError(MAX), containsString("must have a size of max"));
        assertThat(validator.getError(MIN), containsString("must have a least a size of"));
        assertThat(validator.getError(REQUIRED), containsString("is required"));
        assertThat(validator.getError(NUMERIC), containsString("must be a numeric value"));
    }
    
    @Test
    public void testCustomErrorMessages() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(URL, "");
        validator.add(REGEX, "");
        validator.add(RANGE, "");
        validator.add(IPV6, "");
        validator.add(IPV4, "");
        validator.add(EMAIL, "");
        validator.add(MATCH, "");
        validator.add(MATCH2, "");
        validator.add(EXACT_MATCH, "");
        validator.add(EXACT_MATCH2, "");
        validator.add(MAX, "abc");
        validator.add(MIN, "");
        validator.add(REQUIRED, "");
        validator.add(NUMERIC, "");
        validator.url(URL, CUSTOM_ERROR_MESSAGE);
        validator.required(REGEX, CUSTOM_ERROR_MESSAGE);
        validator.range(RANGE, 23, 42, CUSTOM_ERROR_MESSAGE);
        validator.ipv6(IPV6, CUSTOM_ERROR_MESSAGE);
        validator.ipv4(IPV4, CUSTOM_ERROR_MESSAGE);
        validator.email(EMAIL, CUSTOM_ERROR_MESSAGE);
        validator.match(MATCH, MATCH2, CUSTOM_ERROR_MESSAGE);
        validator.exactMatch(EXACT_MATCH, EXACT_MATCH2, CUSTOM_ERROR_MESSAGE);
        validator.max(MAX, 1, CUSTOM_ERROR_MESSAGE);
        validator.min(MIN, 42, CUSTOM_ERROR_MESSAGE);
        validator.required(REQUIRED, CUSTOM_ERROR_MESSAGE);
        validator.url(URL, CUSTOM_ERROR_MESSAGE);
        validator.numeric(NUMERIC, CUSTOM_ERROR_MESSAGE);
        
        //then
        assertThat(validator.getError(URL), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(REGEX), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(RANGE), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(IPV6), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(IPV4), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(EMAIL), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(MATCH), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(EXACT_MATCH), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(MAX), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(MIN), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(REQUIRED), equalTo(CUSTOM_ERROR_MESSAGE));
        assertThat(validator.getError(NUMERIC), equalTo(CUSTOM_ERROR_MESSAGE));
    }
    
    @Test
    public void testValidRequired() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(REQUIRED, REQUIRED);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(REQUIRED), equalTo(false));
    }
    
    @Test
    public void testInvalidRequired() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(REQUIRED, "");
        validator.required(REQUIRED);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(REQUIRED), equalTo(true));
    }

    @Test
    public void testValidMinString() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(MIN, "abcdef");
        validator.min(MIN, 4);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MIN), equalTo(false));
    }
    
    @Test
    public void testInvalidMinString() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(MIN, "abcdef");
        validator.min(MIN, 8);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MIN), equalTo(true));
    }

    @Test
    public void testValidMinNumeric() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(MIN, "6");
        validator.min(MIN, 4);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MIN), equalTo(false));
    }
    
    @Test
    public void testInvalidMinNumeric() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(MIN, "4");
        validator.min(MIN, 8);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MIN), equalTo(true));
    }

    @Test
    public void testValidMaxString() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(MAX, "abcdef");
        validator.max(MAX, 10);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MAX), equalTo(false));
    }
    
    @Test
    public void testInvalidMaxString() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(MAX, "abcdef");
        validator.max(MAX, 3);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MAX), equalTo(true));
    }

    @Test
    public void testValidMaxNumeric() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
            
        //when
        validator.add(MAX, "3");
        validator.max(MAX, 4);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MAX), equalTo(false));
    }
    
    @Test
    public void testInvalidMaxNumeric() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);

        //when
        validator.add(MAX, "4");
        validator.max(MAX, 2);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MAX), equalTo(true));
    }

    @Test
    public void testValidExactMatch() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(EXACT_MATCH, EXACT_MATCH);
        validator.exactMatch(EXACT_MATCH, EXACT_MATCH);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(EXACT_MATCH), equalTo(false));
    }

    @Test
    public void testInvalidExactMatch() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);

        //when
        validator.add(MATCH, MATCH);
        validator.add(MATCH2, MATCH2);
        validator.exactMatch(MATCH, MATCH2);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MATCH), equalTo(true));
    }

    @Test
    public void testValidMatch() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(MATCH, MATCH);
        validator.add(MATCH2, "mAtcH");
        validator.match(MATCH, MATCH2);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MATCH), equalTo(false));
    }

    @Test
    public void testInvalidMatch() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(MATCH, MATCH);
        validator.exactMatch(MATCH, "foo");

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MATCH), equalTo(true));
    }

    @Test
    public void testValidEmail() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(EMAIL, "foo@bar.com");
        validator.email(EMAIL);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(EMAIL), equalTo(false));
    }
    
    @Test
    public void testInvalidEmail() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);

        //when
        validator.add(EMAIL, "foo @");
        validator.exactMatch(EMAIL, "foo");

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(EMAIL), equalTo(true));
    }

    @Test
    public void testValidIpv4Address() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(IPV4, "192.168.2.1");
        validator.ipv4(IPV4);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(IPV4), equalTo(false));
    }
    
    @Test
    public void testInvalidIpv4Address() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);

        //when
        validator.add(IPV4, "192.189.383.122");
        validator.ipv4(IPV4);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(IPV4), equalTo(true));
    }

    @Test
    public void testValidIpv6Address() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(IPV6, "2001:0db8:85a3:08d3:1319:8a2e:0370:7344");
        validator.ipv6(IPV6);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(IPV6), equalTo(false));
    }
    
    @Test
    public void testInvalidIpv6Address() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(IPV6, "1f::::0");
        validator.ipv6(IPV6);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(IPV6), equalTo(true));
    }

    @Test
    public void testValidRangeString() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);

        //when
        validator.add(RANGE, "abcdefg");
        validator.range(RANGE, 4, 10);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(RANGE), equalTo(false));
    }
    
    @Test
    public void testInvalidRangeString() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(RANGE, "abcdef");
        validator.range(RANGE, 8, 12);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(RANGE), equalTo(true));
    }
    
    @Test
    public void testValidRangeNumeric() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(RANGE, "6");
        validator.range(RANGE, 4, 10);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(RANGE), equalTo(false));
    }

    @Test
    public void testInvalidRangeNumeric() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);

        //when
        validator.add(RANGE, "4");
        validator.range(RANGE, 8, 12);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(RANGE), equalTo(true));
    }

    @Test
    public void testValidRegex() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);

        //when
        validator.add(REGEX, "abc");
        validator.regex(REGEX, PATTERN);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(REGEX), equalTo(false));
    }

    @Test
    public void testInvalidRegex() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(REGEX, "abc03");
        validator.regex(REGEX, Pattern.compile("[a-z]"));

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(REGEX), equalTo(true));
    }

    @Test
    public void testValidUrl() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(URL, "https://mangoo.io");
        validator.url(URL);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(URL), equalTo(false));
    }
    
    @Test
    public void testInvalidUrl() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(URL, "https:/mangoo.io");
        validator.url(URL);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(URL), equalTo(true));
    }
    
    @Test
    public void testValidNumeric() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(NUMERIC, "2342");
        validator.numeric(NUMERIC);

        //then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(NUMERIC), equalTo(false));
    }
    
    @Test
    public void testInvalidNumeric() {
        //given
        Validator validator = Mangoo.TEST.getInstance(Validator.class);
        
        //when
        validator.add(NUMERIC, "asjcn");
        validator.numeric(NUMERIC);

        //then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(NUMERIC), equalTo(true));
    }
}