package com.study.tracker.service.interview.ai;

import java.util.List;
import java.util.Map;

/**
 * AI 提供商抽象接口
 */
public interface AiProvider {

    /**
     * 发起聊天请求
     * @param messages 对话历史
     * @param model 模型名称
     * @return AI 回复文本
     */
    String chat(List<Map<String, String>> messages, String model);

    /**
     * 流式聊天请求
     * @param messages 对话历史
     * @param model 模型名称
     * @param callback 每收到一段文本回调
     */
    void chatStream(List<Map<String, String>> messages, String model, StreamCallback callback);

    @FunctionalInterface
    interface StreamCallback {
        void onToken(String token);
    }
}
