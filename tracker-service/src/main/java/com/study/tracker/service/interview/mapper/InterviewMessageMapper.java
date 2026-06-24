package com.study.tracker.service.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.tracker.model.entity.InterviewMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface InterviewMessageMapper extends BaseMapper<InterviewMessage> {

    @Select("SELECT * FROM interview_message WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<InterviewMessage> selectBySession(@Param("sessionId") Long sessionId);
}
