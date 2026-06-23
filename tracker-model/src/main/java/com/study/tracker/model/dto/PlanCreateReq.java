package com.study.tracker.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建计划
 */
@Data
public class PlanCreateReq {
    @NotBlank
    private String title;

    private LocalDate targetDate;

    @NotNull
    private Integer dailyMinutes;

    /**
     * 知识点安排：dayNumber -> topicId 列表
     */
    @NotNull
    private List<DayTopic> schedule;

    @Data
    public static class DayTopic {
        private Integer dayNumber;
        private List<Long> topicIds;
    }
}
