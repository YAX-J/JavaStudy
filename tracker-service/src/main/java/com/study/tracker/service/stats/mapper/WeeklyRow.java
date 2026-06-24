package com.study.tracker.service.stats.mapper;

import lombok.Data;

@Data
public class WeeklyRow {
    private Integer weeklyMinutes;
    private Integer checkDays;
    private Integer totalCheckIns;
}
