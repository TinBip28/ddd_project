package com.xxxx.ddd.application.service.key;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ApiKeyService {

    private final Map<String, String> apiKeysStore = new HashMap<>();

    public ApiKeyService() {
        // Demo -> .env or db
        apiKeysStore.put("test-access-key-123", "super-secret-key-for-123-dont-hardcode-in-prod");
        apiKeysStore.put("another-access-key-456", "very-secure-secret-for-456-use-env-vars");
    }

    public Optional<String> getSecretKey(String accessKey) {
        return Optional.ofNullable(apiKeysStore.get(accessKey));
    }
}
