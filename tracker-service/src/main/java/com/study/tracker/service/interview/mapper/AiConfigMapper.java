package com.study.tracker.service.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.tracker.model.entity.AiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiConfigMapper extends BaseMapper<AiConfig> {

    @Select("SELECT * FROM ai_config WHERE is_active = 1 LIMIT 1")
    AiConfig selectActive();
}
