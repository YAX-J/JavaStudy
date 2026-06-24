package com.study.tracker.service.plan;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.tracker.common.exception.BizException;
import com.study.tracker.model.dto.CheckInReq;
import com.study.tracker.model.dto.PlanCreateReq;
import com.study.tracker.model.entity.*;
import com.study.tracker.model.enums.PlanStatus;
import com.study.tracker.model.vo.CalendarVO;
import com.study.tracker.model.vo.PlanVO;
import com.study.tracker.model.vo.TodayTaskVO;
import com.study.tracker.service.plan.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 学习计划与打卡服务
 */
@Slf4j
@Service
public class PlanService extends ServiceImpl<PlanMapper, Plan> {

    private final PlanItemMapper planItemMapper;
    private final CheckInMapper checkInMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STREAK_KEY = "checkin:streak:";

    public PlanService(PlanItemMapper planItemMapper, CheckInMapper checkInMapper,
                       RedisTemplate<String, Object> redisTemplate) {
        this.planItemMapper = planItemMapper;
        this.checkInMapper = checkInMapper;
        this.redisTemplate = redisTemplate;
    }

    // ==================== 计划管理 ====================

    public List<PlanVO> listPlans() {
        return list(Wrappers.<Plan>lambdaQuery().orderByDesc(Plan::getCreatedAt))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional
    public PlanVO createPlan(PlanCreateReq req) {
        Plan plan = new Plan();
        plan.setTitle(req.getTitle());
        plan.setTargetDate(req.getTargetDate());
        plan.setDailyMinutes(req.getDailyMinutes());
        plan.setStatus(PlanStatus.IN_PROGRESS);
        save(plan);

        // 批量插入计划项
        for (PlanCreateReq.DayTopic day : req.getSchedule()) {
            for (Long topicId : day.getTopicIds()) {
                PlanItem item = new PlanItem();
                item.setPlanId(plan.getId());
                item.setTopicId(topicId);
                item.setDayNumber(day.getDayNumber());
                planItemMapper.insert(item);
            }
        }
        log.info("计划已创建: {}, 共 {} 天", plan.getTitle(), req.getSchedule().size());
        return toVO(plan);
    }

    public void abandonPlan(Long planId) {
        Plan plan = getById(planId);
        if (plan == null) throw new BizException(404, "计划不存在");
        plan.setStatus(PlanStatus.ABANDONED);
        updateById(plan);
    }

    // ==================== 今日任务 ====================

    public TodayTaskVO getTodayTask(Long planId) {
        Plan plan = getById(planId);
        if (plan == null) throw new BizException(404, "计划不存在");

        // 计算当前是第几天：从计划创建日开始算
        LocalDate startDate = plan.getCreatedAt().toLocalDate();
        int dayNumber = (int) ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1;

        List<PlanItemWithTopic> items = planItemMapper.selectByPlanAndDay(planId, dayNumber);

        TodayTaskVO vo = new TodayTaskVO();
        vo.setDate(LocalDate.now());
        vo.setDayNumber(dayNumber);
        List<TodayTaskVO.TaskItem> taskItems = new ArrayList<>();
        boolean allChecked = !items.isEmpty();

        for (PlanItemWithTopic item : items) {
            TodayTaskVO.TaskItem ti = new TodayTaskVO.TaskItem();
            ti.setTopicId(item.getTopicId());
            ti.setTopicTitle(item.getTopicTitle());
            boolean checked = checkInMapper.selectList(
                    Wrappers.<CheckIn>lambdaQuery()
                            .eq(CheckIn::getPlanId, planId)
                            .eq(CheckIn::getTopicId, item.getTopicId())
                            .eq(CheckIn::getCheckDate, LocalDate.now())
            ).size() > 0;
            ti.setChecked(checked);
            if (!checked) allChecked = false;
            taskItems.add(ti);
        }
        vo.setItems(taskItems);
        vo.setAllChecked(allChecked);
        return vo;
    }

    // ==================== 打卡 ====================

    @Transactional
    public void checkIn(CheckInReq req) {
        // 幂等检查：同一天同一知识点不重复打卡
        Long count = checkInMapper.selectCount(
                Wrappers.<CheckIn>lambdaQuery()
                        .eq(CheckIn::getPlanId, req.getPlanId())
                        .eq(CheckIn::getTopicId, req.getTopicId())
                        .eq(CheckIn::getCheckDate, req.getCheckDate())
        );
        if (count > 0) {
            throw new BizException(400, "今天该知识点已打卡，请勿重复操作");
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setPlanId(req.getPlanId());
        checkIn.setTopicId(req.getTopicId());
        checkIn.setCheckDate(req.getCheckDate());
        checkIn.setDurationMinutes(req.getDurationMinutes());
        checkIn.setFeeling(req.getFeeling());
        checkInMapper.insert(checkIn);

        // 刷新 Redis 连续打卡缓存
        refreshStreak(req.getPlanId());
        log.info("打卡成功: plan={}, topic={}", req.getPlanId(), req.getTopicId());
    }

    // ==================== 打卡日历 ====================

    public CalendarVO getCalendar(Long planId, String month) {
        // 参数校验
        if (month == null || !month.matches("\\d{4}-\\d{2}")) {
            throw new BizException(400, "月份格式错误，请使用 yyyy-MM 格式");
        }
        // month: 2026-06
        LocalDate start = LocalDate.parse(month + "-01");
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<LocalDate> dates = checkInMapper.selectDateRange(planId, start);
        int streak = getCurrentStreak(planId);
        int longest = getLongestStreak(planId);

        CalendarVO vo = new CalendarVO();
        vo.setMonth(month);
        vo.setCheckedDates(dates);
        vo.setCurrentStreak(streak);
        vo.setLongestStreak(longest);
        return vo;
    }

    // ==================== 连续打卡 ====================

    private void refreshStreak(Long planId) {
        redisTemplate.delete(STREAK_KEY + planId);
    }

    private int getCurrentStreak(Long planId) {
        String key = STREAK_KEY + planId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Integer && (Integer) cached > 0) {
            return (Integer) cached;
        }

        List<LocalDate> recentDates = checkInMapper.selectRecentDates(planId);
        if (recentDates.isEmpty()) return 0;

        // selectRecentDates 返回的是 ORDER BY check_date DESC（最新在前）
        // 所以 recentDates[0] 是最新的打卡日期
        int streak = 1;
        for (int i = 0; i < recentDates.size() - 1; i++) {
            long diff = ChronoUnit.DAYS.between(recentDates.get(i + 1), recentDates.get(i));
            if (diff == 1) {
                streak++;
            } else {
                break;
            }
        }
        // 检查今天或昨天是否有打卡
        long daysSinceLast = ChronoUnit.DAYS.between(recentDates.get(0), LocalDate.now());
        if (daysSinceLast > 1) streak = 0;

        redisTemplate.opsForValue().set(key, streak, 30, TimeUnit.MINUTES);
        return streak;
    }

    private int getLongestStreak(Long planId) {
        List<LocalDate> dates = checkInMapper.selectDateRange(
                planId, LocalDate.now().minusYears(1));
        if (dates.isEmpty()) return 0;

        int longest = 1, current = 1;
        for (int i = 1; i < dates.size(); i++) {
            long diff = ChronoUnit.DAYS.between(dates.get(i - 1), dates.get(i));
            if (diff <= 1) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 1;
            }
        }
        return longest;
    }

    // ==================== 辅助 ====================

    private PlanVO toVO(Plan plan) {
        PlanVO vo = new PlanVO();
        vo.setId(plan.getId());
        vo.setTitle(plan.getTitle());
        vo.setTargetDate(plan.getTargetDate());
        vo.setDailyMinutes(plan.getDailyMinutes());
        vo.setStatus(plan.getStatus());
        vo.setCreatedAt(plan.getCreatedAt());

        // 统计
        List<PlanItemWithTopic> allItems = planItemMapper.selectByPlan(plan.getId());
        vo.setTotalTopics(allItems.size());
        Integer maxDay = allItems.stream().mapToInt(PlanItemWithTopic::getDayNumber).max().orElse(0);
        vo.setTotalDays(maxDay);
        int diffDays = (int) ChronoUnit.DAYS.between(plan.getCreatedAt().toLocalDate(), LocalDate.now());
        vo.setCurrentDay(Math.max(1, diffDays + 1));

        return vo;
    }
}
