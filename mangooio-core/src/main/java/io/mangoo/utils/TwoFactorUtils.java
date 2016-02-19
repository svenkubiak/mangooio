package io.mangoo.utils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.enums.ErrorMessage;

/**
 * Two factor Java implementation for the Time-based One-Time Password (TOTP) algorithm.
 *
 * See: https://github.com/j256/java-two-factor-auth
 *
 * Copyright 2015, Gray Watson
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby
 * granted provided that the above copyright notice and this permission notice appear in all copies. THE SOFTWARE IS
 * PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT,
 * OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION
 * OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 *
 * @author graywatson, svenkubiak, WilliamDunne
 */
@SuppressWarnings("all")
public final class TwoFactorUtils {
    private static final Logger LOG = LogManager.getLogger(TwoFactorUtils.class);
    private static final Base32 base32 = new Base32();
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String BLOCK_OF_ZEROS = "000000";
    private static final int TIME_STEP_SECONDS = 30;
    private static final boolean USE_SHA1_THREAD_LOCAL = true;
    private static final ThreadLocal<Mac> MAC_THREAD_LOCAL = new ThreadLocal<Mac>() {
        @Override
        protected Mac initialValue() {
            try {
                return Mac.getInstance(HMAC_SHA1);
            } catch (final NoSuchAlgorithmException e) {
                LOG.error("Unknown message authentication code instance", e);
            }
            
            return null;
        }
    };

    private TwoFactorUtils() {
    }

    /**
     * Uses default length of 16, to generate the Base32 secret
     * 
     * @return Generate a 16 character secret key in base32 format
     */
    public static String generateBase32Secret() {
        return generateBase32Secret(16);
    }

    /**
     * method to generate Base32 secret keys for the user
     * @param length how long the key should be
     * 
     * @return Generate a secret key in base32 format (A-Z, 2-7)
     */
    public static String generateBase32Secret(int length) {
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final int value = new SecureRandom().nextInt(32);
            if (value < 26) { 
                buffer.append((char) ('A' + value));
            } else {
                buffer.append((char) ('2' + (value - 26))); 
            }
        }

