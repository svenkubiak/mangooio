package io.mangoo.utils;
    
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.RegExUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
     * Generates a 64 byte (512 bit) secret
     * 
     * @return A 64 characters random string based on SecureRandom
     */
    public static String createSecret() {
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
        
        return buffer.toString();
    }
    
    /**
     * Creates the current TOTP based on the given secret and HMAC algorithm
     * 
     * @param secret The secret to use
     * 
     * @return The totp value or null if generation failed
     */
    public static String getTotp(String secret) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        
        String value = null;
        try {
            TOTP builder = TOTP.key(secret.getBytes(StandardCharsets.US_ASCII.name()))
                    .timeStep(TimeUnit.SECONDS.toMillis(THIRTY_SECONDS))
                    .digits(DIGITS)
                    .hmacSha(HmacShaAlgorithm.HMAC_SHA_512)
                    .build();
            
            value = builder.value();
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to create TOTP",  e);
        }
        
        return value;
    }
    
    /**
     * Verifies a given TOTP based on a given secret and HMAC algorithm
     * 
     * @param secret The secret to use
     * @param totp The TOTP to verify
     * 
     * @return True if the TOTP is valid, false otherwise
     */
    public static boolean verifiedTotp(String secret, String totp) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        Objects.requireNonNull(totp, Required.TOTP.toString());
        
        String value = null;
        try {
            TOTP builder = TOTP.key(secret.getBytes(StandardCharsets.US_ASCII.name()))
                .timeStep(TimeUnit.SECONDS.toMillis(THIRTY_SECONDS))
                .digits(DIGITS)
                .hmacSha(HmacShaAlgorithm.HMAC_SHA_512)
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
     * 
     * @return An URL to google charts API with the QR code
     */
    public static String getQRCode(String name, String issuer, String secret) {
        Objects.requireNonNull(name, Required.ACCOUNT_NAME.toString());
        Objects.requireNonNull(secret, Required.SECRET.toString());
        Objects.requireNonNull(issuer, Required.ISSUER.toString());
        
        final StringBuilder buffer = new StringBuilder();
        buffer.append("https://chart.googleapis.com/chart")
            .append("?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=")
            .append(getOtpauthURL(name, issuer, secret));
        
        return buffer.toString();
    }
    
    /**
     * Generates a otpauth code to share a secret with a user
     * 
     * @param name The name of the account
     * @param issuer The name of the issuer
     * @param secret The secret to use
     * 
     * @return An otpauth url
     */
    public static String getOtpauthURL(String name, String issuer, String secret) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("otpauth://totp/")
            .append(name)
            .append("?secret=")
            .append(RegExUtils.replaceAll(base32.encodeAsString(secret.getBytes(StandardCharsets.UTF_8)), "=", ""))
                .append("&algorithm=")
                .append(HmacShaAlgorithm.HMAC_SHA_512.getAlgorithm())
                .append("&issuer=")
                .append(issuer);
        
        String url = "";
        try {
            url = URLEncoder.encode(buffer.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to encode otpauth url", e);
        }
        
        return url;
    }
}