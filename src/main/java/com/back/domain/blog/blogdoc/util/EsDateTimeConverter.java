package com.back.domain.blog.blogdoc.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

@Component
public class EsDateTimeConverter {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final DateTimeFormatter ES_LOCAL_DATE_TIME_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .optionalEnd()
                    .toFormatter();
    
    public static LocalDateTime parseToKst(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value, ES_LOCAL_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignore) {
            OffsetDateTime odt = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return odt.atZoneSameInstant(KST).toLocalDateTime();
        }
    }
}
