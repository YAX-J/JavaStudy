package com.study.tracker.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 练习列表项
 */
@Data
public class ExerciseVO {
    private Long id;
    private Long topicId;
    private String title;
    private String description;
    private String templateCode;
    private Integer difficulty;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
