package com.study.tracker.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新知识点状态
 */
@Data
public class TopicStatusUpdate {
    @NotNull
    private Integer status;

    private Integer masteryLevel;
}
