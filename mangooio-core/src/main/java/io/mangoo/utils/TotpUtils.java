package io.mangoo.utils;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;
import io.mangoo.constants.Hmac;
import io.mangoo.constants.Required;
import net.glxn.qrgen.QRCode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public class TotpUtils {
    private static final Random RANDOM = new SecureRandom();
    private static final HMACAlgorithm ALGORITHM = HMACAlgorithm.SHA512;
    private static final int DIGITS = 6;
    private static final int MAX_CHARACTERS = 32;
    private static final int ITERATIONS = 26;
    private static final int PERIOD = 30;
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
            var value = RANDOM.nextInt(MAX_CHARACTERS);
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
        Objects.requireNonNull(secret, Required.SECRET);

        TOTPGenerator totp = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(DIGITS);
                    builder.withAlgorithm(ALGORITHM);
                })
                .withPeriod(Duration.ofSeconds(PERIOD))
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
        Objects.requireNonNull(secret, Required.SECRET);
        Objects.requireNonNull(totp, Required.TOTP);

        TOTPGenerator expected = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(DIGITS);
                    builder.withAlgorithm(ALGORITHM);
                })
                .withPeriod(Duration.ofSeconds(PERIOD))
                .build();

        return expected.verify(totp);
    }

    /**
     * Generates a QR code image as a base64 PNG
     *
     * @param name The name of the account
     * @param issuer The name of the issuer
     * @param secret The secret to use
     *
     * @return The QR code as a base64 PNG image
     */
    public static String getQRCode(String name, String issuer, String secret) {
        Objects.requireNonNull(name, Required.NAME);
        Objects.requireNonNull(issuer, Required.ISSUER);
        Objects.requireNonNull(secret, Required.SECRET);

        String text = getOtpauthURL(name, issuer, secret);
        ByteArrayOutputStream qrCodeOutputStream = QRCode.from(text)
                .withSize(250, 250)
                .stream();

        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        return new String(CommonUtils.encodeToBase64(qrCodeBytes), StandardCharsets.UTF_8);
    }

    /**
     * Generates an otpauth code to share a secret with a user
     * 
     * @param name The name of the account
     * @param issuer The name of the issuer
     * @param secret The secret to use
     *
     * @return An otpauth url
     */
    public static String getOtpauthURL(String name, String issuer, String secret) {
        Objects.requireNonNull(name, Required.ACCOUNT_NAME);
        Objects.requireNonNull(secret, Required.SECRET);
        Objects.requireNonNull(issuer, Required.ISSUER);

        var buffer = new StringBuilder();
        buffer.append("otpauth://totp/")
            .append(name)
            .append("?secret=")
            .append(secret)
            .append("&algorithm=")
            .append(Hmac.SHA512)
            .append("&issuer=")
            .append(issuer)
            .append("&digits=")
            .append(DIGITS)
            .append("&period=")
            .append(PERIOD);

        return buffer.toString();
    }
}