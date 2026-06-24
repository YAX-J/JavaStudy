package com.study.tracker.web.controller;

import com.study.tracker.common.result.R;
import com.study.tracker.model.vo.*;
import com.study.tracker.service.stats.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计面板接口
 */
@Tag(name = "统计面板")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @Operation(summary = "总览数据")
    @GetMapping("/overview")
    public R<OverviewVO> getOverview() {
        return R.ok(statsService.getOverview());
    }

    @Operation(summary = "各模块进度（雷达图）")
    @GetMapping("/module-progress")
    public R<List<ModuleProgressVO>> getModuleProgress() {
        return R.ok(statsService.getModuleProgress());
    }

    @Operation(summary = "近半年每日学习时长趋势")
    @GetMapping("/trend")
    public R<List<TrendVO>> getTrend() {
        return R.ok(statsService.getTrend());
    }

    @Operation(summary = "薄弱知识点 Top 5")
    @GetMapping("/weak-topics")
    public R<List<WeakTopicVO>> getWeakTopics() {
        return R.ok(statsService.getWeakTopics());
    }

    @Operation(summary = "本周回顾")
    @GetMapping("/weekly-report")
    public R<WeeklyReportVO> getWeeklyReport() {
        return R.ok(statsService.getWeeklyReport());
    }

    @Operation(summary = "打卡热力图数据")
    @GetMapping("/heatmap")
    public R<List<Map<String, Object>>> getHeatmap() {
        return R.ok(statsService.getHeatmap());
    }
}
