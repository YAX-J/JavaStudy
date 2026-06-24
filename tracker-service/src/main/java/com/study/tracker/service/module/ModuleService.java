package com.study.tracker.service.module;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.tracker.common.exception.BizException;
import com.study.tracker.model.dto.TopicStatusUpdate;
import com.study.tracker.model.entity.Module;
import com.study.tracker.model.entity.Topic;
import com.study.tracker.model.enums.TopicStatus;
import com.study.tracker.model.vo.ModuleVO;
import com.study.tracker.model.vo.TopicBrief;
import com.study.tracker.model.vo.TopicDetailVO;
import com.study.tracker.service.module.mapper.ModuleMapper;
import com.study.tracker.service.module.mapper.TopicMapper;
import com.study.tracker.service.note.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识模块服务
 */
@Slf4j
@Service
public class ModuleService extends ServiceImpl<ModuleMapper, Module> {

    private final TopicMapper topicMapper;
    private final NoteService noteService;

    public ModuleService(TopicMapper topicMapper, NoteService noteService) {
        this.topicMapper = topicMapper;
        this.noteService = noteService;
    }

    /**
     * 查询所有模块及其知识点
     */
    public List<ModuleVO> listModulesWithTopics(Integer status) {
        List<Module> modules = list(Wrappers.<Module>lambdaQuery().orderByAsc(Module::getSortOrder));
        List<Topic> allTopics = topicMapper.selectList(
                Wrappers.<Topic>lambdaQuery()
                        .eq(status != null, Topic::getStatus, status != null ? status : null)
                        .orderByAsc(Topic::getPriority, Topic::getDifficulty)
        );
        Map<Long, List<Topic>> topicMap = allTopics.stream()
                .collect(Collectors.groupingBy(Topic::getModuleId));

        return modules.stream().map(m -> {
            ModuleVO vo = new ModuleVO();
            vo.setId(m.getId());
            vo.setName(m.getName());
            vo.setSortOrder(m.getSortOrder());
            List<Topic> moduleTopics = topicMap.getOrDefault(m.getId(), Collections.emptyList());
            vo.setTopics(moduleTopics.stream().map(this::toBrief).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 知识点详情
     */
    public TopicDetailVO getTopicDetail(Long id) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BizException(404, "知识点不存在");
        }
        Module module = getById(topic.getModuleId());

        TopicDetailVO vo = new TopicDetailVO();
        vo.setId(topic.getId());
        vo.setModuleId(topic.getModuleId());
        vo.setModuleName(module != null ? module.getName() : "");
        vo.setTitle(topic.getTitle());
        vo.setDifficulty(topic.getDifficulty());
        vo.setPriority(topic.getPriority());
        vo.setStatus(topic.getStatus());
        vo.setMasteryLevel(topic.getMasteryLevel());
        vo.setStatusChangeCount(topic.getStatusChangeCount());
        vo.setLastReviewAt(topic.getLastReviewAt());
        vo.setNextReviewAt(topic.getNextReviewAt());
        vo.setRecentNotes(noteService.listBriefByTopic(id));
        return vo;
    }

    /**
     * 更新知识点状态
     */
    @Transactional
    @CacheEvict(value = {"stats:overview", "stats:module"}, allEntries = true)
    public void updateStatus(Long id, TopicStatusUpdate req) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BizException(404, "知识点不存在");
        }

        TopicStatus[] values = TopicStatus.values();
        if (req.getStatus() < 0 || req.getStatus() >= values.length) {
            throw new BizException(400, "无效的状态值: " + req.getStatus());
        }
        TopicStatus newStatus = values[req.getStatus()];
        if (!newStatus.equals(topic.getStatus())) {
            topic.setStatusChangeCount(topic.getStatusChangeCount() + 1);
        }
        topic.setStatus(newStatus);
        if (req.getMasteryLevel() != null) {
            topic.setMasteryLevel(req.getMasteryLevel());
        }
        // 改为已掌握时，设置艾宾浩斯复习时间
        if (newStatus == TopicStatus.MASTERED) {
            topic.setLastReviewAt(LocalDateTime.now());
            topic.setNextReviewAt(LocalDateTime.now().plusDays(1));
        }
        topicMapper.updateById(topic);
        log.info("知识点 {} 状态更新为 {}", topic.getTitle(), newStatus.getLabel());
    }

    /**
     * 今日待复习
     */
    public List<TopicBrief> listDueForReview() {
        return topicMapper.selectDueForReview().stream()
                .map(this::toBrief)
                .collect(Collectors.toList());
    }

    private TopicBrief toBrief(Topic t) {
        TopicBrief b = new TopicBrief();
        b.setId(t.getId());
        b.setTitle(t.getTitle());
        b.setDifficulty(t.getDifficulty());
        b.setPriority(t.getPriority());
        b.setStatus(t.getStatus());
        b.setMasteryLevel(t.getMasteryLevel());
        b.setLastReviewAt(t.getLastReviewAt());
        b.setNextReviewAt(t.getNextReviewAt());
        return b;
    }
}
