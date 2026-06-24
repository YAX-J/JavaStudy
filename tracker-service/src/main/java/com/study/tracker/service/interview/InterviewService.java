package com.study.tracker.service.interview;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.tracker.common.exception.BizException;
import com.study.tracker.model.dto.AiConfigReq;
import com.study.tracker.model.dto.CreateSessionReq;
import com.study.tracker.model.dto.SubmitAnswerReq;
import com.study.tracker.model.entity.*;
import com.study.tracker.model.entity.Module;
import com.study.tracker.model.vo.*;
import com.study.tracker.service.interview.ai.AiProvider;
import com.study.tracker.service.interview.ai.ClaudeProvider;
import com.study.tracker.service.interview.ai.DeepSeekProvider;
import com.study.tracker.service.interview.ai.OpenAiProvider;
import com.study.tracker.service.interview.mapper.AiConfigMapper;
import com.study.tracker.service.interview.mapper.InterviewMessageMapper;
import com.study.tracker.service.interview.mapper.InterviewSessionMapper;
import com.study.tracker.service.module.mapper.ModuleMapper;
import com.study.tracker.service.module.mapper.TopicMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 模拟面试服务
 *
 * 核心流程：
 * 1. 用户创建会话 → 选择考察范围
 * 2. AI 逐一出题 → 用户文字回答 → AI 评分 + 追问
 * 3. 用户结束面试 → AI 生成面试报告
 */
@Slf4j
@Service
public class InterviewService extends ServiceImpl<InterviewSessionMapper, InterviewSession> {

    private final InterviewMessageMapper messageMapper;
    private final AiConfigMapper aiConfigMapper;
    private final ModuleMapper moduleMapper;
    private final TopicMapper topicMapper;
    private final OpenAiProvider openAiProvider;
    private final ClaudeProvider claudeProvider;
    private final DeepSeekProvider deepSeekProvider;

    public InterviewService(InterviewMessageMapper messageMapper, AiConfigMapper aiConfigMapper,
                            ModuleMapper moduleMapper, TopicMapper topicMapper,
                            OpenAiProvider openAiProvider, ClaudeProvider claudeProvider,
                            DeepSeekProvider deepSeekProvider) {
        this.messageMapper = messageMapper;
        this.aiConfigMapper = aiConfigMapper;
        this.moduleMapper = moduleMapper;
        this.topicMapper = topicMapper;
        this.openAiProvider = openAiProvider;
        this.claudeProvider = claudeProvider;
        this.deepSeekProvider = deepSeekProvider;
    }

    // ==================== 面试会话 ====================

    public List<InterviewSessionVO> listSessions() {
        return list(Wrappers.<InterviewSession>lambdaQuery().orderByDesc(InterviewSession::getCreatedAt))
                .stream().map(s -> {
                    InterviewSessionVO vo = new InterviewSessionVO();
                    vo.setId(s.getId());
                    vo.setTitle(s.getTitle());
                    vo.setModuleIds(s.getModuleIds());
                    vo.setDifficulty(s.getDifficulty());
                    vo.setQuestionCount(s.getQuestionCount());
                    vo.setTotalScore(s.getTotalScore());
                    vo.setStatus(s.getStatus());
                    vo.setCreatedAt(s.getCreatedAt());
                    vo.setEndedAt(s.getEndedAt());
                    return vo;
                }).collect(Collectors.toList());
    }

    @Transactional
    public InterviewSessionVO createSession(CreateSessionReq req) {
        InterviewSession session = new InterviewSession();
        session.setTitle(req.getTitle());
        session.setModuleIds(req.getModuleIds());
        session.setDifficulty(req.getDifficulty());
        session.setQuestionCount(0);
        session.setTotalScore(BigDecimal.ZERO);
        session.setStatus(0);
        save(session);

        // AI 出第一道题
        String firstQuestion = generateQuestion(session, null);
        InterviewMessage msg = new InterviewMessage();
        msg.setSessionId(session.getId());
        msg.setRole("interviewer");
        msg.setContent(firstQuestion);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);

