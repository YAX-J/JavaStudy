package com.study.tracker.model.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 某天的计划任务
 */
@Data
public class TodayTaskVO {
    private LocalDate date;
    private Integer dayNumber;
    private List<TaskItem> items;
    private Boolean allChecked;

    @Data
    public static class TaskItem {
        private Long topicId;
        private String topicTitle;
        private Long moduleId;
        private String moduleName;
        private Boolean checked;
    }
}
