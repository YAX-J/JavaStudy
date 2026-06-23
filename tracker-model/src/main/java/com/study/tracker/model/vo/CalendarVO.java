package com.study.tracker.model.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 打卡日历（按月）
 */
@Data
public class CalendarVO {
    private String month;
    private List<LocalDate> checkedDates;
    private Integer currentStreak;
    private Integer longestStreak;
}
