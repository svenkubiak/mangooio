package io.mangoo.persistence.codecs;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class LocalDateCodec implements Codec<LocalDate> {
    private static final ZoneId UTC = ZoneOffset.UTC;
    private final ZoneId zoneId;

    public LocalDateCodec(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public void encode(BsonWriter writer, LocalDate value, EncoderContext encoderContext) {
        if (value == null) {
            writer.writeNull();
            return;
        }

        Instant instant;
        if (UTC.equals(zoneId)) {
            instant = value.atStartOfDay(ZoneOffset.UTC).toInstant();
        } else {
            instant = value.atStartOfDay(zoneId).toInstant();
        }

        writer.writeDateTime(instant.toEpochMilli());
    }

    @Override
    public LocalDate decode(BsonReader reader, DecoderContext decoderContext) {
        if (reader.getCurrentBsonType() == BsonType.NULL) {
            reader.readNull();
            return null;
        }

        long millis = reader.readDateTime();
        Instant instant = Instant.ofEpochMilli(millis);

        if (UTC.equals(zoneId)) {
            return instant.atZone(ZoneOffset.UTC).toLocalDate();
        } else {
            return instant.atZone(zoneId).toLocalDate();
        }
    }

    @Override
    public Class<LocalDate> getEncoderClass() {
        return LocalDate.class;
    }
}
