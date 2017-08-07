package io.mangoo.crypto.totp;

import com.google.common.base.Preconditions;

/**
 * An immutable class representing a One Time Password (OTP) key. An OTP key
 * consists of:
 * <ul>
 * <li>The shared secret key,</li>
 * <li>The type of OTP (either HOTP or TOTP)</li>
 * </ul>
 */
public final class OTPKey {
    private final String key;
    private final OTPType type;

    /**
     * Creates a new instance of an OTP key of type HOTP or TOTP.
     * 
     * @param key
     *            the encoded shared secret key used to generate an OTP
     * @param type
     *            the type of OTP
     */
    public OTPKey(String key, OTPType type) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(type);
        this.key = key;
        this.type = type;
    }

    /**
     * Returns the encoded shared secret key used to generate an OTP.
     * @return the encoded shared secret key used to generate an OTP.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the type of OTP.
     * @return the type of OTP.
     */
    public OTPType getType() {
        return type;
    }

    /**
     * Type of One Time Password. Valid types are either HOTP or TOTP.
     */
    public enum OTPType {
        HOTP("HOTP"),
        TOTP("TOTP");

        private final String name;

        OTPType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public OTPType from(String name) {
            for (OTPType type : values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No matching OTPType constant for [" + name + "]");
        }

        @Override
        public String toString() {
            return name;
        }
    }
}