package com.study.tracker.service.stats.mapper;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyMinutesRow {
    private LocalDate date;
    private Integer minutes;
}
