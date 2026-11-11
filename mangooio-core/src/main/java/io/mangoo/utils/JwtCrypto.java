package io.mangoo.utils;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWECryptoParts;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.jca.JCAContext;
import com.nimbusds.jose.jca.JWEJCAContext;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Set;

/**
 * AES-CBC and HMAC-SHA-512 for the cookie JWE, with one cipher and one MAC per thread.
 * Nimbus creates a new cipher on every call, which dominates cookie parsing.
 * The token format stays dir / A256CBC-HS512 and HS512, so existing cookies still verify.
 */
@SuppressFBWarnings(value = "CIPHER_INTEGRITY", justification = "The HMAC is checked before the AES plaintext is returned")
final class JwtCrypto {
    private static final String AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
    private static final String HMAC_SHA_512 = "HmacSHA512";
    private static final int SECRET_BYTES = 64;
    private static final int HALF_BYTES = 32;
    private static final int IV_BYTES = 16;
    private static final int TAG_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ThreadLocal<Cipher> AES = ThreadLocal.withInitial(JwtCrypto::cipher);
    private static final ThreadLocal<Mac> HMAC = ThreadLocal.withInitial(JwtCrypto::mac);

    private JwtCrypto() {
    }

    static void sign(SignedJWT signedJwt, byte[] key) throws JOSEException {
        signedJwt.sign(new HmacSigner(key));
    }

    static boolean verify(SignedJWT signedJwt, byte[] key) throws JOSEException {
        return signedJwt.verify(new HmacVerifier(key));
    }

    static void encrypt(JWEObject jweObject, byte[] secret) throws JOSEException {
        jweObject.encrypt(new DirectEncrypter(secret));
    }

    static void decrypt(JWEObject jweObject, byte[] secret) throws JOSEException {
        jweObject.decrypt(new DirectDecrypter(secret));
    }

    private static Cipher cipher() {
        try {
            return Cipher.getInstance(AES_CBC_PKCS5);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(AES_CBC_PKCS5 + " is not available", e);
        }
    }

    private static Mac mac() {
        try {
            return Mac.getInstance(HMAC_SHA_512);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(HMAC_SHA_512 + " is not available", e);
        }
    }

    private static byte[] hmacSha512(byte[] key, int offset, int length, byte[] message) throws JOSEException {
        try {
            Mac mac = HMAC.get();
            mac.init(new SecretKeySpec(key, offset, length, HMAC_SHA_512));
            return mac.doFinal(message);
        } catch (GeneralSecurityException e) {
            throw new JOSEException(e.getMessage(), e);
        }
    }

    private static byte[] aes(int mode, byte[] secret, byte[] iv, byte[] input) throws JOSEException {
        try {
            Cipher cipher = AES.get();
            cipher.init(mode, new SecretKeySpec(secret, HALF_BYTES, HALF_BYTES, "AES"), new IvParameterSpec(iv));
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw new JOSEException(e.getMessage(), e);
        }
    }

    private static byte[] authenticationTag(byte[] secret, byte[] aad, byte[] iv, byte[] cipherText) throws JOSEException {
        byte[] aadBits = ByteBuffer.allocate(Long.BYTES).putLong(Math.multiplyExact((long) aad.length, 8L)).array();
        byte[] input = ByteBuffer.allocate(aad.length + iv.length + cipherText.length + aadBits.length)
                .put(aad)
                .put(iv)
                .put(cipherText)
                .put(aadBits)
                .array();
        return Arrays.copyOf(hmacSha512(secret, 0, HALF_BYTES, input), TAG_BYTES);
    }

    private static void requireSecret(byte[] secret) throws KeyLengthException {
        if (secret == null || secret.length != SECRET_BYTES) {
            throw new KeyLengthException("A256CBC-HS512 requires a 512 bit key");
        }
    }

    private static void rejectCritical(Set<String> critical) throws JOSEException {
        if (critical != null && !critical.isEmpty()) {
            throw new JOSEException("Unsupported critical header parameter");
        }
    }

    private static final class HmacSigner implements JWSSigner {
        private static final Set<JWSAlgorithm> ALGORITHMS = Set.of(JWSAlgorithm.HS512);
        private final byte[] key;

        private HmacSigner(byte[] key) throws KeyLengthException {
            if (key == null || key.length < SECRET_BYTES) {
                throw new KeyLengthException("The secret length for HS512 must be at least 512 bits");
            }
            this.key = key;
        }

        @Override
        public Base64URL sign(JWSHeader header, byte[] signingInput) throws JOSEException {
            if (!JWSAlgorithm.HS512.equals(header.getAlgorithm())) {
                throw new JOSEException("Unexpected JWS algorithm: " + header.getAlgorithm());
            }
            rejectCritical(header.getCriticalParams());
            return Base64URL.encode(hmacSha512(key, 0, key.length, signingInput));
        }

        @Override
        public Set<JWSAlgorithm> supportedJWSAlgorithms() {
            return ALGORITHMS;
        }

        @Override
        public JCAContext getJCAContext() {
            return new JCAContext();
        }
    }

