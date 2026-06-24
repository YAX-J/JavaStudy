package com.study.tracker.web.controller;

import com.study.tracker.common.result.R;
import com.study.tracker.model.dto.AiConfigReq;
import com.study.tracker.model.dto.CreateSessionReq;
import com.study.tracker.model.dto.SubmitAnswerReq;
import com.study.tracker.model.entity.AiConfig;
import com.study.tracker.model.vo.*;
import com.study.tracker.service.interview.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 模拟面试接口
 */
@Tag(name = "AI 模拟面试")
@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @Operation(summary = "面试会话列表")
    @GetMapping("/sessions")
    public R<List<InterviewSessionVO>> listSessions() {
        return R.ok(interviewService.listSessions());
    }

    @Operation(summary = "创建面试会话")
    @PostMapping("/sessions")
    public R<InterviewSessionVO> createSession(@Valid @RequestBody CreateSessionReq req) {
        return R.ok(interviewService.createSession(req));
    }

    @Operation(summary = "获取对话消息")
    @GetMapping("/sessions/{id}/messages")
    public R<List<InterviewMessageVO>> getMessages(@PathVariable Long id) {
        return R.ok(interviewService.getMessages(id));
    }

    @Operation(summary = "提交回答")
    @PostMapping("/sessions/{id}/answer")
    public R<InterviewMessageVO> submitAnswer(@PathVariable Long id, @Valid @RequestBody SubmitAnswerReq req) {
        return R.ok(interviewService.submitAnswer(id, req));
    }

    @Operation(summary = "跳过当前题")
    @PostMapping("/sessions/{id}/skip")
    public R<Void> skipQuestion(@PathVariable Long id) {
        interviewService.skipQuestion(id);
        return R.ok();
    }

    @Operation(summary = "结束面试并生成报告")
    @PostMapping("/sessions/{id}/end")
    public R<InterviewReportVO> endSession(@PathVariable Long id) {
        return R.ok(interviewService.endSession(id));
    }

    @Operation(summary = "获取面试报告")
    @GetMapping("/sessions/{id}/report")
    public R<InterviewReportVO> getReport(@PathVariable Long id) {
        return R.ok(interviewService.endSession(id));
    }
}
