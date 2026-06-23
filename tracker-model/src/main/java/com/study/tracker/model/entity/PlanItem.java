package com.study.tracker.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 计划-知识点关联
 */
@Data
@TableName("plan_item")
public class PlanItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long topicId;
    private Integer dayNumber;
}
