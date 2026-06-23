package com.study.tracker.service.plan.mapper;

import lombok.Data;

/**
 * PlanItem 联表查询结果（含 topic 标题）
 */
@Data
public class PlanItemWithTopic {
    private Long id;
    private Long planId;
    private Long topicId;
    private Integer dayNumber;
    private String topicTitle;
    private Long moduleId;
}
