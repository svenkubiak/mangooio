package io.mangoo.utils;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;
import com.google.common.base.Preconditions;
import io.mangoo.constants.NotNull;
import net.glxn.qrgen.QRCode;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.RegExUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public class TotpUtils {
    private static final Base32 base32 = new Base32();
    private static final Random random = new SecureRandom();
    private static final HMACAlgorithm ALGORITHM = HMACAlgorithm.SHA512;
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
        Objects.requireNonNull(secret, NotNull.SECRET);

        return getTotp(secret, ALGORITHM, DIGITS, PERIOD);
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
    public static String getTotp(String secret, HMACAlgorithm algorithm, int digits, int period) {
        Objects.requireNonNull(secret, NotNull.SECRET);
        Objects.requireNonNull(algorithm, NotNull.ALGORITHM);
        Preconditions.checkArgument(digits > 0, "digits must be greater than 0");
        Preconditions.checkArgument(period > 0, "period must be greater than 0");

        TOTPGenerator totp = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(digits);
                    builder.withAlgorithm(algorithm);
                })
                .withPeriod(Duration.ofSeconds(period))
                .build();

        return totp.now();
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
    public static boolean verifyTotp(String secret, String totp) {
        Objects.requireNonNull(secret, NotNull.SECRET);
        Objects.requireNonNull(totp, NotNull.TOTP);

        return verifyTotp(secret, totp, ALGORITHM, DIGITS, PERIOD);
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
    public static boolean verifyTotp(String secret, String totp, HMACAlgorithm algorithm, int digits, int period) {
        Objects.requireNonNull(secret, NotNull.SECRET);
        Objects.requireNonNull(totp, NotNull.TOTP);
        Objects.requireNonNull(algorithm, NotNull.ALGORITHM);
        Preconditions.checkArgument(digits > 0, "digits must be greater than 0");
        Preconditions.checkArgument(period > 0, "period must be greater than 0");

        TOTPGenerator expected = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(digits);
                    builder.withAlgorithm(algorithm);
                })
                .withPeriod(Duration.ofSeconds(period))
                .build();

        return expected.verify(totp);
    }

    /**
     * Generates a QR code image as a base64 PNG
     *
     * @param name The name of the account
     * @param issuer The name of the issuer
     * @param secret The secret to use
     * @param algorithm The algorithm to use
     * @param digits The number of digits to use
     * @param period The period to use
     *
     * @return The QR code as a base64 PNG image
     */
    public static String getQRCode(String name, String issuer, String secret, String algorithm, String digits, String period) {
        Objects.requireNonNull(name, NotNull.NAME);
        Objects.requireNonNull(issuer, NotNull.ISSUER);
        Objects.requireNonNull(secret, NotNull.SECRET);
        Objects.requireNonNull(algorithm, NotNull.ALGORITHM);
        Objects.requireNonNull(digits, NotNull.DIGITS);
        Objects.requireNonNull(period, NotNull.PERIOD);

        String text = getOtpauthURL(name, issuer, secret, algorithm, digits, period);
        ByteArrayOutputStream qrCodeOutputStream = QRCode.from(text)
                .withSize(250, 250)
                .stream();

        // Convert byte array output stream to a byte array
        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        return new String(CodecUtils.encodeToBase64(qrCodeBytes), StandardCharsets.UTF_8);
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
    public static String getOtpauthURL(String name, String issuer, String secret, String algorithm, String digits, String period) {
        Objects.requireNonNull(name, NotNull.ACCOUNT_NAME);
        Objects.requireNonNull(secret, NotNull.SECRET);
        Objects.requireNonNull(issuer, NotNull.ISSUER);
        Objects.requireNonNull(algorithm, NotNull.ALGORITHM);
        Objects.requireNonNull(digits, NotNull.DIGITS);
        Objects.requireNonNull(period, NotNull.PERIOD);
        
        var buffer = new StringBuilder();
        buffer.append("otpauth://totp/")
            .append(name)
            .append("?secret=")
            .append(RegExUtils.replaceAll(base32.encodeAsString(secret.getBytes(StandardCharsets.UTF_8)), "=", ""))
            .append("&algorithm=")
            .append(algorithm)
            .append("&issuer=")
            .append(issuer)
            .append("&digits=")
            .append(digits)
            .append("&period=")
            .append(period);

        return buffer.toString();
    }
}