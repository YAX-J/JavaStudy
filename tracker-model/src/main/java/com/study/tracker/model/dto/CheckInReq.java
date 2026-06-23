package com.study.tracker.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * 打卡请求
 */
@Data
public class CheckInReq {
    @NotNull
    private Long planId;

    @NotNull
    private Long topicId;

    @NotNull
    private LocalDate checkDate;

    @NotNull
    private Integer durationMinutes;

    private Integer feeling;
}
