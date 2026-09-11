package com.fleet.tracking.config;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// The app forces its JVM clock to UTC (see FleetTrackingApplication.main), so
// every LocalDateTime.now() call - createdAt, recordedAt, startTime, etc. -
// is genuinely a UTC wall-clock value, even though LocalDateTime itself
// carries no timezone marker. Jackson's default serializer just prints that
// value as a bare ISO string with no zone info, e.g. "2026-09-10T10:38:31" -
// browsers then misinterpret that as *local* time when parsing it with
// `new Date(...)`, which is what made timestamps appear hours off depending
// on the viewer's timezone. Appending a literal 'Z' here tells the browser
// to correctly treat it as UTC and convert to the viewer's local time
// automatically, the way ISO 8601 timestamps are supposed to work.
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer utcLocalDateTimeCustomizer() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return builder -> builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
    }
}