package com.study.tracker.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 面试对话记录
 */
@Data
@TableName("interview_message")
public class InterviewMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private Long questionId;
    private Integer score;
    private String feedback;
    private LocalDateTime createdAt;
}
