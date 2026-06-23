package com.study.tracker.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试会话
 */
@Data
@TableName("interview_session")
public class InterviewSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String moduleIds;
    private Integer difficulty;
    private Integer questionCount;
    private BigDecimal totalScore;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
