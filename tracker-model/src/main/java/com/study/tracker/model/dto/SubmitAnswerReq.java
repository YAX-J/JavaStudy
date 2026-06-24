package com.study.tracker.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交回答
 */
@Data
public class SubmitAnswerReq {
    @NotBlank
    private String answer;
}
