package com.study.tracker.service.interview.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容接口实现（支持 OpenAI / DeepSeek / 通义千问 等）
 */
@Slf4j
@Component
public class OpenAiProvider implements AiProvider {

    @Override
    public String chat(List<Map<String, String>> messages, String model) {
        String apiKey = getApiKey();
        String baseUrl = getBaseUrl();

        JSONObject body = new JSONObject();
        body.set("model", model);
        body.set("messages", messages);
        body.set("temperature", 0.7);

        try (HttpResponse resp = HttpRequest.post(baseUrl + "/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(60000)
                .execute()) {
            JSONObject json = JSONUtil.parseObj(resp.body());
            return json.getByPath("choices[0].message.content", String.class);
        }
    }

    @Override
    public void chatStream(List<Map<String, String>> messages, String model, StreamCallback callback) {
        String apiKey = getApiKey();
        String baseUrl = getBaseUrl();

        JSONObject body = new JSONObject();
        body.set("model", model);
        body.set("messages", messages);
        body.set("temperature", 0.7);
        body.set("stream", true);

        try (HttpResponse resp = HttpRequest.post(baseUrl + "/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(120000)
                .execute()) {
            String bodyText = resp.body();
            // 解析 SSE 流式响应
            for (String line : bodyText.split("\n")) {
                if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                    try {
                        JSONObject json = JSONUtil.parseObj(line.substring(6));
                        JSONArray choices = json.getJSONArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                            if (delta != null && delta.containsKey("content")) {
                                callback.onToken(delta.getStr("content"));
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private String apiKey;
    private String baseUrl;

    public void configure(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    private String getApiKey() { return apiKey != null ? apiKey : ""; }
    private String getBaseUrl() { return baseUrl != null ? baseUrl : "https://api.openai.com"; }
}
