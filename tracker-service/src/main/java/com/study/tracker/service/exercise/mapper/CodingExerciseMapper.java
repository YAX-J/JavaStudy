package com.study.tracker.service.exercise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.tracker.model.entity.CodingExercise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CodingExerciseMapper extends BaseMapper<CodingExercise> {

    @Select("SELECT * FROM coding_exercise WHERE topic_id = #{topicId} ORDER BY sort_order ASC")
    List<CodingExercise> selectByTopic(@Param("topicId") Long topicId);
}
