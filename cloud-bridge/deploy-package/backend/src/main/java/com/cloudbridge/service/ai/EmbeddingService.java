package com.cloudbridge.service.ai;

import com.cloudbridge.dto.EmbeddingRequest;
import com.cloudbridge.dto.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Service
public class EmbeddingService {

    @Value("${ai.api.embedding-url:http://localhost:8000/v1/embeddings}")
    private String embeddingApiUrl;

    @Value("${ai.api.embedding-key:#{null}}")
    private String embeddingApiKey;

    @Value("${ai.api.key}")
    private String fallbackApiKey;

    @Value("${ai.api.embedding-model:BAAI/bge-large-zh-v1.5}")
    private String embeddingModel;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        System.out.println("EmbeddingService initialized: URL=" + embeddingApiUrl + ", model=" + embeddingModel);
    }

    public List<Double> getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        EmbeddingRequest request = new EmbeddingRequest();
        request.setModel(embeddingModel);
        request.setInput(text);
        request.setInput_type("query");  // NVIDIA asymmetric models require input_type

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String key = (embeddingApiKey != null && !embeddingApiKey.isEmpty()) ? embeddingApiKey : fallbackApiKey;
        if (key != null && !key.isEmpty()) {
            headers.setBearerAuth(key);
        }

        HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<EmbeddingResponse> response = restTemplate.postForEntity(embeddingApiUrl, entity, EmbeddingResponse.class);
            if (response.getBody() != null && response.getBody().getData() != null && !response.getBody().getData().isEmpty()) {
                return response.getBody().getData().get(0).getEmbedding();
            }
        } catch (Exception e) {
            System.err.println("Embedding API failed: " + e.getMessage());
            // Fallback: Generate a deterministic mock vector for demo purposes if API fails
            // This is crucial so the app doesn't break if the external service is down/incompatible
            return generateMockEmbedding(text);
        }
        return Collections.emptyList();
    }
    
    // Mock fallback dimension - should match actual embedding model output
    // nvidia/nv-embedqa-e5-v5: 1024 dimensions
    // BAAI/bge-large-zh-v1.5: 1024 dimensions
    private static final int MOCK_EMBEDDING_DIM = 1024;
    
    // Simple deterministic hash-based vector for fallback/demo
    private List<Double> generateMockEmbedding(String text) {
        List<Double> vector = new ArrayList<>();
        long seed = text.hashCode();
        java.util.Random rng = new java.util.Random(seed);
        for (int i = 0; i < MOCK_EMBEDDING_DIM; i++) {
            vector.add(rng.nextGaussian());
        }
        // Normalize
        double norm = 0;
        for (Double v : vector) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) {
             for (int i = 0; i < MOCK_EMBEDDING_DIM; i++) {
                 vector.set(i, vector.get(i) / norm);
             }
        }
        return vector;
    }
}
