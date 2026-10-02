package io.mangoo.constants;

import java.util.Set;

public final class ClaimKey {
    public static final String DATA = "data";
    public static final String FORM = "form";
    public static final String TWO_FACTOR = "twofactor";
    public static final String REMEMBER_ME = "rememberMe";
    public static final Set<String> RESERVED = Set.of("iss", "aud", "sub", "iat", "nbf", "exp", "jti");

    private ClaimKey() {
    }
}
