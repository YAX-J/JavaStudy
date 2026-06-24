package com.study.tracker.model.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 总览统计
 */
@Data
public class OverviewVO {
    private Integer totalTopics;
    private Integer mastered;
    private Integer inProgress;
    private Integer notStarted;
    private BigDecimal avgMastery;
    private BigDecimal masteredPercent;
    private Integer streak;
    private Integer weeklyMinutes;
}
