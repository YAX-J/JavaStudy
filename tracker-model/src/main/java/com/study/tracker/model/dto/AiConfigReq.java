package com.study.tracker.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 配置保存
 */
@Data
public class AiConfigReq {
    @NotBlank
    private String provider;

    @NotBlank
    private String apiKey;

    @NotBlank
    private String model;

    private String baseUrl;
}
