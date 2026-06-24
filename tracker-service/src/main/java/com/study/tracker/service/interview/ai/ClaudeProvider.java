package com.study.tracker.service.interview.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Claude (Anthropic) API 实现
 */
@Slf4j
@Component
public class ClaudeProvider implements AiProvider {

    private String apiKey;

    @Override
    public String chat(List<Map<String, String>> messages, String model) {
        // 分离 system 消息和对话消息
        String systemPrompt = null;
        List<Map<String, String>> convMessages = messages;
        if (!messages.isEmpty() && "system".equals(messages.get(0).get("role"))) {
            systemPrompt = messages.get(0).get("content");
            convMessages = messages.subList(1, messages.size());
        }

        JSONObject body = new JSONObject();
        body.set("model", model);
        body.set("max_tokens", 4096);
        if (systemPrompt != null) body.set("system", systemPrompt);
        body.set("messages", convMessages.stream().map(m -> {
            JSONObject msg = new JSONObject();
            msg.set("role", m.get("role"));
            msg.set("content", m.get("content"));
            return msg;
        }).collect(Collectors.toList()));

        try (HttpResponse resp = HttpRequest.post("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(60000)
                .execute()) {
            JSONObject json = JSONUtil.parseObj(resp.body());
            return json.getByPath("content[0].text", String.class);
        }
    }

    @Override
    public void chatStream(List<Map<String, String>> messages, String model, StreamCallback callback) {
        // Claude streaming 实现类似，此处保持简洁
        String result = chat(messages, model);
        if (result != null) {
            // 模拟逐 token 回调（实际应该用 SSE）
            for (int i = 0; i < result.length(); i += 5) {
                callback.onToken(result.substring(i, Math.min(i + 5, result.length())));
            }
        }
    }

    public void configure(String apiKey) {
        this.apiKey = apiKey;
    }
}
