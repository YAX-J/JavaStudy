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
