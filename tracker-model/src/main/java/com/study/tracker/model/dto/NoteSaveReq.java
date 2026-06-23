package com.study.tracker.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新笔记
 */
@Data
public class NoteSaveReq {
    @NotBlank(message = "笔记标题不能为空")
    private String title;

    @NotBlank(message = "笔记内容不能为空")
    private String content;
}
