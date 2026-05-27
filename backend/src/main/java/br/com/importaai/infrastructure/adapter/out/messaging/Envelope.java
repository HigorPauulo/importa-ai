package br.com.importaai.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record Envelope(
        @JsonProperty("event_id")       String  eventId,
        @JsonProperty("event_type")     String  eventType,
        @JsonProperty("schema_version") String  schemaVersion,
        @JsonProperty("occurred_at")    Instant occurredAt,
        @JsonProperty("data")           Object  data
) {}
