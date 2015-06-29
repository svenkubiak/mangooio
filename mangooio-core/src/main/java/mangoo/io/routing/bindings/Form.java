package mangoo.io.routing.bindings;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import mangoo.io.enums.Key;
import mangoo.io.i18n.Messages;

/**
 *
 * @author svenkubiak
 *
 */
public class Form {
	private static final Pattern ipv4Pattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Pattern emailPattern = Pattern.compile("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[a-zA-Z0-9](?:[\\w-]*[\\w])?");
    private static final Pattern urlPattern = Pattern.compile("^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~\\!])*$");
    private boolean submitted;
    private List<File> files = new ArrayList<File>();
    private Map<String, String> values = new HashMap<String, String>();
    private Map<String, String> errors = new HashMap<String, String>();

    @Inject
    private Messages messages;

    public void add(String key, String value) {
        this.values.put(key, value);
    }

    public String get(String key) {
        return this.values.get(key);
    }

    public List<File> getFiles() {
        return this.files;
    }

    public File getFile() {
        File file = null;
        if (!this.files.isEmpty()) {
            file = this.files.get(0);
        }

        return file;
    }

    public void addFile(File file) {
        this.files.add(file);
    }
    
    public boolean hasError(String fieldName) {
    	return this.errors.containsKey(fieldName);
    }
    
    public String getError(String fieldName) {
    	return (hasError(fieldName)) ? this.errors.get(fieldName) : "";
    }
    
    public String getValue(String fieldName) {
    	return this.values.get(fieldName);
    }

    public void required(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: required");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (StringUtils.isBlank(StringUtils.trimToNull(value))) {
            this.errors.put(fieldName, messages.get(Key.FORM_REQUIRED, fieldName));
        }
    }

    public void min(int minLength, String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: min");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (value.length() < minLength) {
            this.errors.put(fieldName, messages.get(Key.FORM_MIN, fieldName, minLength));
        }
    }

    public void max(int maxLength, String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: max");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (value.length() > maxLength) {
            this.errors.put(fieldName, messages.get(Key.FORM_MAX, fieldName, maxLength));
        }
    }

    public void exactMatch(String fieldName, String anotherFieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: match");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        String anotherValue = (get(anotherFieldName) == null) ? "" : get(anotherFieldName);
        if (StringUtils.isBlank(value) || StringUtils.isBlank(anotherValue) && !value.equals(anotherValue)) {
            this.errors.put(fieldName, messages.get(Key.FORM_EXACT_MATCH, fieldName, anotherFieldName));
        }
    }

    public void match(String fieldName, String anotherFieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: exactMatch");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        String anotherValue = (get(anotherFieldName) == null) ? "" : get(anotherFieldName);
        if (StringUtils.isBlank(value) || StringUtils.isBlank(anotherValue) && !value.equalsIgnoreCase(anotherValue)) {
            this.errors.put(fieldName, messages.get(Key.FORM_MATCH, fieldName, anotherFieldName));
        }
    }

    public void email(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: email");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (!emailPattern.matcher(value).matches()) {
            this.errors.put(fieldName, messages.get(Key.FORM_EMAIL, fieldName));
        }
    }

    public void ipv4(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: ipv4");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        Matcher matcher = ipv4Pattern.matcher(value);

        if (!matcher.matches()) {
            this.errors.put(fieldName, messages.get(Key.FORM_IPV4, fieldName));
        }
    }

    public void ipv6(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: ipv6");

        boolean valid = false;
        String value = (get(fieldName) == null) ? "" : get(fieldName);
        try {
            InetAddress inetAddress = InetAddress.getByName(value);
            if (inetAddress instanceof Inet6Address) {
                valid = true;
            }
        } catch (UnknownHostException e) {
        	//intentionally left blank
        }

        if (!valid) {
            this.errors.put(fieldName, messages.get(Key.FORM_IPV6, fieldName));
        }
    }

    public void range(int minLength, int maxLength, String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: range");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (value.length() >= minLength && value.length() <= maxLength) {
            this.errors.put(fieldName, messages.get(Key.FORM_RANGE, fieldName, minLength, maxLength));
        }
    }

    public void url(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: url");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (!urlPattern.matcher(value).matches()) {
            this.errors.put(fieldName, messages.get(Key.FORM_URL, fieldName));
        }
    }

    public boolean hasErrors() {
        return this.submitted && this.errors.size() > 0;
    }

    public Map<String, String> getValues() {
        return this.values;
    }

	public boolean hasContent() {
		return this.errors.size() > 0 && this.values.size() > 0;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}
}