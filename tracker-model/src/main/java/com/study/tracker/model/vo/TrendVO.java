package com.study.tracker.model.vo;

import lombok.Data;

/**
 * 每日学习时长趋势
 */
@Data
public class TrendVO {
    private String date;
    private Integer minutes;
}
