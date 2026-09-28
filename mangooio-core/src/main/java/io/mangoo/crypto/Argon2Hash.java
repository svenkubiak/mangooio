package io.mangoo.crypto;

import io.mangoo.constants.Required;
import io.mangoo.utils.Argument;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An Argon2id hash together with the salt and the parameters it was computed with,
 * encoded in the PHC string format
 * <p>
 * {@snippet :
 * $argon2id$v=19$m=32768,t=3,p=1$<salt-b64>$<hash-b64>
 * }
 * <p>
 * Salt and hash are Base64 encoded without padding as the format requires. Because the
 * parameters travel with the hash, the configured parameters can be changed without
 * locking out users whose password was hashed with the previous ones
 *
 * @param settings The Argon2id parameters the hash was computed with
 * @param salt The raw salt bytes
 * @param hash The raw hash bytes
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
    }

    /**
     * Checks whether a stored value is an Argon2id hash in PHC format. Everything else
     * is a legacy hash, a bare Base64 encoding of the raw hash bytes
     *
     * @param value The stored value, may be null
     * @return True if the value is in PHC format, false otherwise
     */
    public static boolean isPhcFormat(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    /**
     * Parses an Argon2id hash in PHC format
     *
     * @param value The stored value
     * @return The parsed hash
     * @throws IllegalArgumentException If the value is not a well-formed Argon2id PHC string
     */
    public static Argon2Hash parse(String value) {
        Argument.requireNonBlank(value, Required.HASH);
        Argument.check(isPhcFormat(value), "Not an " + ALGORITHM + " hash in PHC format");

        String[] segments = value.split("\\$", -1);
        Argument.check(segments.length == SEGMENTS, "Malformed " + ALGORITHM + " PHC string");
        Argument.check(("v=" + VERSION).equals(segments[2]), "Unsupported Argon2 version in " + segments[2]);

        var parameters = parameters(segments[3]);
        var settings = new Argon2Settings(
                parameters.get(MEMORY),
                parameters.get(ITERATIONS),
                parameters.get(PARALLELISM));

        //the lower bounds of the Argon2 specification, not the ones mangoo I/O hashes with.
        //A stored hash below them was never produced by Argon2 and would only make the
        //generator throw
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

    /**
     * @return This hash encoded in the PHC string format
     */
    public String encode() {
        return PREFIX + "v=" + VERSION + "$"
                + MEMORY + "=" + settings.memoryKb() + ","
                + ITERATIONS + "=" + settings.iterations() + ","
                + PARALLELISM + "=" + settings.parallelism() + "$"
                + BASE64_ENCODER.encodeToString(salt) + "$"
                + BASE64_ENCODER.encodeToString(hash);
    }
}
