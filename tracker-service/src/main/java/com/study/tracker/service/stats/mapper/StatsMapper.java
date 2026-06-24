package com.study.tracker.service.stats.mapper;

import com.study.tracker.model.entity.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 统计模块专用的扩展 Mapper
 */
@Mapper
public interface StatsMapper {

    /**
     * 各模块进度统计（按 module 分组统计 topic 状态）
     */
    @Select("SELECT m.id AS moduleId, m.name AS moduleName, " +
            "SUM(CASE WHEN t.status = 2 THEN 1 ELSE 0 END) AS mastered, " +
            "SUM(CASE WHEN t.status = 1 THEN 1 ELSE 0 END) AS inProgress, " +
            "SUM(CASE WHEN t.status = 0 THEN 1 ELSE 0 END) AS notStarted, " +
            "COUNT(t.id) AS total " +
            "FROM module m LEFT JOIN topic t ON m.id = t.module_id " +
            "GROUP BY m.id, m.name ORDER BY m.sort_order ASC")
    List<ModuleStatsRow> selectModuleStats();

    /**
     * 总览统计
     */
    @Select("SELECT COUNT(*) AS totalTopics, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS mastered, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS inProgress, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS notStarted, " +
            "COALESCE(AVG(CASE WHEN mastery_level > 0 THEN mastery_level END), 0) AS avgMastery " +
            "FROM topic")
    OverviewRow selectOverview();

    /**
     * 最近N天每天的学习时长
     */
    @Select("SELECT check_date AS date, SUM(duration_minutes) AS minutes " +
            "FROM check_in WHERE check_date >= #{start} " +
            "GROUP BY check_date ORDER BY check_date ASC")
    List<DailyMinutesRow> selectDailyMinutes(@Param("start") java.time.LocalDate start);

    /**
     * 知识点薄弱点：状态变更次数最多的前 N 个
     */
    @Select("SELECT t.id, t.title, t.status_change_count AS changeCount, m.name AS moduleName " +
            "FROM topic t JOIN module m ON t.module_id = m.id " +
            "WHERE t.status_change_count > 0 " +
            "ORDER BY t.status_change_count DESC LIMIT #{limit}")
    List<WeakTopicRow> selectWeakTopics(@Param("limit") int limit);

    /**
     * 本周打卡统计
     */
    @Select("SELECT COALESCE(SUM(duration_minutes), 0) AS weeklyMinutes, " +
            "COUNT(DISTINCT check_date) AS checkDays, " +
            "COUNT(id) AS totalCheckIns " +
            "FROM check_in WHERE check_date >= #{monday} AND check_date <= #{sunday}")
    WeeklyRow selectWeekly(@Param("monday") java.time.LocalDate monday, @Param("sunday") java.time.LocalDate sunday);
}
