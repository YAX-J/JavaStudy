package com.study.tracker.model.vo;

import com.study.tracker.model.enums.PlanStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 计划列表项
 */
@Data
public class PlanVO {
    private Long id;
    private String title;
    private LocalDate targetDate;
    private Integer dailyMinutes;
    private PlanStatus status;
    private Integer totalTopics;       // 总知识点数
    private Integer completedTopics;   // 已完成打卡数
    private Integer totalDays;         // 计划总天数
    private Integer currentDay;        // 当前第几天
    private LocalDateTime createdAt;
}