        InterviewSessionVO vo = new InterviewSessionVO();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setModuleIds(session.getModuleIds());
        vo.setDifficulty(session.getDifficulty());
        vo.setQuestionCount(1);
        vo.setStatus(0);
        vo.setCreatedAt(session.getCreatedAt());
        return vo;
    }

    @Transactional
    public InterviewMessageVO submitAnswer(Long sessionId, SubmitAnswerReq req) {
        InterviewSession session = getById(sessionId);
        if (session == null || session.getStatus() == 1) {
            throw new BizException(400, "会话不存在或已结束");
        }

        // 保存用户回答
        InterviewMessage answerMsg = new InterviewMessage();
        answerMsg.setSessionId(sessionId);
        answerMsg.setRole("candidate");
        answerMsg.setContent(req.getAnswer());
        answerMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(answerMsg);

        // 获取对话历史
        List<InterviewMessage> history = messageMapper.selectBySession(sessionId);

        // AI 评分 + 追问
        AiResult aiResult = evaluateAndFollowUp(history, session);
        if (aiResult.score != null) {
            answerMsg.setScore(aiResult.score);
            answerMsg.setFeedback(aiResult.feedback);
            messageMapper.updateById(answerMsg);
        }

        // 更新会话统计
        session.setQuestionCount(session.getQuestionCount() + 1);
        if (session.getTotalScore() == null) {
            session.setTotalScore(BigDecimal.ZERO);
        }
        if (aiResult.score != null) {
            session.setTotalScore(session.getTotalScore().add(BigDecimal.valueOf(aiResult.score)));
        }
        updateById(session);

        InterviewMessageVO vo = new InterviewMessageVO();
        vo.setId(answerMsg.getId());
        vo.setRole("candidate");
        vo.setContent(req.getAnswer());
        vo.setScore(aiResult.score);
        vo.setFeedback(aiResult.feedback);
        vo.setCreatedAt(answerMsg.getCreatedAt());
        return vo;
    }

    @Transactional
    public void skipQuestion(Long sessionId) {
        InterviewSession session = getById(sessionId);
        if (session == null || session.getStatus() == 1) {
            throw new BizException(400, "会话不存在或已结束");
        }

        InterviewMessage skipMsg = new InterviewMessage();
        skipMsg.setSessionId(sessionId);
        skipMsg.setRole("interviewer");
        skipMsg.setContent("（用户跳过了这道题）我们来下一题。");
        skipMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(skipMsg);

        String next = generateQuestion(session, messageMapper.selectBySession(sessionId));
        InterviewMessage nextMsg = new InterviewMessage();
        nextMsg.setSessionId(sessionId);
        nextMsg.setRole("interviewer");
        nextMsg.setContent(next);
        nextMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(nextMsg);
    }

    @Transactional
    public InterviewReportVO endSession(Long sessionId) {
        InterviewSession session = getById(sessionId);
        if (session == null) {
            throw new BizException(400, "会话不存在");
        }

        session.setStatus(1);
        session.setEndedAt(LocalDateTime.now());
        updateById(session);

        List<InterviewMessage> history = messageMapper.selectBySession(sessionId);
        return generateReport(session, history);
    }

    public InterviewReportVO getReport(Long sessionId) {
        InterviewSession session = getById(sessionId);
        if (session == null) {
            throw new BizException(400, "会话不存在");
        }
        List<InterviewMessage> history = messageMapper.selectBySession(sessionId);
        return generateReport(session, history);
    }

    public List<InterviewMessageVO> getMessages(Long sessionId) {
        return messageMapper.selectBySession(sessionId).stream().map(m -> {
            InterviewMessageVO vo = new InterviewMessageVO();
            vo.setId(m.getId());
            vo.setRole(m.getRole());
            vo.setContent(m.getContent());
            vo.setQuestionId(m.getQuestionId());
            vo.setScore(m.getScore());
            vo.setFeedback(m.getFeedback());
            vo.setCreatedAt(m.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    // ==================== AI 配置 ====================

    @Transactional
    public void saveAiConfig(AiConfigReq req) {
        // 先停用所有
        List<AiConfig> all = aiConfigMapper.selectList(null);
        for (AiConfig c : all) {
            c.setIsActive(0);
        }
        for (AiConfig c : all) {
            aiConfigMapper.updateById(c);
        }

        // 保存新配置
        AiConfig config = new AiConfig();
        config.setProvider(req.getProvider());
        config.setApiKey(req.getApiKey());
        config.setModel(req.getModel());
        config.setBaseUrl(req.getBaseUrl());
        config.setIsActive(1);
        aiConfigMapper.insert(config);

        // 同步到 Provider
        configureProvider(req);
    }

    public AiConfig getActiveConfig() {
        AiConfig config = aiConfigMapper.selectActive();
        if (config != null) {
            // 脱敏 api_key
            config.setApiKey(config.getApiKey() != null && config.getApiKey().length() > 8
                    ? config.getApiKey().substring(0, 4) + "****" + config.getApiKey().substring(config.getApiKey().length() - 4)
                    : "");
        }
        return config;
    }

    private void configureProvider(AiConfigReq req) {
        switch (req.getProvider().toLowerCase()) {
            case "claude" -> claudeProvider.configure(req.getApiKey());
            case "deepseek" -> deepSeekProvider.configure(req.getApiKey(), req.getBaseUrl() != null ? req.getBaseUrl() : "https://api.deepseek.com");
            default -> openAiProvider.configure(req.getApiKey(), req.getBaseUrl() != null ? req.getBaseUrl() : "https://api.openai.com");
        }
    }

    // ==================== AI 调用 ====================

    private String generateQuestion(InterviewSession session, List<InterviewMessage> history) {
        List<Map<String, String>> messages = buildSystemPrompt(session);
        // 根据难度调整出题策略
        messages.add(Map.of("role", "user", "content", "请根据考察范围出第" + (session.getQuestionCount() + 1) + "道面试题。"));
        return callAi(messages, false);
    }

    private AiResult evaluateAndFollowUp(List<InterviewMessage> history, InterviewSession session) {
        List<Map<String, String>> messages = buildSystemPrompt(session);
        // 追加对话历史
        for (InterviewMessage m : history) {
            messages.add(Map.of("role", m.getRole().equals("interviewer") ? "assistant" : "user", "content", m.getContent()));
        }
        messages.add(Map.of("role", "user", "content", "请评分并追问。"));

        String resp = callAi(messages, false);
        return parseAiResult(resp);
    }

    private InterviewReportVO generateReport(InterviewSession session, List<InterviewMessage> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", """
            你是大厂面试复盘专家。根据以下面试记录生成报告，格式为JSON：
            {"overall":"总体评价","scores":[{"question":"题","answer":"答","score":8,"feedback":"..."}],
            "weakTopics":["薄弱点1","薄弱点2"],"suggestions":"复习建议","estimate":"评估（一面通过/二面通过/拿offer）"}
            """));

        StringBuilder sb = new StringBuilder();
        for (InterviewMessage m : history) {
            sb.append(m.getRole()).append(": ").append(m.getContent()).append("\n");
        }
        messages.add(Map.of("role", "user", "content", "面试记录：\n" + sb.toString()));

        String resp = callAi(messages, false);
        return parseReport(resp);
    }

    // ==================== Prompt 构建 ====================

    private List<Map<String, String>> buildSystemPrompt(InterviewSession session) {
        // 获取考察范围的模块名称
        String moduleNames = "";
        if (session.getModuleIds() != null && !session.getModuleIds().isEmpty()) {
            List<Module> modules = moduleMapper.selectList(
                    Wrappers.<Module>lambdaQuery().in(Module::getId,
                            Arrays.stream(session.getModuleIds().split(","))
                                    .map(Long::parseLong).collect(Collectors.toList())));
            moduleNames = modules.stream().map(Module::getName).collect(Collectors.joining("、"));
        }

        String difficultyLabel = switch (session.getDifficulty()) {
            case 1 -> "初级（基础概念）";
            case 3 -> "高级（底层原理+场景设计）";
            default -> "中级（原理+实践）";
        };

        return new ArrayList<>(List.of(Map.of("role", "system", "content", """
            你是字节跳动/阿里巴巴的Java后端面试官。面试特点：基础问得深、项目问得细、出场景题考察举一反三。

            考察范围：%s
            难度：%s

            出题规则：
            1. 从考察范围中选知识点，优先选薄弱点
            2. 每道题追问至少1次，追问要深入
            3. 如果回答模糊，要求具体化
            4. 每3题切换一次考察模块
            5. 题目难度逐步递进

            评分标准（每题10分）：
            - 准确性(4分)：回答正确，无硬伤
            - 深度(3分)：讲到原理层面，有自己的理解
            - 表达(3分)：逻辑清晰，术语准确

            评分后给出30字内的具体改进建议。
            """.formatted(moduleNames, difficultyLabel))));
    }

    // ==================== 工具方法 ====================

    private String callAi(List<Map<String, String>> messages, boolean stream) {
        AiConfig config = aiConfigMapper.selectActive();
        if (config == null) {
            throw new BizException(400, "请先配置AI");
        }

        AiProvider provider = switch (config.getProvider().toLowerCase()) {
            case "claude" -> claudeProvider;
            case "deepseek" -> deepSeekProvider;
            default -> openAiProvider;
        };

        return provider.chat(messages, config.getModel());
    }

    private AiResult parseAiResult(String resp) {
        AiResult r = new AiResult();
        try {
            // AI 返回值可能包含结构化内容，简单提取
            if (resp != null) {
                // 尝试解析评分（简单正则）
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*分").matcher(resp);
                if (m.find()) {
                    r.score = Integer.parseInt(m.group(1));
                }
                r.feedback = resp.length() > 200 ? resp.substring(0, 200) : resp;
            }
        } catch (Exception e) {
            r.score = null;
            r.feedback = "评分解析异常";
        }
        return r;
    }

    private InterviewReportVO parseReport(String resp) {
        InterviewReportVO vo = new InterviewReportVO();
        try {
            if (resp != null && resp.contains("{")) {
                String json = resp.substring(resp.indexOf("{"), resp.lastIndexOf("}") + 1);
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                om.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                @SuppressWarnings("unchecked")
                Map<String, Object> map = om.readValue(json, Map.class);
                vo.setOverall((String) map.getOrDefault("overall", ""));
                vo.setSuggestions((String) map.getOrDefault("suggestions", ""));
                vo.setEstimate((String) map.getOrDefault("estimate", ""));
                // scores
                Object scoresObj = map.get("scores");
                if (scoresObj instanceof List) {
                    List<InterviewReportVO.QuestionScore> scores = new ArrayList<>();
                    for (Object s : (List<?>) scoresObj) {
                        if (s instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> sm = (Map<String, Object>) s;
                            InterviewReportVO.QuestionScore qs = new InterviewReportVO.QuestionScore();
                            qs.setQuestion((String) sm.getOrDefault("question", ""));
                            qs.setAnswer((String) sm.getOrDefault("answer", ""));
                            qs.setScore(sm.get("score") instanceof Integer ? (Integer) sm.get("score") : 0);
                            qs.setFeedback((String) sm.getOrDefault("feedback", ""));
                            scores.add(qs);
                        }
                    }
                    vo.setScores(scores);
                }
                // weakTopics
                Object wtObj = map.get("weakTopics");
                if (wtObj instanceof List) {
                    vo.setWeakTopics(((List<?>) wtObj).stream().map(Object::toString).collect(Collectors.toList()));
                }
            }
        } catch (Exception e) {
            log.error("解析报告失败", e);
            vo.setOverall("报告生成失败");
            vo.setSuggestions("请重试");
            vo.setEstimate("无法评估")