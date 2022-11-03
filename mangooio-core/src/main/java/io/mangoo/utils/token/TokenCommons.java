package io.mangoo.utils.token;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;

public class TokenCommons {
    protected static final String ALGORITHM = "AES";
    protected static final Charset CHARSET = StandardCharsets.UTF_8;
    protected static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;
}