    private static final class HmacVerifier implements JWSVerifier {
        private static final Set<JWSAlgorithm> ALGORITHMS = Set.of(JWSAlgorithm.HS512);
        private final byte[] key;

        private HmacVerifier(byte[] key) throws KeyLengthException {
            if (key == null || key.length < SECRET_BYTES) {
                throw new KeyLengthException("The secret length for HS512 must be at least 512 bits");
            }
            this.key = key;
        }

        @Override
        public boolean verify(JWSHeader header, byte[] signingInput, Base64URL signature) throws JOSEException {
            if (!JWSAlgorithm.HS512.equals(header.getAlgorithm())) {
                return false;
            }
            if (header.getCriticalParams() != null && !header.getCriticalParams().isEmpty()) {
                return false;
            }
            byte[] expected = hmacSha512(key, 0, key.length, signingInput);
            return MessageDigest.isEqual(expected, signature.decode());
        }

        @Override
        public Set<JWSAlgorithm> supportedJWSAlgorithms() {
            return ALGORITHMS;
        }

        @Override
        public JCAContext getJCAContext() {
            return new JCAContext();
        }
    }

    private static final class DirectEncrypter implements JWEEncrypter {
        private static final Set<JWEAlgorithm> ALGORITHMS = Set.of(JWEAlgorithm.DIR);
        private static final Set<EncryptionMethod> METHODS = Set.of(EncryptionMethod.A256CBC_HS512);
        private final byte[] secret;

        private DirectEncrypter(byte[] secret) throws KeyLengthException {
            requireSecret(secret);
            this.secret = secret;
        }

        @Override
        public JWECryptoParts encrypt(JWEHeader header, byte[] clearText, byte[] aad) throws JOSEException {
            if (!JWEAlgorithm.DIR.equals(header.getAlgorithm())) {
                throw new JOSEException("Unexpected JWE algorithm: " + header.getAlgorithm());
            }
            if (!EncryptionMethod.A256CBC_HS512.equals(header.getEncryptionMethod())) {
                throw new JOSEException("Unexpected JWE encryption method: " + header.getEncryptionMethod());
            }
            rejectCritical(header.getCriticalParams());

            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);
            byte[] cipherText = aes(Cipher.ENCRYPT_MODE, secret, iv, clearText);
            byte[] tag = authenticationTag(secret, aad, iv, cipherText);
            return new JWECryptoParts(header, null, Base64URL.encode(iv), Base64URL.encode(cipherText), Base64URL.encode(tag));
        }

        @Override
        public Set<JWEAlgorithm> supportedJWEAlgorithms() {
            return ALGORITHMS;
        }

        @Override
        public Set<EncryptionMethod> supportedEncryptionMethods() {
            return METHODS;
        }

        @Override
        public JWEJCAContext getJCAContext() {
            return new JWEJCAContext();
        }
    }

    private static final class DirectDecrypter implements JWEDecrypter {
        private static final Set<JWEAlgorithm> ALGORITHMS = Set.of(JWEAlgorithm.DIR);
        private static final Set<EncryptionMethod> METHODS = Set.of(EncryptionMethod.A256CBC_HS512);
        private final byte[] secret;

        private DirectDecrypter(byte[] secret) throws KeyLengthException {
            requireSecret(secret);
            this.secret = secret;
        }

        @Override
        public byte[] decrypt(JWEHeader header, Base64URL encryptedKey, Base64URL iv, Base64URL cipherText, Base64URL authTag, byte[] aad) throws JOSEException {
            if (!JWEAlgorithm.DIR.equals(header.getAlgorithm())) {
                throw new JOSEException("Unexpected JWE algorithm: " + header.getAlgorithm());
            }
            if (!EncryptionMethod.A256CBC_HS512.equals(header.getEncryptionMethod())) {
                throw new JOSEException("Unexpected JWE encryption method: " + header.getEncryptionMethod());
            }
            if (encryptedKey != null) {
                throw new JOSEException("Unexpected present JWE encrypted key");
            }
            if (iv == null) {
                throw new JOSEException("Missing JWE initialization vector");
            }
            if (authTag == null) {
                throw new JOSEException("Missing JWE authentication tag");
            }
            rejectCritical(header.getCriticalParams());

            byte[] ivBytes = iv.decode();
            byte[] cipherBytes = cipherText.decode();
            byte[] tagBytes = authTag.decode();
            if (ivBytes.length != IV_BYTES) {
                throw new JOSEException("Unexpected JWE initialization vector length");
            }
            byte[] expected = authenticationTag(secret, aad, ivBytes, cipherBytes);
            if (!MessageDigest.isEqual(expected, tagBytes)) {
                throw new JOSEException("MAC check failed");
            }
            return aes(Cipher.DECRYPT_MODE, secret, ivBytes, cipherBytes);
        }

        @Override
        public Set<JWEAlgorithm> supportedJWEAlgorithms() {
            return ALGORITHMS;
        }

        @Override
        public Set<EncryptionMethod> supportedEncryptionMethods() {
            return METHODS;
        }

        @Override
        public JWEJCAContext getJCAContext() {
            return new JWEJCAContext();
        }
    }
}
