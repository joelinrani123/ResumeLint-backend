package com.resumelint.dto;

/** type: improvement|warning|success ; priority: high|medium|low ; section may be null. */
public record SuggestionDto(String type, String priority, String text, String section) {
}
