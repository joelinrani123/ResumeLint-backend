package com.resumelint.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.resumelint.dto.ScoreBreakdownDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ScoresConverter implements AttributeConverter<List<ScoreBreakdownDto>, String> {

    @Override
    public String convertToDatabaseColumn(List<ScoreBreakdownDto> attribute) {
        return JsonListConverterSupport.toJson(attribute);
    }

    @Override
    public List<ScoreBreakdownDto> convertToEntityAttribute(String dbData) {
        return JsonListConverterSupport.fromJson(dbData, new TypeReference<>() {
        });
    }
}
