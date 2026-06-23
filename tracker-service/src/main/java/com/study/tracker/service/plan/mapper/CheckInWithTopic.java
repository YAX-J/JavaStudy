package com.study.tracker.service.plan.mapper;

import lombok.Data;
import java.time.LocalDate;

/**
 * CheckIn 联表查询结果
 */
@Data
public class CheckInWithTopic {
    private Long id;
    private Long planId;
    private Long topicId;
    private LocalDate checkDate;
    private Integer durationMinutes;
    private Integer feeling;
    private String topicTitle;
}
