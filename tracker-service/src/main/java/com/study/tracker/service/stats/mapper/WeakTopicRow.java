package com.study.tracker.service.stats.mapper;

import lombok.Data;

@Data
public class WeakTopicRow {
    private Long id;
    private String title;
    private Integer changeCount;
    private String moduleName;
}
