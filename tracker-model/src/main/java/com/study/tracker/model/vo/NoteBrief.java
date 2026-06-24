package com.study.tracker.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 笔记摘要
 */
@Data
public class NoteBrief {
    private Long id;
    private String title;
    private LocalDateTime updatedAt;
}
