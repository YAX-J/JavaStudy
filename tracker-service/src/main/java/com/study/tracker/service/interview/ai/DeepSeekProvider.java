package com.study.tracker.service.interview.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek API 实现（兼容 OpenAI 接口格式）
 */
@Slf4j
@Component
public class DeepSeekProvider extends OpenAiProvider {
    // DeepSeek 完全兼容 OpenAI API，直接继承即可
    // base_url 配置为 https://api.deepseek.com
}
