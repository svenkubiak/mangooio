package io.mangoo.utils;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.cache.Cache;

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
 * @author graywatson
 */
public final class TwoFactorUtils {
    private static final Logger LOG = LogManager.getLogger(Cache.class);
    private static final Base32 base32 = new Base32();
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String BLOCK_OF_ZEROS = "000000";
    private static final int TIME_STEP_SECONDS = 30;
    private static final boolean USE_SHA1_THREAD_LOCAL = true;
    private static final ThreadLocal<Mac> MAC_THREAD_LOCAL = new ThreadLocal<Mac>() {
        @Override
        protected Mac initialValue() {
            final String name = HMAC_SHA1;
            try {
                return Mac.getInstance(name);
            } catch (final NoSuchAlgorithmException e) {
                throw new RuntimeException("Unknown message authentication code instance: " + name, e);
            }
        }
    };

    /**
     * @return Generate a secret key in base32 format (A-Z2-7)
     */
    public static String generateBase32Secret() {
        final Random random = new SecureRandom();
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            final int val = random.nextInt(32);
            if (val < 26) {
                buffer.append((char) ('A' + val));
            } else {
                buffer.append((char) ('2' + (val - 26)));
            }
        }

        return buffer.toString();
    }

    /**
     * Return the current number to be checked. This can be compared against user input.
     *
     * WARNING: This requires a system clock that is in sync with the world.
     *
     * For more details of this magic algorithm, see:
     * http://en.wikipedia.org/wiki/Time-based_One-time_Password_Algorithm
     */
    public static String generateCurrentNumber(String secret) {
        return generateCurrentNumber(secret, System.currentTimeMillis());
    }

    /**
     * Same as {@link #generateCurrentNumber(String)} except at a particular time in millis. Mostly for testing
     * purposes.
     */
    private static String generateCurrentNumber(String secret, long currentTimeMillis) {
        Objects.requireNonNull(secret, "secret can not be null");

        final byte[] key = base32.decode(secret.getBytes());
        final byte[] data = new byte[8];

        long value = currentTimeMillis / 1000 / TIME_STEP_SECONDS;
        for (int i = 7; value > 0; i--) {
            data[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        // encrypt the data with the key and return the SHA1 of it in hex
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

        // take the 4 least significant bits from the encrypted string as an offset
        final int offset = hash[hash.length - 1] & 0xF;

        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = offset; i < offset + 4; ++i) {
            truncatedHash <<= 8;
            // get the 4 bytes at the offset
            truncatedHash |= (hash[i] & 0xFF);
        }
        // cut off the top bit
        truncatedHash &= 0x7FFFFFFF;

        // the token is then the last 6 digits in the number
        truncatedHash %= 1000000;

        return zeroPrepend(truncatedHash, 000000);
    }

    /**
     * Return the QR image url thanks to Google. This can be shown to the user and scanned by the authenticator program
     * as an easy way to enter the secret.
     *
     * NOTE: this must be URL escaped if it is to be put into a href on a web-page.
     */
    public String getQRCode(String keyId, String secret) {
        final StringBuilder buffer = new StringBuilder(128);
        buffer.append("https://chart.googleapis.com/chart")
            .append("?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=")
            .append("otpauth://totp/")
            .append(keyId)
            .append("?secret=")
            .append(secret);

        return buffer.toString();
    }

    /**
     * Return the string prepended with 0s. Tested as 10x faster than String.format("%06d", ...); Exposed for testing.
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