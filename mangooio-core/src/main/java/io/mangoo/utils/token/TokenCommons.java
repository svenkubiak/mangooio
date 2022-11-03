package io.mangoo.utils.token;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;

public class TokenCommons {
    protected final static String ALGORITHM = "AES";
    protected final static Charset CHARSET = StandardCharsets.UTF_8;
    protected final static ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;
}