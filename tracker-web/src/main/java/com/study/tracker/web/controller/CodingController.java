package com.study.tracker.web.controller;

import com.study.tracker.common.result.R;
import com.study.tracker.model.dto.CodeRunReq;
import com.study.tracker.model.vo.CodeRunResultVO;
import com.study.tracker.model.vo.ExerciseVO;
import com.study.tracker.model.vo.SubmissionVO;
import com.study.tracker.service.exercise.CodingExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线代码练习接口
 */
@Tag(name = "在线代码练习")
@RestController
@RequestMapping("/api")
public class CodingController {

    private final CodingExerciseService codingService;

    public CodingController(CodingExerciseService codingService) {
        this.codingService = codingService;
    }

    @Operation(summary = "某知识点的练习列表")
    @GetMapping("/topics/{topicId}/exercises")
    public R<List<ExerciseVO>> listExercises(@PathVariable Long topicId) {
        return R.ok(codingService.listByTopic(topicId));
    }

    @Operation(summary = "练习详情")
    @GetMapping("/exercises/{id}")
    public R<ExerciseVO> getExercise(@PathVariable Long id) {
        return R.ok(codingService.getDetail(id));
    }

    @Operation(summary = "运行代码")
    @PostMapping("/exercises/{id}/run")
    public R<CodeRunResultVO> runCode(@PathVariable Long id, @Valid @RequestBody CodeRunReq req) {
        req.setExerciseId(id);
        return R.ok(codingService.runCode(req));
    }

    @Operation(summary = "提交历史")
    @GetMapping("/exercises/{id}/submissions")
    public R<List<SubmissionVO>> listSubmissions(@PathVariable Long id) {
        return R.ok(codingService.listSubmissions(id));
    }
}
