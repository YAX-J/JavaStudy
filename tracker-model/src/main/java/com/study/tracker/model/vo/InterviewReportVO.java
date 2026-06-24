package com.study.tracker.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 面试报告
 */
@Data
public class InterviewReportVO {
    private String overall;
    private List<QuestionScore> scores;
    private List<ModuleScore> moduleScores;
    private List<String> weakTopics;
    private String suggestions;
    private String estimate;

    @Data
    public static class QuestionScore {
        private String question;
        private String answer;
        private Integer score;
        private String feedback;
    }

    @Data
    public static class ModuleScore {
        private String moduleName;
        private BigDecimal avgScore;
        private Integer questionCount;
    }
}
