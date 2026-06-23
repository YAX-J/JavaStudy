package com.study.tracker.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡记录
 */
@Data
@TableName("check_in")
public class CheckIn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long topicId;
    private LocalDate checkDate;
    private Integer durationMinutes;
    private Integer feeling;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
