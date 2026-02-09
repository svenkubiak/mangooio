package io.mangoo.persistence.codecs;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LocalDateTimeCodec implements Codec<LocalDateTime> {
    private final ZoneId zoneId;

    public LocalDateTimeCodec(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public void encode(BsonWriter writer, LocalDateTime value, EncoderContext encoderContext) {
        if (value == null) {
            writer.writeNull();
            return;
        }
        Instant instant = value.atZone(zoneId).toInstant();
        writer.writeDateTime(instant.toEpochMilli());
    }

    @Override
    public LocalDateTime decode(BsonReader reader, DecoderContext decoderContext) {
        if (reader.getCurrentBsonType() == BsonType.NULL) {
            reader.readNull();
            return null;
        }
        long millis = reader.readDateTime();
        Instant instant = Instant.ofEpochMilli(millis);
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    @Override
    public Class<LocalDateTime> getEncoderClass() {
        return LocalDateTime.class;
    }
}
