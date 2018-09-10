package io.mangoo.routing.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
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
    private static final String DOMAIN_NAME = "domainname";
    private static final String NUMERIC = "numeric";
    private static final String MIN = "min";
    private static final String REQUIRED = "required";
    private static final String CUSTOM_ERROR_MESSAGE = "Custom error message";

    @Test
    public void testDefaultErrorMessages() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(URL, "");
        validator.addValue(REGEX, "");
        validator.addValue(RANGE, "");
        validator.addValue(IPV6, "");
        validator.addValue(IPV4, "");
        validator.addValue(EMAIL, "");
        validator.addValue(MATCH, "");
        validator.addValue(EXACT_MATCH, "");
        validator.addValue(MAX, "abc");
        validator.addValue(MIN, "");
        validator.addValue(REQUIRED, "");
        validator.addValue(NUMERIC, "");
        validator.expectUrl(URL);
        validator.expectRegex(REGEX, PATTERN);
        validator.expectRange(RANGE, 23, 42);
        validator.expectIpv6(IPV6);
        validator.expectIpv4(IPV4);
        validator.expectEmail(EMAIL);
        validator.expectMatch(MATCH, MATCH2);
        validator.expectExactMatch(EXACT_MATCH, EXACT_MATCH2);
        validator.expectMax(MAX, 1);
        validator.expectMin(MIN, 42);
        validator.expectValue(REQUIRED);
        validator.expectUrl(URL);
        validator.expectNumeric(NUMERIC);

        // then
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
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(URL, "");
        validator.addValue(REGEX, "");
        validator.addValue(RANGE, "");
        validator.addValue(IPV6, "");
        validator.addValue(IPV4, "");
        validator.addValue(EMAIL, "");
        validator.addValue(MATCH, "");
        validator.addValue(MATCH2, "");
        validator.addValue(EXACT_MATCH, "");
        validator.addValue(EXACT_MATCH2, "");
        validator.addValue(MAX, "abc");
        validator.addValue(MIN, "");
        validator.addValue(REQUIRED, "");
        validator.addValue(NUMERIC, "");
        validator.expectUrl(URL, CUSTOM_ERROR_MESSAGE);
        validator.expectValue(REGEX, CUSTOM_ERROR_MESSAGE);
        validator.expectRange(RANGE, 23, 42, CUSTOM_ERROR_MESSAGE);
        validator.expectIpv6(IPV6, CUSTOM_ERROR_MESSAGE);
        validator.expectIpv4(IPV4, CUSTOM_ERROR_MESSAGE);
        validator.expectEmail(EMAIL, CUSTOM_ERROR_MESSAGE);
        validator.expectMatch(MATCH, MATCH2, CUSTOM_ERROR_MESSAGE);
        validator.expectExactMatch(EXACT_MATCH, EXACT_MATCH2, CUSTOM_ERROR_MESSAGE);
        validator.expectMax(MAX, 1, CUSTOM_ERROR_MESSAGE);
        validator.expectMin(MIN, 42, CUSTOM_ERROR_MESSAGE);
        validator.expectValue(REQUIRED, CUSTOM_ERROR_MESSAGE);
        validator.expectUrl(URL, CUSTOM_ERROR_MESSAGE);
        validator.expectNumeric(NUMERIC, CUSTOM_ERROR_MESSAGE);

        // then
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
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(REQUIRED, REQUIRED);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(REQUIRED), equalTo(false));
    }

    @Test
    public void testInvalidRequired() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(REQUIRED, "");
        validator.expectValue(REQUIRED);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(REQUIRED), equalTo(true));
    }

    @Test
    public void testValidMinString() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MIN, "abcdef");
        validator.expectMin(MIN, 4);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MIN), equalTo(false));
    }

    @Test
    public void testInvalidMinString() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MIN, "abcdef");
        validator.expectMin(MIN, 8);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MIN), equalTo(true));
    }

    @Test
    public void testValidMinNumeric() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MIN, "6");
        validator.expectMin(MIN, 4);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MIN), equalTo(false));
    }

    @Test
    public void testInvalidMinNumeric() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MIN, "4");
        validator.expectMin(MIN, 8);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MIN), equalTo(true));
    }

    @Test
    public void testValidMaxString() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MAX, "abcdef");
        validator.expectMax(MAX, 10);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MAX), equalTo(false));
    }

    @Test
    public void testInvalidMaxString() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MAX, "abcdef");
        validator.expectMax(MAX, 3);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MAX), equalTo(true));
    }

    @Test
    public void testValidMaxNumeric() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MAX, "3");
        validator.expectMax(MAX, 4);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MAX), equalTo(false));
    }

    @Test
    public void testInvalidMaxNumeric() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MAX, "4");
        validator.expectMax(MAX, 2);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MAX), equalTo(true));
    }

    @Test
    public void testValidDomainName() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(DOMAIN_NAME, "www.mangoo.io");
        validator.expectDomainName(DOMAIN_NAME);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(DOMAIN_NAME), equalTo(false));
    }

    @Test
    public void testInvalidDomainName() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(DOMAIN_NAME, "mangooio");
        validator.expectDomainName(DOMAIN_NAME);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(DOMAIN_NAME), equalTo(true));
    }

    @Test
    public void testValidExactMatch() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(EXACT_MATCH, EXACT_MATCH);
        validator.expectExactMatch(EXACT_MATCH, EXACT_MATCH);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(EXACT_MATCH), equalTo(false));
    }

    @Test
    public void testInvalidExactMatch() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MATCH, MATCH);
        validator.addValue(MATCH2, MATCH2);
        validator.expectExactMatch(MATCH, MATCH2);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MATCH), equalTo(true));
    }

    @Test
    public void testValidMatch() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MATCH, MATCH);
        validator.addValue(MATCH2, "mAtcH");
        validator.expectMatch(MATCH, MATCH2);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(MATCH), equalTo(false));
    }

    @Test
    public void testInvalidMatch() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(MATCH, MATCH);
        validator.expectExactMatch(MATCH, "foo");

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(MATCH), equalTo(true));
    }

    @Test
    public void testValidMatchValues() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue("foo", "bar");
        validator.expectMatch("foo", Arrays.asList("foobar", "bla", "bar"));

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError("foo"), equalTo(false));
    }

    @Test
    public void testInvalidMatchValues() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue("foo", "bar");
        validator.expectMatch("foo", Arrays.asList("foobar", "bla", "bra"));

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError("foo"), equalTo(true));
    }

    @Test
    public void testValidEmail() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(EMAIL, "foo@bar.com");
        validator.expectEmail(EMAIL);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(EMAIL), equalTo(false));
    }

    @Test
    public void testInvalidEmail() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(EMAIL, "foo @");
        validator.expectExactMatch(EMAIL, "foo");

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(EMAIL), equalTo(true));
    }

    @Test
    public void testValidIpv4Address() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(IPV4, "192.168.2.1");
        validator.expectIpv4(IPV4);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(IPV4), equalTo(false));
    }

    @Test
    public void testInvalidIpv4Address() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(IPV4, "192.189.383.122");
        validator.expectIpv4(IPV4);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(IPV4), equalTo(true));
    }

    @Test
    public void testValidIpv6Address() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(IPV6, "2001:0db8:85a3:08d3:1319:8a2e:0370:7344");
        validator.expectIpv6(IPV6);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(IPV6), equalTo(false));
    }

    @Test
    public void testInvalidIpv6Address() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(IPV6, "1f::::0");
        validator.expectIpv6(IPV6);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(IPV6), equalTo(true));
    }

    @Test
    public void testValidRangeString() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(RANGE, "abcdefg");
        validator.expectRange(RANGE, 4, 10);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(RANGE), equalTo(false));
    }

    @Test
    public void testInvalidRangeString() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(RANGE, "abcdef");
        validator.expectRange(RANGE, 8, 12);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(RANGE), equalTo(true));
    }

    @Test
    public void testValidRangeNumeric() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(RANGE, "6");
        validator.expectRange(RANGE, 4, 10);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(RANGE), equalTo(false));
    }

    @Test
    public void testInvalidRangeNumeric() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(RANGE, "4");
        validator.expectRange(RANGE, 8, 12);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(RANGE), equalTo(true));
    }

    @Test
    public void testValidRegex() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(REGEX, "abc");
        validator.expectRegex(REGEX, PATTERN);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(REGEX), equalTo(false));
    }

    @Test
    public void testInvalidRegex() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(REGEX, "abc03");
        validator.expectRegex(REGEX, Pattern.compile("[a-z]"));

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(REGEX), equalTo(true));
    }

    @Test
    public void testValidUrl() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(URL, "https://mangoo.io");
        validator.expectUrl(URL);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(URL), equalTo(false));
    }

    @Test
    public void testInvalidUrl() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(URL, "https:/mangoo.io");
        validator.expectUrl(URL);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(URL), equalTo(true));
    }

    @Test
    public void testValidNumeric() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(NUMERIC, "2342");
        validator.expectNumeric(NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(NUMERIC), equalTo(false));
    }

    @Test
    public void testInvalidNumeric() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(NUMERIC, "asjcn");
        validator.expectNumeric(NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(NUMERIC), equalTo(true));
    }

    @Test
    public void testValidValidateTrue() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(NUMERIC, "2342");
        validator.validateTrue(true, NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(NUMERIC), equalTo(false));
    }

    @Test
    public void testInvalidValidateTrue() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(NUMERIC, "2342");
        validator.validateTrue(false, NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(NUMERIC), equalTo(true));
    }

    @Test
    public void testValidValidateFalse() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(NUMERIC, "2342");
        validator.validateFalse(false, NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(NUMERIC), equalTo(false));
    }

    @Test
    public void testInvalidValidateFalse() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        validator.addValue(NUMERIC, "2342");
        validator.validateFalse(true, NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(NUMERIC), equalTo(true));
    }

    @Test
    public void testValidValidateNotNull() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        String foo = "kfkfkf";
        validator.addValue(NUMERIC, "2342");
        validator.validateNotNull(foo, NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(NUMERIC), equalTo(false));
    }

    @Test
    public void testInvalidValidateNotNull() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        String foo = null;
        validator.addValue(NUMERIC, "2342");
        validator.validateNotNull(foo, NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(NUMERIC), equalTo(true));
    }

    @Test
    public void testValidValidateNull() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        String foo = null;
        validator.addValue(NUMERIC, "2342");
        validator.validateNull(foo, NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(false));
        assertThat(validator.hasError(NUMERIC), equalTo(false));
    }

    @Test
    public void testInvalidValidateNull() {
        // given
        Validator validator = Application.getInstance(Validator.class);

        // when
        String foo = "fdfdsfd";
        validator.addValue(NUMERIC, "2342");
        validator.validateNull(foo, NUMERIC);

        // then
        assertThat(validator.hasErrors(), equalTo(true));
        assertThat(validator.hasError(NUMERIC), equalTo(true));
    }
}