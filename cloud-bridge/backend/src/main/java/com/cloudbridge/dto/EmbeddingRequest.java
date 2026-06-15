package com.cloudbridge.dto;

import lombok.Data;

@Data
public class EmbeddingRequest {
    private String model;
    private String input;
}
