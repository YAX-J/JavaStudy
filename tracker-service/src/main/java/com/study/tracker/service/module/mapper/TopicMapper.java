package com.study.tracker.service.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.tracker.model.entity.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    /**
     * 查询某模块下的知识点，可按状态筛选
     */
    @Select("<script>" +
            "SELECT * FROM topic WHERE module_id = #{moduleId} " +
            "<if test='status != null'> AND status = #{status} </if>" +
            " ORDER BY priority ASC, difficulty ASC" +
            "</script>")
    List<Topic> selectByModule(@Param("moduleId") Long moduleId, @Param("status") Integer status);

    /**
     * 今天该复习的知识点
     */
    @Select("SELECT * FROM topic WHERE next_review_at IS NOT NULL AND next_review_at <= NOW() ORDER BY next_review_at ASC")
    List<Topic> selectDueForReview();
}
