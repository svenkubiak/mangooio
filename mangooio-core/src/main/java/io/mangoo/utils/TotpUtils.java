package io.mangoo.utils;

import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.TOTPGenerator;
import io.mangoo.constants.Required;
import net.glxn.qrgen.QRCode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
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
     * Returns a random 64 character Base32 secret.
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

    public static String getTotp(String secret) {
        Argument.requireNonBlank(secret, Required.SECRET);

        TOTPGenerator totp = new TOTPGenerator.Builder(secret)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(DIGITS);
                    builder.withAlgorithm(ALGORITHM);
                })
                .withPeriod(Duration.ofSeconds(PERIOD))
                .build();

        return totp.now();
    }

    public static boolean verifyTotp(String secret, String totp) {
        Argument.requireNonBlank(secret, Required.SECRET);
        Argument.requireNonBlank(totp, Required.TOTP);

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
     * Returns the QR code as a base64 encoded PNG image.
     */
    public static String getQRCode(String name, String issuer, String secret) {
        Argument.requireNonBlank(name, Required.NAME);
        Argument.requireNonBlank(issuer, Required.ISSUER);
        Argument.requireNonBlank(secret, Required.SECRET);

        String text = getOtpAuthURL(name, issuer, secret);
        ByteArrayOutputStream qrCodeOutputStream = QRCode.from(text)
                .withSize(250, 250)
                .stream();

        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();

        return new String(CommonUtils.encodeToBase64(qrCodeBytes), StandardCharsets.UTF_8);
    }

    public static String getOtpAuthURL(String name, String issuer, String secret) {
        Argument.requireNonBlank(name, Required.ACCOUNT_NAME);
        Argument.requireNonBlank(secret, Required.SECRET);
        Argument.requireNonBlank(issuer, Required.ISSUER);

        var buffer = new StringBuilder();
        buffer.append("otpauth://totp/")
            .append(name)
            .append("?secret=")
            .append(secret)
            .append("&algorithm=")
            .append("SHA512")
            .append("&issuer=")
            .append(issuer)
            .append("&digits=")
            .append(DIGITS)
            .append("&period=")
            .append(PERIOD);

        return buffer.toString();
    }
}