package io.mangoo.crypto;

import io.mangoo.constants.Required;
import io.mangoo.utils.Argument;
import org.bouncycastle.util.Arrays;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An Argon2id hash with its salt and parameters, encoded in PHC string format: $argon2id$v=19$m=...,t=...,p=...$salt$hash.
 * Salt and hash are copied on the way in and out, so a caller can not change a hash after handing it over.
 */
public record Argon2Hash(Argon2Settings settings, byte[] salt, byte[] hash) {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final String ALGORITHM = "argon2id";
    private static final String PREFIX = "$" + ALGORITHM + "$";
    private static final String MEMORY = "m";
    private static final String ITERATIONS = "t";
    private static final String PARALLELISM = "p";
    private static final int VERSION = 19;
    private static final int SEGMENTS = 6;
    private static final int MIN_HASH_LENGTH = 16;

    public Argon2Hash {
        Objects.requireNonNull(settings, Required.SETTINGS);
        Objects.requireNonNull(salt, Required.SALT);
        Objects.requireNonNull(hash, Required.HASH);

        salt = salt.clone();
        hash = hash.clone();
    }

    @Override
    public byte[] salt() {
        return salt.clone();
    }

    @Override
    public byte[] hash() {
        return hash.clone();
    }

    /** Anything not in PHC format is a legacy hash, i.e. a bare Base64 encoding of the raw hash bytes. */
    public static boolean isPhcFormat(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    /** Throws IllegalArgumentException if the value is not a well-formed Argon2id PHC string. */
    public static Argon2Hash parse(String value) {
        String phc = Argument.requireNonBlank(value, Required.HASH);
        Argument.check(isPhcFormat(phc), "Not an " + ALGORITHM + " hash in PHC format");

        String[] segments = phc.split("\\$", -1);
        Argument.check(segments.length == SEGMENTS, "Malformed " + ALGORITHM + " PHC string");
        Argument.check(("v=" + VERSION).equals(segments[2]), "Unsupported Argon2 version in " + segments[2]);

        var parameters = parameters(segments[3]);
        var settings = new Argon2Settings(
                parameters.get(MEMORY),
                parameters.get(ITERATIONS),
                parameters.get(PARALLELISM));

        // Lower bounds of the Argon2 specification, not mangoo I/O's minimums; a stored hash below them would only make the generator throw.
        Argument.check(settings.parallelism() >= 1, "Invalid Argon2 parallelism " + settings.parallelism());
        Argument.check(settings.iterations() >= 1, "Invalid Argon2 iterations " + settings.iterations());
        Argument.check(settings.memoryKb() >= 8 * settings.parallelism(), "Invalid Argon2 memory cost " + settings.memoryKb());

        byte[] salt = BASE64_DECODER.decode(segments[4]);
        byte[] hash = BASE64_DECODER.decode(segments[5]);
        Argument.check(salt.length > 0, "Malformed " + ALGORITHM + " PHC string, salt is empty");
        Argument.check(hash.length >= MIN_HASH_LENGTH, "Malformed " + ALGORITHM + " PHC string, hash is too short");

        return new Argon2Hash(settings, salt, hash);
    }

    private static Map<String, Integer> parameters(String segment) {
        Map<String, Integer> parameters = new HashMap<>();
        for (String parameter : segment.split(",")) {
            String[] pair = parameter.split("=", -1);
            Argument.check(pair.length == 2, "Malformed Argon2 parameter " + parameter);
            try {
                parameters.put(pair[0], Integer.valueOf(pair[1]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Malformed Argon2 parameter " + parameter, e);
            }
        }

        Argument.check(parameters.keySet().equals(Set.of(MEMORY, ITERATIONS, PARALLELISM)),
                "Malformed Argon2 parameters, expected m, t and p in " + segment);

        return parameters;
    }

    public String encode() {
        return PREFIX + "v=" + VERSION + "$"
                + MEMORY + "=" + settings.memoryKb() + ","
                + ITERATIONS + "=" + settings.iterations() + ","
                + PARALLELISM + "=" + settings.parallelism() + "$"
                + BASE64_ENCODER.encodeToString(salt) + "$"
                + BASE64_ENCODER.encodeToString(hash);
    }

    // Compares the arrays by content in constant time; the generated record equals would compare them by identity.
    @Override
    public boolean equals(Object object) {
        return object instanceof Argon2Hash other
                && settings.equals(other.settings)
                && Arrays.constantTimeAreEqual(salt, other.salt)
                && Arrays.constantTimeAreEqual(hash, other.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settings, Arrays.hashCode(salt), Arrays.hashCode(hash));
    }

    // Omits salt and hash bytes as this may end up in logs; use encode() for the actual value.
    @Override
    public String toString() {
        return "Argon2Hash[settings=" + settings
                + ", salt=" + salt.length + " bytes"
                + ", hash=" + hash.length + " bytes]";
    }
}
