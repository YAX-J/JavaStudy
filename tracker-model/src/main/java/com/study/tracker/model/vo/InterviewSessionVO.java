package com.study.tracker.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试会话列表
 */
@Data
public class InterviewSessionVO {
    private Long id;
    private String title;
    private String moduleIds;
    private Integer difficulty;
    private Integer questionCount;
    private BigDecimal totalScore;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
