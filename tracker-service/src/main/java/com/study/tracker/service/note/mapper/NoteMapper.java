package com.study.tracker.service.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.tracker.model.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    /**
     * 某知识点下的笔记列表，按更新时间倒序
     */
    @Select("SELECT * FROM note WHERE topic_id = #{topicId} ORDER BY updated_at DESC")
    List<Note> selectByTopic(@Param("topicId") Long topicId);

    /**
     * 全文搜索笔记（支持 title 和 content 模糊匹配）
     */
    @Select("SELECT * FROM note WHERE MATCH(title, content) AGAINST(#{keyword} IN BOOLEAN MODE) ORDER BY updated_at DESC")
    List<Note> searchFulltext(@Param("keyword") String keyword);

    /**
     * 关键词模糊搜索（兜底，InnoDB 未开启全文索引时用）
     */
    @Select("SELECT * FROM note WHERE title LIKE CONCAT('%',#{keyword},'%') OR content LIKE CONCAT('%',#{keyword},'%') ORDER BY updated_at DESC")
    List<Note> searchLike(@Param("keyword") String keyword);
}
