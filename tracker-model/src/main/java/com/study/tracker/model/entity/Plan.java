package com.study.tracker.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.study.tracker.model.enums.PlanStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习计划
 */
@Data
@TableName("plan")
public class Plan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private LocalDate targetDate;
    private Integer dailyMinutes;
    private PlanStatus status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
