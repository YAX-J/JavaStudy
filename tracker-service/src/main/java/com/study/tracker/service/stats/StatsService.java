package com.study.tracker.service.stats;

import com.study.tracker.model.vo.*;
import com.study.tracker.service.stats.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计面板服务
 */
@Slf4j
@Service
public class StatsService {

    private final StatsMapper statsMapper;

    public StatsService(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    /**
     * 总览数据（30分钟缓存）
     */
    @Cacheable(value = "stats:overview", key = "'overview'")
    public OverviewVO getOverview() {
        OverviewRow row = statsMapper.selectOverview();
        OverviewVO vo = new OverviewVO();
        if (row != null) {
            vo.setTotalTopics(row.getTotalTopics());
            vo.setMastered(row.getMastered());
            vo.setInProgress(row.getInProgress());
            vo.setNotStarted(row.getNotStarted());
            vo.setAvgMastery(BigDecimal.valueOf(row.getAvgMastery())
                    .setScale(1, RoundingMode.HALF_UP));
            if (row.getTotalTopics() > 0) {
                vo.setMasteredPercent(BigDecimal.valueOf(
                        row.getMastered() * 100.0 / row.getTotalTopics())
                        .setScale(0, RoundingMode.HALF_UP));
            } else {
                vo.setMasteredPercent(BigDecimal.ZERO);
            }
        }
        vo.setStreak(calcGlobalStreak());
        WeeklyRow wr = statsMapper.selectWeekly(getMonday(), getSunday());
        vo.setWeeklyMinutes(wr != null ? wr.getWeeklyMinutes() : 0);
        return vo;
    }

    /**
     * 各模块进度（雷达图数据）
     */
    @Cacheable(value = "stats:module", key = "'module'")
    public List<ModuleProgressVO> getModuleProgress() {
        List<ModuleStatsRow> rows = statsMapper.selectModuleStats();
        return rows.stream().map(r -> {
            ModuleProgressVO vo = new ModuleProgressVO();
            vo.setModuleName(r.getModuleName());
            vo.setTotal(r.getTotal());
            vo.setMastered(r.getMastered());
            vo.setInProgress(r.getInProgress());
            vo.setNotStarted(r.getNotStarted());
            if (r.getTotal() > 0) {
                vo.setPercent(BigDecimal.valueOf(r.getMastered() * 100.0 / r.getTotal())
                        .setScale(0, RoundingMode.HALF_UP));
            } else {
                vo.setPercent(BigDecimal.ZERO);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 近半年每日学习时长趋势
     */
    public List<TrendVO> getTrend() {
        LocalDate start = LocalDate.now().minusMonths(6);
        List<DailyMinutesRow> rows = statsMapper.selectDailyMinutes(start);
        return rows.stream().map(r -> {
            TrendVO vo = new TrendVO();
            vo.setDate(r.getDate().toString());
            vo.setMinutes(r.getMinutes());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 薄弱知识点 Top 5（经常在状态间反复的）
     */
    public List<WeakTopicVO> getWeakTopics() {
        return statsMapper.selectWeakTopics(5).stream().map(r -> {
            WeakTopicVO vo = new WeakTopicVO();
            vo.setTopicId(r.getId());
            vo.setTitle(r.getTitle());
            vo.setModuleName(r.getModuleName());
            vo.setChangeCount(r.getChangeCount());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 本周回顾
     */
    public WeeklyReportVO getWeeklyReport() {
        LocalDate monday = getMonday();
        LocalDate sunday = getSunday();
        WeeklyRow row = statsMapper.selectWeekly(monday, sunday);
        WeeklyReportVO vo = new WeeklyReportVO();
        vo.setWeekRange(monday + " ~ " + sunday);
        if (row != null) {
            vo.setWeeklyMinutes(row.getWeeklyMinutes());
            vo.setCheckDays(row.getCheckDays());
            vo.setTotalCheckIns(row.getTotalCheckIns());
            // 生成一句话总结
            if (row.getCheckDays() == 0) {
                vo.setSummary("本周还未开始学习，打开计划开始吧！");
            } else {
                double avgMin = row.getWeeklyMinutes() * 1.0 / row.getCheckDays();
                if (row.getCheckDays() >= 5) {
                    vo.setSummary(String.format("太棒了！本周坚持学习 %d 天，日均 %.0f 分钟，保持节奏！",
                            row.getCheckDays(), avgMin));
                } else {
                    vo.setSummary(String.format("本周学习了 %d 天共 %d 分钟，下周争取连续打卡哦。",
                            row.getCheckDays(), row.getWeeklyMinutes()));
                }
            }
        } else {
            vo.setWeeklyMinutes(0);
            vo.setCheckDays(0);
            vo.setTotalCheckIns(0);
            vo.setSummary("本周还未开始学习，打开计划开始吧！");
        }
        return vo;
    }

    /**
     * 打卡热力图数据（全局，近半年）
     */
    public List<Map<String, Object>> getHeatmap() {
        LocalDate start = LocalDate.now().minusMonths(6);
        List<DailyMinutesRow> rows = statsMapper.selectDailyMinutes(start);
        List<Map<String, Object>> result = new ArrayList<>();
        for (DailyMinutesRow r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("date", r.getDate().toString());
            m.put("minutes", r.getMinutes());
            result.add(m);
        }
        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 全局连续打卡天数
     */
    private int calcGlobalStreak() {
        LocalDate start = LocalDate.now().minusMonths(6);
        List<DailyMinutesRow> rows = statsMapper.selectDailyMinutes(start);
        if (rows.isEmpty()) {
            return 0;
        }

        // 按日期从近到远排，计算从今天往回连续的天数
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        Set<LocalDate> dateSet = rows.stream()
                .map(DailyMinutesRow::getDate)
                .collect(Collectors.toSet());

        while (dateSet.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        // 如果今天没打卡但昨天有，也算（cursor 是第一个缺失的日期）
        return streak;
    }

    private LocalDate getMonday() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }

    private LocalDate getSunday() {
  