package com.study.tracker.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试消息
 */
@Data
public class InterviewMessageVO {
    private Long id;
    private String role;
    private String content;
    private Long questionId;
    private Integer score;
    private String feedback;
    private LocalDateTime createdAt;
}
