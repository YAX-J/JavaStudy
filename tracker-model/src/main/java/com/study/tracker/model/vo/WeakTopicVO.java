package com.study.tracker.model.vo;

import lombok.Data;

/**
 * 薄弱知识点
 */
@Data
public class WeakTopicVO {
    private Long topicId;
    private String title;
    private String moduleName;
    private Integer changeCount;
}
