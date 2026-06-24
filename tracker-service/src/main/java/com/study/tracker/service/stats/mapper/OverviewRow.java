package com.study.tracker.service.stats.mapper;

import lombok.Data;

@Data
public class OverviewRow {
    private Integer totalTopics;
    private Integer mastered;
    private Integer inProgress;
    private Integer notStarted;
    private Double avgMastery;
}
