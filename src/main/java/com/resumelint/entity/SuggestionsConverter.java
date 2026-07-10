package com.resumelint.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.resumelint.dto.SuggestionDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class SuggestionsConverter implements AttributeConverter<List<SuggestionDto>, String> {

    @Override
    public String convertToDatabaseColumn(List<SuggestionDto> attribute) {
        return JsonListConverterSupport.toJson(attribute);
    }

    @Override
    public List<SuggestionDto> convertToEntityAttribute(String dbData) {
        return JsonListConverterSupport.fromJson(dbData, new TypeReference<>() {
        });
    }
}
