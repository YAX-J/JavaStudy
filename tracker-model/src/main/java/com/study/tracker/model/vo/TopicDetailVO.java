package com.study.tracker.model.vo;

import com.study.tracker.model.enums.TopicStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识点详情
 */
@Data
public class TopicDetailVO {
    private Long id;
    private Long moduleId;
    private String moduleName;
    private String title;
    private Integer difficulty;
    private Integer priority;
    private TopicStatus status;
    private Integer masteryLevel;
    private Integer statusChangeCount;
    private LocalDateTime lastReviewAt;
    private LocalDateTime nextReviewAt;
    private List<NoteBrief> recentNotes;
}

@Data
public class NoteBrief {
    private Long id;
    private String title;
    private LocalDateTime updatedAt;
}
