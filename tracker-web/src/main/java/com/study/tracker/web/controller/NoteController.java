package com.study.tracker.web.controller;

import com.study.tracker.common.result.R;
import com.study.tracker.model.dto.NoteSaveReq;
import com.study.tracker.model.vo.NoteDetailVO;
import com.study.tracker.service.note.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 笔记相关接口
 */
@Tag(name = "学习笔记")
@RestController
@RequestMapping("/api")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @Operation(summary = "某知识点的笔记列表")
    @GetMapping("/topics/{topicId}/notes")
    public R<List<NoteDetailVO>> listByTopic(@PathVariable Long topicId) {
        return R.ok(noteService.listByTopic(topicId));
    }

    @Operation(summary = "创建笔记")
    @PostMapping("/topics/{topicId}/notes")
    public R<NoteDetailVO> create(@PathVariable Long topicId, @Valid @RequestBody NoteSaveReq req) {
        return R.ok(noteService.create(topicId, req));
    }

    @Operation(summary = "更新笔记")
    @PutMapping("/notes/{id}")
    public R<NoteDetailVO> update(@PathVariable Long id, @Valid @RequestBody NoteSaveReq req) {
        return R.ok(noteService.update(id, req));
    }

    @Operation(summary = "删除笔记")
    @DeleteMapping("/notes/{id}")
    public R<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return R.ok();
    }

    @Operation(summary = "搜索笔记")
    @GetMapping("/notes/search")
    public R<List<NoteDetailVO>> search(@RequestParam String keyword) {
        return R.ok(noteService.search(keyword));
    }
}
