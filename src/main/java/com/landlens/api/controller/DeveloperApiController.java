package com.landlens.api.controller;

import com.landlens.api.dto.ApiKeyResponseDto;
import com.landlens.api.dto.ApiLogResponseDto;
import com.landlens.api.mapper.ApiMapper;
import com.landlens.api.model.ApiKey;
import com.landlens.api.model.ApiLog;
import com.landlens.api.service.DeveloperApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/developer")
@Transactional
public class DeveloperApiController {

    @Autowired
    private DeveloperApiService developerApiService;

    @PostMapping("/keys")
    public ResponseEntity<Object> createKey(@RequestParam String name, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        DeveloperApiService.KeyCreationResult result = developerApiService.createApiKey(userId, name);
        
        Map<String, Object> response = new HashMap<>();
        response.put("apiKeyId", result.getApiKey().getId());
        response.put("name", result.getApiKey().getName());
        response.put("status", result.getApiKey().getStatus());
        response.put("expiryDate", result.getApiKey().getExpiryDate());
        response.put("rawApiKey", result.getRawKey());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/keys")
    public ResponseEntity<List<ApiKeyResponseDto>> getMyKeys(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        List<ApiKey> list = developerApiService.getUserApiKeys(userId);
        List<ApiKeyResponseDto> dtoList = list.stream()
                .map(ApiMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/keys/{id}")
    public ResponseEntity<Object> revokeKey(@PathVariable UUID id, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        developerApiService.revokeApiKey(id, userId);
        return ResponseEntity.ok(Map.of("message", "API key revoked successfully"));
    }

    @GetMapping("/keys/{id}/logs")
    public ResponseEntity<List<ApiLogResponseDto>> getLogs(@PathVariable UUID id, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        List<ApiLog> list = developerApiService.getLogsForKey(id, userId);
        List<ApiLogResponseDto> dtoList = list.stream()
                .map(ApiMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }
}
