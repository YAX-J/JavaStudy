package com.study.tracker.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 知识点状态
 */
@Getter
public enum TopicStatus {
    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "学习中"),
    MASTERED(2, "已掌握");

    @EnumValue
    private final int code;
    private final String label;

    TopicStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
