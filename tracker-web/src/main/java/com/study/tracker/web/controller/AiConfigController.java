package com.study.tracker.web.controller;

import com.study.tracker.common.result.R;
import com.study.tracker.model.dto.AiConfigReq;
import com.study.tracker.model.entity.AiConfig;
import com.study.tracker.service.interview.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * AI 配置接口
 */
@Tag(name = "AI 配置")
@RestController
@RequestMapping("/api/ai")
public class AiConfigController {

    private final InterviewService interviewService;

    public AiConfigController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @Operation(summary = "保存/更新 AI 配置")
    @PostMapping("/config")
    public R<Void> saveConfig(@Valid @RequestBody AiConfigReq req) {
        interviewService.saveAiConfig(req);
        return R.ok();
    }

    @Operation(summary = "查看当前 AI 配置")
    @GetMapping("/config")
    public R<AiConfig> getConfig() {
        return R.ok(interviewService.getActiveConfig());
    }
}
