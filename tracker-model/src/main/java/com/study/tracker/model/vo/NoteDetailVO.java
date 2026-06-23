package com.study.tracker.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 笔记详情
 */
@Data
public class NoteDetailVO {
    private Long id;
    private Long topicId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
