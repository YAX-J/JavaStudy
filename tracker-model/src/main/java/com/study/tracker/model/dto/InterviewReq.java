package com.study.tracker.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建面试会话
 */
@Data
public class CreateSessionReq {
    @NotBlank
    private String title;

    /** 考察模块ID列表，逗号分隔 */
    private String moduleIds;

    @NotNull
    private Integer difficulty;
}

/**
 * 提交回答
 */
@Data
public class SubmitAnswerReq {
    @NotBlank
    private String answer;
}

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
