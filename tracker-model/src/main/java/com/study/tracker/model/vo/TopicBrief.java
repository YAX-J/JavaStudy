package com.study.tracker.model.vo;

import com.study.tracker.model.enums.TopicStatus;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识点摘要
 */
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
