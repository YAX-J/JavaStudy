package com.study.tracker.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 计划状态
 */
@Getter
public enum PlanStatus {
    IN_PROGRESS(0, "进行中"),
    COMPLETED(1, "已完成"),
    ABANDONED(2, "已放弃");

    @EnumValue
    private final int code;
    private final String label;

    PlanStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
