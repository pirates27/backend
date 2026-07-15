package com.landlens.api.service;

import com.landlens.api.model.ApiKey;
import com.landlens.api.model.ApiLog;
import com.landlens.api.model.ApiRateLimit;
import com.landlens.api.model.ApiUsage;
import com.landlens.api.repository.ApiKeyRepository;
import com.landlens.api.repository.ApiLogRepository;
import com.landlens.api.repository.ApiRateLimitRepository;
import com.landlens.api.repository.ApiUsageRepository;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeveloperApiService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ApiUsageRepository apiUsageRepository;

    @Autowired
    private ApiLogRepository apiLogRepository;

    @Autowired
    private ApiRateLimitRepository apiRateLimitRepository;

    @Autowired
    private UserRepository userRepository;

    public static class KeyCreationResult {
        public ApiKey apiKey;
        public String rawKey;

        public KeyCreationResult(ApiKey apiKey, String rawKey) {
            this.apiKey = apiKey;
            this.rawKey = rawKey;
        }
    }

    @Transactional
    public KeyCreationResult createApiKey(UUID userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String prefix = "ll_live_";
        String randomPart = UUID.randomUUID().toString().replace("-", "");
        String rawKey = prefix + randomPart;
        String hashedKey = hashKey(rawKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setKeyHash(hashedKey);
        apiKey.setName(name);
        apiKey.setPrefix(prefix);
        apiKey.setStatus("ACTIVE");
        // Expires in 1 year
        apiKey.setExpiryDate(Instant.now().plus(365, ChronoUnit.DAYS));
        apiKey.setIsActive(true);
        ApiKey saved = apiKeyRepository.save(apiKey);

        // Seed default daily rate limit: 1000 requests
        ApiRateLimit rateLimit = new ApiRateLimit();
        rateLimit.setApiKey(saved);
        rateLimit.setLimitType("DAILY");
        rateLimit.setMaxRequests(1000);
        rateLimit.setCurrentWindowStart(Instant.now());
        rateLimit.setIsActive(true);
        apiRateLimitRepository.save(rateLimit);

        return new KeyCreationResult(saved, rawKey);
    }

    public List<ApiKey> getUserApiKeys(UUID userId) {
        return apiKeyRepository.findByUserIdAndStatus(userId, "ACTIVE");
    }

    @Transactional
    public void revokeApiKey(UUID keyId, UUID userId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("API key not found"));

        if (!apiKey.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: not the owner of this API key");
        }

        apiKey.setStatus("REVOKED");
        apiKeyRepository.save(apiKey);
    }

    @Transactional
    public ApiKey validateApiKey(String rawKey, int[] statusCodeOut) {
        String hashedKey = hashKey(rawKey);
        Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyHashAndStatus(hashedKey, "ACTIVE");

        if (keyOpt.isEmpty()) {
            statusCodeOut[0] = 401;
            return null;
        }

        ApiKey apiKey = keyOpt.get();

        if (apiKey.getExpiryDate() != null && apiKey.getExpiryDate().isBefore(Instant.now())) {
            apiKey.setStatus("EXPIRED");
            apiKeyRepository.save(apiKey);
            statusCodeOut[0] = 401;
            return null;
        }

        // Validate Rate Limit
        ApiRateLimit rateLimit = apiRateLimitRepository.findByApiKeyId(apiKey.getId())
                .orElseThrow(() -> new RuntimeException("Rate limit configuration missing"));

        LocalDate today = LocalDate.now();
        ApiUsage usage = apiUsageRepository.findByApiKeyIdAndUsageDate(apiKey.getId(), today)
                .orElseGet(() -> {
                    ApiUsage newUsage = new ApiUsage();
                    newUsage.setApiKey(apiKey);
                    newUsage.setUsageDate(today);
                    newUsage.setCallCount(0);
                    newUsage.setIsActive(true);
                    return apiUsageRepository.save(newUsage);
                });

        if (usage.getCallCount() >= rateLimit.getMaxRequests()) {
            statusCodeOut[0] = 429; // Too many requests
            return null;
        }

        // Increment Call Count
        usage.setCallCount(usage.getCallCount() + 1);
        apiUsageRepository.save(usage);

        statusCodeOut[0] = 200;
        return apiKey;
    }

    @Transactional
    public void logRequest(ApiKey apiKey, String endpoint, String method, int statusCode, String ipAddress, long responseTimeMs) {
        ApiLog log = new ApiLog();
        log.setApiKey(apiKey);
        log.setEndpoint(endpoint);
        log.setMethod(method.toUpperCase());
        log.setStatusCode(statusCode);
        log.setIpAddress(ipAddress);
        log.setRequestTimestamp(Instant.now());
        log.setResponseTimeMs((int) responseTimeMs);
        log.setIsActive(true);
        apiLogRepository.save(log);
    }

    public List<ApiLog> getLogsForKey(UUID keyId, UUID userId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("API key not found"));
        if (!key.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return apiLogRepository.findByApiKeyId(keyId);
    }

    private String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 digest hashing failed", e);
        }
    }
}
