package io.mangoo.utils;
    
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.RegExUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;

import io.mangoo.crypto.totp.TOTP;
import io.mangoo.enums.HmacShaAlgorithm;
import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
public class TotpUtils {
    private static final Logger LOG = LogManager.getLogger(TotpUtils.class);
    private static final Base32 base32 = new Base32();
    private static final int DIGITS = 6;
    private static final int MAX_CHARACTERS = 32;
    private static final int THIRTY_SECONDS = 30;
    private static final int ITERATIONS = 26;
    private static final int BYTES_SECRET = 64;
    
    private TotpUtils() {
    }
    
    /**
     * Generates a 64 byte (512 bit) secret, best used with HMAC_SHA512
     * 
     * @return A 64 characters random string based on SecureRandom
     */
    public static Optional<String> createSecret() {
        Random random = new SecureRandom();
        StringBuilder buffer = new StringBuilder(BYTES_SECRET);
        for (int i = 0; i < BYTES_SECRET; i++) {
            int value = random.nextInt(MAX_CHARACTERS);
            if (value < ITERATIONS) {
                buffer.append((char) ('A' + value));
            } else {
                buffer.append((char) ('2' + (value - ITERATIONS)));
            }
        }
        
        return Optional.of(buffer.toString());
    }
    
    /**
     * Creates the current TOTP based on the given secret and HMAC algorithm
     * 
     * @param secret The secret to use
     * @param hmacShaAlgorithm The HMAC algorithm to use
     * 
     * @return The totp value or null if generation failed
     */
    public static Optional<String> getTotp(String secret, HmacShaAlgorithm hmacShaAlgorithm) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        Objects.requireNonNull(hmacShaAlgorithm, Required.ALGORITHM.toString());
        
        String value = null;
        try {
            TOTP builder = TOTP.key(secret.getBytes(Charsets.US_ASCII.toString()))
                    .timeStep(TimeUnit.SECONDS.toMillis(THIRTY_SECONDS))
                    .digits(DIGITS)
                    .hmacSha(hmacShaAlgorithm)
                    .build();
            
            value = builder.value();
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to create TOTP",  e);
        }
        
        return Optional.ofNullable(value);
    }
    
    /**
     * Verifies a given TOTP based on a given secret and HMAC algorithm
     * 
     * @param secret The secret to use
     * @param totp The TOTP to verify
      * @param hmacShaAlgorithm The HMAC algorithm to use
      * 
     * @return True if the TOTP is valid, false otherwise
     */
    public static boolean verifiedTotp(String secret, String totp, HmacShaAlgorithm hmacShaAlgorithm) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        Objects.requireNonNull(totp, Required.TOTP.toString());
        Objects.requireNonNull(hmacShaAlgorithm, Required.ALGORITHM.toString());
        
        String value = null;
        try {
            TOTP builder = TOTP.key(secret.getBytes(Charsets.US_ASCII.toString()))
                .timeStep(TimeUnit.SECONDS.toMillis(THIRTY_SECONDS))
                .digits(DIGITS)
                .hmacSha(hmacShaAlgorithm)
                .build();
            
            value = builder.value();
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to verify TOTP",  e);
        }
        
        return totp.equals(value);
    }
    
    /**
     * Generates a QR code to share a secret with a user
     * 
     * @param name The name of the account
     * @param issuer The name of the issuer
     * @param secret The secret to use
     * @param hmacShaAlgorithm The HMAC algorithm to use
     * 
     * @return An URL to google charts API with the QR code
     */
    public static String getQRCode(String name, String issuer, String secret, HmacShaAlgorithm hmacShaAlgorithm) {
        Objects.requireNonNull(name, Required.ACCOUNT_NAME.toString());
        Objects.requireNonNull(secret, Required.SECRET.toString());
        Objects.requireNonNull(issuer, Required.ISSUER.toString());
        Objects.requireNonNull(hmacShaAlgorithm, Required.ALGORITHM.toString());
        
        final StringBuilder buffer = new StringBuilder();
        buffer.append("https://chart.googleapis.com/chart")
            .append("?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=")
            .append(getOtpauthURL(name, issuer, secret, hmacShaAlgorithm));
        
        return buffer.toString();
    }
    
    /**
     * Generates a otpauth code to share a secret with a user
     * 
     * @param name The name of the account
     * @param issuer The name of the issuer
     * @param secret The secret to use
     * @param hmacShaAlgorithm The HMAC algorithm to use
     * 
     * @return An otpauth url
     */
    public static String getOtpauthURL(String name, String issuer, String secret, HmacShaAlgorithm hmacShaAlgorithm) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("otpauth://totp/")
            .append(name)
            .append("?secret=")
            .append(RegExUtils.replaceAll(base32.encodeAsString(secret.getBytes(Charsets.UTF_8)), "=", ""))
                .append("&algorithm=")
                .append(hmacShaAlgorithm.getAlgorithm())
                .append("&issuer=")
                .append(issuer);
        
        return buffer.toString();
    }
}
