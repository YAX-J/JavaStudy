package com.study.tracker.model.vo;

import com.study.tracker.model.enums.TopicStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识模块 VO（含知识点列表）
 */
@Data
public class ModuleVO {
    private Long id;
    private String name;
    private Integer sortOrder;
    private List<TopicBrief> topics;
}

@Data
public class TopicBrief {
    private Long id;
    private String title;
    private Integer difficulty;
    private Integer priority;
    private TopicStatus status;
    private Integer masteryLevel;
    private LocalDateTime lastReviewAt;
    private LocalDateTime nextReviewAt;
}
