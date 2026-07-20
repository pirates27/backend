package com.landlens.api.mapper;

import com.landlens.api.dto.ApiKeyResponseDto;
import com.landlens.api.dto.ApiLogResponseDto;
import com.landlens.api.model.ApiKey;
import com.landlens.api.model.ApiLog;

public class ApiMapper {

    private ApiMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ApiKeyResponseDto toResponseDto(ApiKey key) {
        if (key == null) {
            return null;
        }
        ApiKeyResponseDto dto = new ApiKeyResponseDto();
        dto.setId(key.getId());
        dto.setName(key.getName());
        dto.setPrefix(key.getPrefix());
        dto.setStatus(key.getStatus());
        dto.setExpiryDate(key.getExpiryDate());
        dto.setIsActive(key.getIsActive());
        dto.setCreatedAt(key.getCreatedAt());
        return dto;
    }

    public static ApiLogResponseDto toResponseDto(ApiLog log) {
        if (log == null) {
            return null;
        }
        ApiLogResponseDto dto = new ApiLogResponseDto();
        dto.setId(log.getId());
        if (log.getApiKey() != null) {
            dto.setApiKeyId(log.getApiKey().getId());
        }
        dto.setEndpoint(log.getEndpoint());
        dto.setMethod(log.getMethod());
        dto.setStatusCode(log.getStatusCode());
        dto.setIpAddress(log.getIpAddress());
        dto.setRequestTimestamp(log.getRequestTimestamp());
        dto.setResponseTimeMs(log.getResponseTimeMs());
        return dto;
    }
}
