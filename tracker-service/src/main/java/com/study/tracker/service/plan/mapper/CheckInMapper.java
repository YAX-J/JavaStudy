package com.study.tracker.service.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.tracker.model.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {

    /**
     * 某计划的打卡日历（按月）
     */
    @Select("SELECT * FROM check_in WHERE plan_id = #{planId} AND check_date >= #{start} AND check_date <= #{end} ORDER BY check_date ASC")
    List<CheckIn> selectByPlanAndMonth(@Param("planId") Long planId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * 某天的打卡记录
     */
    @Select("SELECT c.*, t.title AS topicTitle FROM check_in c " +
            "JOIN topic t ON c.topic_id = t.id " +
            "WHERE c.plan_id = #{planId} AND c.check_date = #{date}")
    List<CheckInWithTopic> selectByPlanAndDate(@Param("planId") Long planId, @Param("date") LocalDate date);

    /**
     * 近半年打卡日期（用于热力图）
     */
    @Select("SELECT DISTINCT check_date FROM check_in WHERE plan_id = #{planId} AND check_date >= #{start} ORDER BY check_date ASC")
    List<LocalDate> selectDateRange(@Param("planId") Long planId, @Param("start") LocalDate start);

    /**
     * 某计划连续打卡统计（基于最近未打卡的断点）
     */
    @Select("SELECT check_date FROM check_in WHERE plan_id = #{planId} ORDER BY check_date DESC")
    List<LocalDate> selectRecentDates(@Param("planId") Long planId);
}
