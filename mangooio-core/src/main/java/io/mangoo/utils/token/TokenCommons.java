package io.mangoo.utils.token;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;

/**
 * 
 * @author svenkubiak
 *
 */
public class TokenCommons {
    protected final Charset CHARSET = StandardCharsets.UTF_8;
    protected final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;
    protected final String ALGORITHM = "AES";
}