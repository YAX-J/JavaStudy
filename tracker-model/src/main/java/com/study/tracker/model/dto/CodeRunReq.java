package com.study.tracker.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 运行代码请求
 */
@Data
public class CodeRunReq {
    @NotNull
    private Long exerciseId;

    @NotBlank
    private String code;
}
