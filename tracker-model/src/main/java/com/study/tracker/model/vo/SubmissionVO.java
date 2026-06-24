package com.study.tracker.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提交历史
 */
@Data
public class SubmissionVO {
    private Long id;
    private String userCode;
    private Integer status;
    private String output;
    private String errorMessage;
    private LocalDateTime submittedAt;
}