        return buffer.toString();
    }

    /**
     * Validate a given code using the secret, defaults to window of 3 either side,
     * allowing a margin of error equivalent to three windows to adjust for time
     * discrepancies.
     * 
     * Uses the current time.
     * 
     * @param number code provided by user
     * @param secret the secret for the users code
     * 
     * @return boolean if the code is valid
     */
    public static boolean validateCurrentNumber(int number, String secret) {
        return validateCurrentNumber(number, secret, 3);
    }

    /**
     * Validate a given code at a specific time, and specific window
     * 
     * @param number the code provided by the user.
     * @param secret the secret used to generate the users code
     * @param window the number of windows to check around the time
     * @param time the time in milliseconds at which the code should be checked
     * 
     * @return True if the code is valid within the timeframe, false otherwise
     */
    public static boolean validateNumber(int number, String secret, int window, long time) {
        try {
            int current = Integer.parseInt(generateCurrentNumber(secret, time));
            if (number == current) {
                return true;
            } else if(validateCurrentNumberLow(number, secret, window - 1, time - TIME_STEP_SECONDS * 1000)) {
                return true;
            } else if(validateCurrentNumberHigh(number, secret, window - 1, time + TIME_STEP_SECONDS * 1000)) {
                return true;
            }
        }
        catch(GeneralSecurityException e) {
            LOG.error("Failed to validate number", e);
        }

        return false;
    }
    /**
     * Validate a given code using the secret, provided number, and number of windows
     * to check. Uses currentTimeMillis for time
     * 
     * @param number the code provided by the user
     * @param secret the secret for the users code
     * @param window the number of windows to check around the time
     * 
     * @return True if the code is correct, false otherwise
     */
    public static boolean validateCurrentNumber(int number, String secret, int window) {
        long time = System.currentTimeMillis();

        return validateNumber(number, secret, window, time);
    }

    private static boolean validateCurrentNumberLow(int number, String secret, int window, Long time) throws GeneralSecurityException {
        int current = Integer.parseInt(generateCurrentNumber(secret, time));
        if (current == number) {
            return true;
        } else {
            if (window > 0) {
                return validateCurrentNumberLow(number, secret, window - 1, time - TIME_STEP_SECONDS * 1000);
            }
        }
        
        return false;
    }

    private static boolean validateCurrentNumberHigh(int number, String secret, int window, long time) throws GeneralSecurityException {
        int current = Integer.parseInt(generateCurrentNumber(secret, time));
        if (current == number) {
            return true;
        } else {
            if (window > 0) {
                return validateCurrentNumberHigh(number, secret, window - 1, time + TIME_STEP_SECONDS * 1000);
            }
        }
        
        return false;
    }

    /**
     * Return the current number to be checked against the user input, using the
     * time found in System.currentTimeMillis()
     *
     * WARNING: This requires a system clock that is in sync with the world.
     *
     * For more details of this magic algorithm, see:
     * http://en.wikipedia.org/wiki/Time-based_One-time_Password_Algorithm
     * 
     * @param secret The secret to use
     * 
     * @return The current number to be checked
     */
    public static String getNumber(String secret) {
        Objects.requireNonNull(secret, ErrorMessage.SECRET.toString());
        
        return generateCurrentNumber(secret, System.currentTimeMillis());
    }

    /**
     * Return the current number to be checked. This can be compared against user input.
     *
     * WARNING: This requires a system clock that is in sync with the world.
     *
     * @param secret The secret to use
     */
    public static String generateCurrentNumber(String secret) {
        return generateCurrentNumber(secret, System.currentTimeMillis());
    }

    /**
     * Same as {@link #getNumber(String)} except at a particular time in milliseconds
     * 
     * @param secret The secret to use
     * @param currentTimeMillis A provided time in milli seconds
     * 
     * @return The current number to be checked
     */
    public static String generateCurrentNumber(String secret, long currentTimeMillis) {
        Objects.requireNonNull(secret, ErrorMessage.SECRET.toString());

        byte[] key = null;
        try {
            key = base32.decode(secret.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to decode secrete to base32", e);
        }
        
        final byte[] data = new byte[8]; 
        long value = currentTimeMillis / 1000 / TIME_STEP_SECONDS; 
        for (int i = 7; value > 0; i--) { 
            data[i] = (byte) (value & 0xFF);
            value >>= 8; 
        }

        final SecretKeySpec signKey = new SecretKeySpec(key, HMAC_SHA1);
        Mac mac;
        byte[] hash = null;
        try {
            if (USE_SHA1_THREAD_LOCAL) {
                mac = MAC_THREAD_LOCAL.get();
            } else {
                mac = Mac.getInstance(HMAC_SHA1);
            }
            mac.init(signKey);
            hash = mac.doFinal(data);
        } catch (final GeneralSecurityException e) {
            LOG.error("Failed to encrypt data with key", e);
        }

        long truncatedHash = 0;
        final int offset = hash[hash.length - 1] & 0xF;
        for (int i = offset; i < offset + 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000; 

        return zeroPrepend(truncatedHash, 000000); 
    }

    /**
     * Return the QR image URL from Google Charts API.
     * 
     * This can be shown to the user and scanned by the authenticator program as an easy way to enter the secret
     * 
     * @param accountName The account name used to display to the user
     * @param secret The secret to use
     * 
     * @return A URL to the Google charts API
     */
    public static String getQRCode(String accountName, String secret) {
        Objects.requireNonNull(accountName, "accountName can not be null");
        Objects.requireNonNull(secret, "secret can not be null");
        
        final StringBuilder buffer = new StringBuilder(128);
        buffer.append("https://chart.googleapis.com/chart")
            .append("?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=")
            .append("otpauth://totp/")
            .append(accountName)
            .append("?secret=")
            .append(secret);

        return buffer.toString();
    }

    /**
     * @return A string prepended with 0s. Tested as 10x faster than String.format("%06d", ...);
     */
    private static String zeroPrepend(long num, int digits) {
        final String hash = Long.toString(num);
        if (hash.length() >= digits) {
            return hash;
        } else {
            final int zeroCount = digits - hash.length();
            final StringBuilder buffer = new StringBuilder(digits)
                    .append(BLOCK_OF_ZEROS, 0, zeroCount)
                    .append(hash);

            return buffer.toString();
        }
    }
}