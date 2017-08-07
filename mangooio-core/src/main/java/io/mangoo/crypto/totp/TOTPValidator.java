package io.mangoo.crypto.totp;

import com.google.common.base.Preconditions;

import io.mangoo.enums.HmacShaAlgorithm;

/**
 * A Time-based One-time Password (TOTP) validator.
 * 
 * <p>
 * As per <a href="https://tools.ietf.org/html/rfc6238#section-5.2">RFC 6238 (section 5.2)</a>:
 * </p>
 * <p>
 * "An OTP generated within the same time step will be the same. When an OTP is
 * received at a validation system, it doesn't know a client's exact timestamp
 * when an OTP was generated. The validation system may typically use the
 * timestamp when an OTP is received for OTP comparison. Due to network latency,
 * the gap (as measured by T, that is, the number of time steps since T0)
 * between the time that the OTP was generated and the time that the OTP arrives
 * at the receiving system may be large. The receiving time at the validation
 * system and the actual OTP generation may not fall within the same time-step
 * window that produced the same OTP. When an OTP is generated at the end of a
 * time-step window, the receiving time most likely falls into the next
 * time-step window. A validation system SHOULD typically set a policy for an
 * acceptable OTP transmission delay window for validation. The validation
 * system should compare OTPs not only with the receiving timestamp but also the
 * past timestamps that are within the transmission delay. A larger acceptable
 * delay window would expose a larger window for attacks. We RECOMMEND that at
 * most one time step is allowed as the network delay."
 * </p>
 * 
 * <p>
 * Example:
 * </p>
 * 
 * <pre>
 * // We will let the TOTP generation time == TOTP validation time so validation will succeed.
 * final long time = System.currentTimeMillis(); 
 * byte[] key = &quot;...&quot;;
 * TOTP totp = TOTP.key(key).build(time);
 * boolean valid = TOTPValidator.window(0).isValid(key, totp.timeStep(), totp.digits(), totp.hmacShaAlgorithm(), totp.value(), time);
 * // Should print &quot;TOTP = ..., valid = true&quot;
 * System.out.printf(&quot;TOTP = %s, valid = %s%n&quot;, totp.value(), valid);
 * </pre>
 * 
 * @author Johnny Mongiat
 *
 * @see <a href="https://tools.ietf.org/html/rfc6238#section-5.2">RFC 6238 (section 5.2)</a>
 */
public final class TOTPValidator {

    /** The default window verification size. */
    public static final int DEFAULT_WINDOW = 1;

    private final int window;

    /**
     * Creates a new instance of {@code TOTPValidator} initialized with the
     * specified {@code window} verification size.
     * 
     * @param window
     *            the window verification size
     * 
     * @throws IllegalArgumentException
     *             if {@code window} is < 0.
     */
    private TOTPValidator(int window) {
        Preconditions.checkArgument(window >= 0);
        this.window = window;
    }

    /**
     * Returns a new {@link TOTPValidator} instance initialized with the
     * {@link #DEFAULT_WINDOW} verification size.
     * 
     * @return a new {@link TOTPValidator} instance.
     */
    public static TOTPValidator defaultWindow() {
        return window(DEFAULT_WINDOW);
    }

    /**
     * Returns a new {@link TOTPValidator} instance initialized with the
     * specified {@code window} verification size.
     * 
     * @param window
     *            the window verification size
     * 
     * @return a new {@link TOTPValidator} instance.
     * 
     * @throws IllegalArgumentException
     *             if {@code window} is {@literal <} 0.
     */
    public static TOTPValidator window(int window) {
        return new TOTPValidator(window);
    }

    /**
     * Returns {@code true} if the specified TOTP {@code value} matches the
     * value of the TOTP generated at validation, otherwise {@code false}. The
     * current system time (current time in milliseconds since the UNIX epoch)
     * is used as the validation reference time.
     * 
     * @param key
     *            the encoded shared secret key
     * @param timeStep
     *            the time step size in milliseconds
     * @param digits
     *            the number of digits a TOTP should contain
     * @param hmacShaAlgorithm
     *            {@link HmacShaAlgorithm}
     * @param value
     *            the TOTP value to validate
     * 
     * @return {@code true} if the specified TOTP {@code code} value matches the
     *         code value of the TOTP generated at validation, otherwise
     *         {@code false}.
     */
    public boolean isValid(byte[] key, long timeStep, int digits, HmacShaAlgorithm hmacShaAlgorithm, String value) {
        return isValid(key, timeStep, digits, hmacShaAlgorithm, value, System.currentTimeMillis());
    }

    /**
     * Returns {@code true} if the specified TOTP {@code value} matches the
     * value of the TOTP generated at validation, otherwise {@code false}.
     * 
     * @param key
     *            the encoded shared secret key
     * @param timeStep
     *            the time step size in milliseconds
     * @param digits
     *            the number of digits a TOTP should contain
     * @param hmacShaAlgorithm
     *            {@link HmacShaAlgorithm}
     * @param value
     *            the TOTP value to validate
     * @param validationTime
     *            the validation reference time in milliseconds
     * 
     * @return {@code true} if the specified TOTP {@code code} value matches the
     *         code value of the TOTP generated at validation, otherwise
     *         {@code false}.
     */
    public boolean isValid(byte[] key, long timeStep, int digits, HmacShaAlgorithm hmacShaAlgorithm, String value, long validationTime) {
        boolean result = false;
        TOTPBuilder builder = TOTP.key(key).timeStep(timeStep).digits(digits).hmacSha(hmacShaAlgorithm);
        for (int i = -window; i <= window; i++) {
            final long time = validationTime + (i * timeStep);
            final TOTP vtotp = builder.build(time);
            if (vtotp.value().equals(value)) {
                result = true;
                break;
            }
        }
        return result;
    }
}