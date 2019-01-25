package io.mangoo.enums;

/**
 * 
 * @author svenkubiak
 *
 */
public enum HmacShaAlgorithm {
    HMAC_SHA_1("SHA1"),
    HMAC_SHA_256("SHA256"),
    HMAC_SHA_512("SHA512");

    private final String algorithm;

    HmacShaAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public static HmacShaAlgorithm from(String algorithm) {
        for (HmacShaAlgorithm alg : values()) {
            if (alg.algorithm.equals(algorithm)) {
                return alg;
            }
        }
        throw new IllegalArgumentException("No matching HmacShaAlgorithm constant for [" + algorithm + "]");
    }

    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String toString() {
        return getAlgorithm();
    }
}