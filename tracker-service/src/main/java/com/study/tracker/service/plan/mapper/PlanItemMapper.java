package com.study.tracker.service.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.tracker.model.entity.PlanItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlanItemMapper extends BaseMapper<PlanItem> {

    /**
     * 某计划第 N 天的学习任务
     */
    @Select("SELECT pi.*, t.title AS topicTitle, t.module_id AS moduleId FROM plan_item pi " +
            "JOIN topic t ON pi.topic_id = t.id " +
            "WHERE pi.plan_id = #{planId} AND pi.day_number = #{dayNumber}")
    List<PlanItemWithTopic> selectByPlanAndDay(@Param("planId") Long planId, @Param("dayNumber") int dayNumber);

    /**
     * 某计划所有任务（按天数分组）
     */
    @Select("SELECT pi.*, t.title AS topicTitle, t.module_id AS moduleId FROM plan_item pi " +
            "JOIN topic t ON pi.topic_id = t.id " +
            "WHERE pi.plan_id = #{planId} ORDER BY pi.day_number ASC")
    List<PlanItemWithTopic> selectByPlan(@Param("planId") Long planId);
}
