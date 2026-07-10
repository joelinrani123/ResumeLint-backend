package com.resumelint.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.resumelint.dto.KeywordDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class KeywordsConverter implements AttributeConverter<List<KeywordDto>, String> {

    @Override
    public String convertToDatabaseColumn(List<KeywordDto> attribute) {
        return JsonListConverterSupport.toJson(attribute);
    }

    @Override
    public List<KeywordDto> convertToEntityAttribute(String dbData) {
        return JsonListConverterSupport.fromJson(dbData, new TypeReference<>() {
        });
    }
}
