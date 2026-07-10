package com.resumelint.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

/**
 * Shared Jackson helper used by the JSON {@code AttributeConverter}s below to
 * persist {@code List<T>} fields (scores / suggestions / keywords) as a JSON
 * string in a single MySQL column, equivalent to the {@code jsonb(...)}
 * Drizzle columns in the original Node schema.
 */
final class JsonListConverterSupport {

    static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonListConverterSupport() {
    }

    static <T> String toJson(List<T> value) {
        if (value == null) {
            return "[]";
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize JSON column", e);
        }
    }

    static <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize JSON column", e);
        }
    }
}
