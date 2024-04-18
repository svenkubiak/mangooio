package io.mangoo.utils.totp;

import io.mangoo.enums.HmacShaAlgorithm;
import io.mangoo.enums.Required;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.RegExUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TotpUtils {
    private static final Logger LOG = LogManager.getLogger(TotpUtils.class);
    private static final Base32 base32 = new Base32();
    private static final Random random = new SecureRandom();
    private static final HmacShaAlgorithm ALGORITHM = HmacShaAlgorithm.HMAC_SHA_512;
    private static final int DIGITS = 6;
    private static final int MAX_CHARACTERS = 32;
    private static final int PERIOD = 30;
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
        var buffer = new StringBuilder(BYTES_SECRET);
        for (var i = 0; i < BYTES_SECRET; i++) {
            var value = random.nextInt(MAX_CHARACTERS);
            if (value < ITERATIONS) {
                buffer.append((char) ('A' + value));
            } else {
                buffer.append((char) ('2' + (value - ITERATIONS)));
            }
        }
        
        return buffer.toString();
    }
    
    /**
     * Creates the current TOTP based on the following default values:
     * SHA512 algorithm, 6 digits, 30 seconds time period
     * 
     * @param secret The secret to use
     * 
     * @return The totp value or null if generation failed
     */
    public static String getTotp(String secret) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        
        String value = null;
        try {
            Totp builder = Totp.key(secret.getBytes(StandardCharsets.US_ASCII.name()))
                    .timeStep(TimeUnit.SECONDS.toMillis(PERIOD))
                    .digits(DIGITS)
                    .hmacSha(ALGORITHM)
                    .build();
            
            value = builder.value();
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to create TOTP",  e);
        }
        
        return value;
    }
    
    /**
     * Creates the current TOTP based on the given parameters
     * 
     * @param secret The secret to use
     * @param algorithm The algorithm to use
     * @param digits The digits to use (6 or 8)
     * @param period The time period in seconds
     * 
     * @return The totp value or null if generation failed
     */
    public static String getTotp(String secret, HmacShaAlgorithm algorithm, int digits, int period) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        Objects.requireNonNull(algorithm, Required.ALGORITHM.toString());
        
        String value = null;
        try {
            Totp builder = Totp.key(secret.getBytes(StandardCharsets.US_ASCII.name()))
                    .timeStep(TimeUnit.SECONDS.toMillis(period))
                    .digits(digits)
                    .hmacSha(algorithm)
                    .build();
            
            value = builder.value();
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to create TOTP",  e);
        }
        
        return value;
    }
    
    /**
     * Verifies a given TOTP based on the following default values:
     * SHA512 algorithm, 6 digits, 30 seconds time period
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
            Totp builder = Totp.key(secret.getBytes(StandardCharsets.US_ASCII.name()))
                .timeStep(TimeUnit.SECONDS.toMillis(PERIOD))
                .digits(DIGITS)
                .hmacSha(ALGORITHM)
                .build();
            
            value = builder.value();
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to verify TOTP",  e);
        }
        
        return totp.equals(value);
    }
    
    /**
     * Generates a QR code link from Google charts API to share a secret with a user
     * 
     * @param name The name of the account
     * @param issuer The name of the issuer
     * @param secret The secret to use
     * @param algorithm The algorithm to use
     * @param digits The number of digits to use
     * @param period The period to use
     * 
     * @return A URL to Google charts API with the QR code
     */
    public static String getQRCode(String name, String issuer, String secret, HmacShaAlgorithm algorithm, String digits, String period) {
        Objects.requireNonNull(name, Required.ACCOUNT_NAME.toString());
        Objects.requireNonNull(secret, Required.SECRET.toString());
        Objects.requireNonNull(issuer, Required.ISSUER.toString());
        Objects.requireNonNull(algorithm, Required.ALGORITHM.toString());
        Objects.requireNonNull(digits, Required.DIGITS.toString());
        Objects.requireNonNull(period, Required.PERIOD.toString());
        
        var buffer = new StringBuilder();
            buffer
                .append("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=")
                .append(getOtpauthURL(name, issuer, secret, algorithm, digits, period));

        return buffer.toString();
    }
    
    /**
     * Generates an otpauth code to share a secret with a user
     * 
     * @param name The name of the account
     * @param issuer The name of the issuer
     * @param secret The secret to use
     * @param algorithm The algorithm to use
     * @param digits The number of digits to use
     * @param period The period to use
     * 
     * @return An otpauth url
     */
    public static String getOtpauthURL(String name, String issuer, String secret, HmacShaAlgorithm algorithm, String digits, String period) {
        Objects.requireNonNull(name, Required.ACCOUNT_NAME.toString());
        Objects.requireNonNull(secret, Required.SECRET.toString());
        Objects.requireNonNull(issuer, Required.ISSUER.toString());
        Objects.requireNonNull(algorithm, Required.ALGORITHM.toString());
        Objects.requireNonNull(digits, Required.DIGITS.toString());
        Objects.requireNonNull(period, Required.PERIOD.toString());
        
        var buffer = new StringBuilder();
        buffer.append("otpauth://totp/")
            .append(name)
            .append("?secret=")
            .append(RegExUtils.replaceAll(base32.encodeAsString(secret.getBytes(StandardCharsets.UTF_8)), "=", ""))
            .append("&algorithm=")
            .append(algorithm.getAlgorithm())
            .append("&issuer=")
            .append(issuer)
            .append("&digits=")
            .append(digits)
            .append("&period=")
            .append(period);
        
        var url = "";
        url = URLEncoder.encode(buffer.toString(), StandardCharsets.UTF_8);

        return url;
    }
}