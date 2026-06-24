package com.study.tracker.service.stats.mapper;

import lombok.Data;

@Data
public class ModuleStatsRow {
    private Long moduleId;
    private String moduleName;
    private Integer mastered;
    private Integer inProgress;
    private Integer notStarted;
    private Integer total;
}
