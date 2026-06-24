package com.study.tracker.model.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 模块进度（雷达图数据）
 */
@Data
public class ModuleProgressVO {
    private String moduleName;
    private Integer total;
    private Integer mastered;
    private Integer inProgress;
    private Integer notStarted;
    private BigDecimal percent;
}
