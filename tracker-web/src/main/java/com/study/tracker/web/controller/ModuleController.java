package com.study.tracker.web.controller;

import com.study.tracker.common.result.R;
import com.study.tracker.model.dto.TopicStatusUpdate;
import com.study.tracker.model.vo.ModuleVO;
import com.study.tracker.model.vo.TopicBrief;
import com.study.tracker.model.vo.TopicDetailVO;
import com.study.tracker.service.module.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识模块相关接口
 */
@Tag(name = "知识模块")
@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @Operation(summary = "知识模块列表（含知识点）")
    @GetMapping
    public R<List<ModuleVO>> list(@RequestParam(required = false) Integer status) {
        return R.ok(moduleService.listModulesWithTopics(status));
    }

    @Operation(summary = "今日待复习")
    @GetMapping("/topics/due-review")
    public R<List<TopicBrief>> listDueForReview() {
        return R.ok(moduleService.listDueForReview());
    }

    @Operation(summary = "知识点详情")
    @GetMapping("/topics/{id}")
    public R<TopicDetailVO> getTopicDetail(@PathVariable Long id) {
        return R.ok(moduleService.getTopicDetail(id));
    }

    @Operation(summary = "更新知识点状态")
    @PutMapping("/topics/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody TopicStatusUpdate req) {
        moduleService.updateStatus(id, req);
        return R.ok();
    }
}
