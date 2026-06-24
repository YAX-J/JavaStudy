package com.study.tracker.service.exercise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.tracker.model.entity.CodingSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CodingSubmissionMapper extends BaseMapper<CodingSubmission> {

    @Select("SELECT * FROM coding_submission WHERE exercise_id = #{exerciseId} ORDER BY submitted_at DESC")
    List<CodingSubmission> selectByExercise(@Param("exerciseId") Long exerciseId);
}
