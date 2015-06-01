package mangoo.io.routing.bindings;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mangoo.io.i18n.Messages;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 *
 * @author svenkubiak
 *
 */
public class Form {
    private static final Logger LOG = LoggerFactory.getLogger(Form.class);
    private static final int IPV4_MAX = 255;
    private static final int IPV4_MIN = 0;
    private static final int IPV4_PARTS = 4;
    private static final Pattern emailPattern = Pattern.compile("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[a-zA-Z0-9](?:[\\w-]*[\\w])?");
    private static final Pattern urlPattern = Pattern.compile("^(http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(:[a-zA-Z0-9]*)?/?([a-zA-Z0-9\\-\\._\\?\\,\\'/\\\\\\+&amp;%\\$#\\=~\\!])*$");
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

    public void required(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: required");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (StringUtils.isBlank(StringUtils.trimToNull(value))) {
            this.errors.put(fieldName, messages.get("form.required", fieldName));
        }
    }

    public void min(int minLength, String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: min");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (value.length() < minLength) {
            this.errors.put(fieldName, messages.get("form.min", fieldName));
        }
    }

    public void max(int maxLength, String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: max");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (value.length() > maxLength) {
            this.errors.put(fieldName, messages.get("form.max", fieldName));
        }
    }

    public void exactMatch(String fieldName, String anotherFieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: match");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        String anotherValue = (get(anotherFieldName) == null) ? "" : get(anotherFieldName);
        if (value.equals(anotherValue)) {
            this.errors.put(fieldName, messages.get("form.exactMatch", fieldName));
        }
    }

    public void match(String fieldName, String anotherFieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: exactMatch");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        String anotherValue = (get(anotherFieldName) == null) ? "" : get(anotherFieldName);
        if (value.equalsIgnoreCase(anotherValue)) {
            this.errors.put(fieldName, messages.get("form.match", fieldName));
        }
    }

    public void email(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: email");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (!emailPattern.matcher(value).matches()) {
            this.errors.put(fieldName, messages.get("form.email", fieldName));
        }
    }

    public void ipv4(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: ipv4");

        boolean valid = true;
        String value = (get(fieldName) == null) ? "" : get(fieldName);
        try {
            String[] parts = value.split("[.]");
            if (parts.length != IPV4_PARTS) {
                valid = false;
            }

            for (int i=0; i < parts.length; i++) {
                int p = Integer.parseInt(parts[i]);
                if(p < IPV4_MIN || p > IPV4_MAX) {
                    valid = false;
                }
            }
        } catch (NumberFormatException e) {
            LOG.error("Invalid IPv4 address", e);
            valid = false;
        }

        if (!valid) {
            this.errors.put(fieldName, messages.get("form.ipv4", fieldName));
        }
    }

    public void ipv6(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: ipv6");

        boolean valid = true;
        String value = (get(fieldName) == null) ? "" : get(fieldName);
        try {
            InetAddress inetAddress = InetAddress.getByName(value);
            if (inetAddress instanceof Inet6Address) {
                valid = false;
            }
        } catch (UnknownHostException e) {
            LOG.error("Invalid IPv6 address", e);
            valid = false;
        }

        if (!valid) {
            this.errors.put(fieldName, messages.get("form.ipv6", fieldName));
        }
    }

    public void range(int minLength, int maxLength, String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: rang");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (value.length() >= minLength && value.length() <= maxLength) {
            this.errors.put(fieldName, messages.get("form.url", fieldName));
        }
    }

    public void url(String fieldName) {
        Preconditions.checkNotNull(fieldName, "Fieldname is required for validation: url");

        String value = (get(fieldName) == null) ? "" : get(fieldName);
        if (!urlPattern.matcher(value).matches()) {
            this.errors.put(fieldName, messages.get("form.url", fieldName));
        }
    }

    public boolean hasErrors() {
        return this.errors.size() > 0;
    }
}