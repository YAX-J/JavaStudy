package com.study.tracker.web.controller;

import com.study.tracker.common.result.R;
import com.study.tracker.model.dto.CheckInReq;
import com.study.tracker.model.dto.PlanCreateReq;
import com.study.tracker.model.vo.CalendarVO;
import com.study.tracker.model.vo.PlanVO;
import com.study.tracker.model.vo.TodayTaskVO;
import com.study.tracker.service.plan.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习计划与打卡接口
 */
@Tag(name = "学习计划与打卡")
@RestController
@RequestMapping("/api")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @Operation(summary = "计划列表")
    @GetMapping("/plans")
    public R<List<PlanVO>> listPlans() {
        return R.ok(planService.listPlans());
    }

    @Operation(summary = "创建计划")
    @PostMapping("/plans")
    public R<PlanVO> createPlan(@Valid @RequestBody PlanCreateReq req) {
        return R.ok(planService.createPlan(req));
    }

    @Operation(summary = "放弃计划")
    @PutMapping("/plans/{id}/abandon")
    public R<Void> abandonPlan(@PathVariable Long id) {
        planService.abandonPlan(id);
        return R.ok();
    }

    @Operation(summary = "今日任务")
    @GetMapping("/plans/{id}/today")
    public R<TodayTaskVO> getTodayTask(@PathVariable Long id) {
        return R.ok(planService.getTodayTask(id));
    }

    @Operation(summary = "打卡")
    @PostMapping("/check-in")
    public R<Void> checkIn(@Valid @RequestBody CheckInReq req) {
        planService.checkIn(req);
        return R.ok();
    }

    @Operation(summary = "打卡日历")
    @GetMapping("/check-in/calendar/{planId}")
    public R<CalendarVO> getCalendar(@PathVariable Long planId, @RequestParam String month) {
        return R.ok(planService.getCalendar(planId, month));
    }
}
