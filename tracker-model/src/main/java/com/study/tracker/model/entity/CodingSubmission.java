package com.study.tracker.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 代码提交记录
 */
@Data
@TableName("coding_submission")
public class CodingSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long exerciseId;
    private String userCode;
    private Integer status;
    private String output;
    private String errorMessage;
    private LocalDateTime submittedAt;
}
