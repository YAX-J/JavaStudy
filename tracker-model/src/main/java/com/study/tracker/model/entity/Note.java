package com.study.tracker.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习笔记
 */
@Data
@TableName("note")
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long topicId;
    private String title;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
