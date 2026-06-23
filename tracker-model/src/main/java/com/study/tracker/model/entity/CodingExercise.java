package com.study.tracker.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 编程练习
 */
@Data
@TableName("coding_exercise")
public class CodingExercise {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long topicId;
    private String title;
    private String description;
    private String templateCode;
    private String testCode;
    private Integer difficulty;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
