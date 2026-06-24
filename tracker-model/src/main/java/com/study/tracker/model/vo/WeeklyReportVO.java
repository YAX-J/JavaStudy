package com.study.tracker.model.vo;

import lombok.Data;

/**
 * 周报
 */
@Data
public class WeeklyReportVO {
    private String weekRange;
    private Integer weeklyMinutes;
    private Integer checkDays;
    private Integer totalCheckIns;
    private String summary;
}
