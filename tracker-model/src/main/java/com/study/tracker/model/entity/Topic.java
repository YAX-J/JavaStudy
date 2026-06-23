package com.study.tracker.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.study.tracker.model.enums.TopicStatus;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识点
 */
@Data
@TableName("topic")
public class Topic {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long moduleId;
    private String title;
    private Integer difficulty;
    private Integer priority;
    private TopicStatus status;
    private Integer masteryLevel;
    private Integer statusChangeCount;
    private LocalDateTime lastReviewAt;
    private LocalDateTime nextReviewAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
