package com.study.tracker.service.note;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.tracker.common.exception.BizException;
import com.study.tracker.model.dto.NoteSaveReq;
import com.study.tracker.model.entity.Note;
import com.study.tracker.model.vo.NoteBrief;
import com.study.tracker.model.vo.NoteDetailVO;
import com.study.tracker.service.note.mapper.NoteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记服务
 */
@Slf4j
@Service
public class NoteService extends ServiceImpl<NoteMapper, Note> {

    /**
     * 某知识点下笔记列表
     */
    public List<NoteDetailVO> listByTopic(Long topicId) {
        return baseMapper.selectByTopic(topicId).stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    /**
     * 创建笔记
     */
    public NoteDetailVO create(Long topicId, NoteSaveReq req) {
        Note note = new Note();
        note.setTopicId(topicId);
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        save(note);
        log.info("笔记已创建: {}", note.getTitle());
        return toDetail(note);
    }

    /**
     * 更新笔记
     */
    public NoteDetailVO update(Long id, NoteSaveReq req) {
        Note note = getById(id);
        if (note == null) {
            throw new BizException(404, "笔记不存在");
        }
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        updateById(note);
        log.info("笔记已更新: {}", note.getTitle());
        return toDetail(note);
    }

    /**
     * 删除笔记
     */
    public void delete(Long id) {
        Note note = getById(id);
        if (note == null) {
            throw new BizException(404, "笔记不存在");
        }
        removeById(id);
        log.info("笔记已删除: {}", note.getTitle());
    }

    /**
     * 搜索笔记
     */
    public List<NoteDetailVO> search(String keyword) {
        // 先尝试全文索引，失败回退到 LIKE
        try {
            return baseMapper.searchFulltext(keyword).stream()
                    .map(this::toDetail)
                    .collect(Collectors.toList());
        } catch (org.springframework.dao.DataAccessException e) {
            log.debug("全文搜索不可用，降级为 LIKE 查询");
            return baseMapper.searchLike(keyword).stream()
                    .map(this::toDetail)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 转换为笔记摘要（供 TopicDetailVO 使用）
     */
    public List<NoteBrief> listBriefByTopic(Long topicId) {
        return baseMapper.selectByTopic(topicId).stream()
                .map(n -> {
                    NoteBrief b = new NoteBrief();
                    b.setId(n.getId());
                    b.setTitle(n.getTitle());
                    b.setUpdatedAt(n.getUpdatedAt());
                    return b;
                })
                .collect(Collectors.toList());
    }

    private NoteDetailVO toDetail(Note n) {
        NoteDetailVO vo = new NoteDetailVO();
        vo.setId(n.getId());
        vo.setTopicId(n.getTopicId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setCreatedAt(n.getCreatedAt());
        vo.setUpdatedAt(n.getUpdatedAt());
        return vo;
    }
}
