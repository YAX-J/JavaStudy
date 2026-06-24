package com.study.tracker.model.vo;

import lombok.Data;

/**
 * 代码运行结果
 */
@Data
public class CodeRunResultVO {
    private String output;
    private String errorMessage;
    private Boolean passed;
    private Integer executionTimeMs;
    private Long submissionId;
}
