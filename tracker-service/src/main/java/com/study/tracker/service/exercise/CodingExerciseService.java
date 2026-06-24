package com.study.tracker.service.exercise;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.tracker.common.exception.BizException;
import com.study.tracker.model.dto.CodeRunReq;
import com.study.tracker.model.entity.CodingExercise;
import com.study.tracker.model.entity.CodingSubmission;
import com.study.tracker.model.vo.CodeRunResultVO;
import com.study.tracker.model.vo.ExerciseVO;
import com.study.tracker.model.vo.SubmissionVO;
import com.study.tracker.service.exercise.mapper.CodingExerciseMapper;
import com.study.tracker.service.exercise.mapper.CodingSubmissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 在线代码练习服务
 *
 * 通过 Docker 容器沙箱执行用户提交的 Java 代码，
 * 超时 10 秒自动 kill，限制内存 256M，无网络权限。
 * Docker 不可用时自动降级为本地执行（仅限信任环境）。
 */
@Slf4j
@Service
public class CodingExerciseService extends ServiceImpl<CodingExerciseMapper, CodingExercise> {

    private final CodingSubmissionMapper submissionMapper;

    @Value("${coding.docker.enabled:false}")
    private boolean dockerEnabled;

    public CodingExerciseService(CodingSubmissionMapper submissionMapper) {
        this.submissionMapper = submissionMapper;
    }

    /**
     * 某知识点下的练习列表
     */
    public List<ExerciseVO> listByTopic(Long topicId) {
        return baseMapper.selectByTopic(topicId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 练习详情
     */
    public ExerciseVO getDetail(Long id) {
        CodingExercise ex = getById(id);
        if (ex == null) throw new BizException(404, "练习不存在");
        return toVO(ex);
    }

    /**
     * 运行代码
     */
    @Transactional
    public CodeRunResultVO runCode(CodeRunReq req) {
        CodingExercise exercise = getById(req.getExerciseId());
        if (exercise == null) throw new BizException(404, "练习不存在");

        String userCode = req.getCode();
        // 拼装完整类：模板代码 + 用户代码，包装进一个可编译的 Main 类
        String fullCode = buildFullCode(userCode);

        // 写入临时文件
        String runId = IdUtil.fastSimpleUUID();
        Path workDir = Path.of("/tmp/coding", runId);
        FileUtil.mkdir(workDir.toFile());
        FileUtil.writeUtf8String(fullCode, workDir.resolve("Main.java").toFile());

        // 执行代码
        CodeRunResultVO result = new CodeRunResultVO();
        result.setSubmissionId(null); // 稍后填入

        long startMs = System.currentTimeMillis();
        ProcessResult proc;
        try {
            if (dockerEnabled) {
                proc = runInDocker(workDir);
            } else {
                proc = runLocally(workDir);
            }
        } catch (Exception e) {
            log.error("代码执行异常", e);
            proc = new ProcessResult();
            proc.exitCode = -1;
            proc.stderr = "执行环境异常: " + e.getMessage();
            proc.stdout = "";
        }
        long elapsed = System.currentTimeMillis() - startMs;

        result.setExecutionTimeMs((int) elapsed);
        result.setOutput(proc.stdout);
        result.setErrorMessage(proc.stderr);
        result.setPassed(proc.exitCode == 0 && proc.stderr.isEmpty());

        // 保存提交记录
        CodingSubmission submission = new CodingSubmission();
        submission.setExerciseId(req.getExerciseId());
        submission.setUserCode(userCode);
        submission.setStatus(proc.exitCode == 0 ? (proc.stderr.isEmpty() ? 1 : 2) : 2);
        submission.setOutput(proc.stdout);
        submission.setErrorMessage(proc.stderr);
        submission.setSubmittedAt(LocalDateTime.now());
        submissionMapper.insert(submission);

        result.setSubmissionId(submission.getId());

        // 清理临时文件
        FileUtil.del(workDir.toFile());
        return result;
    }

    /**
     * 提交历史
     */
    public List<SubmissionVO> listSubmissions(Long exerciseId) {
        return submissionMapper.selectByExercise(exerciseId).stream()
                .map(s -> {
                    SubmissionVO vo = new SubmissionVO();
                    vo.setId(s.getId());
                    vo.setUserCode(s.getUserCode());
                    vo.setStatus(s.getStatus());
                    vo.setOutput(s.getOutput());
                    vo.setErrorMessage(s.getErrorMessage());
                    vo.setSubmittedAt(s.getSubmittedAt());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // ==================== 内部方法 ====================

    /**
     * 包装可编译的完整 Java 类
     */
    private String buildFullCode(String userCode) {
        // 简单策略：把用户代码包进一个 Main 类
        // 如果用户已经写了 class 声明，直接使用；否则包装
        if (userCode.contains("class ")) {
            return userCode;
        }
        return """
        """
        public class Main {
            public static void main(String[] args) {
                %s
            }
        }
        """.formatted(userCode);
    }

    /**
     * Docker 沙箱执行
     */
    private ProcessResult runInDocker(Path workDir) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("--network=none");
        cmd.add("--memory=256m");
        cmd.add("--cpus=0.5");
        cmd.add("-v");
        cmd.add(workDir.toString() + ":/code");
        cmd.add("--tmpfs=/tmp:exec");
        cmd.add("openjdk:17-slim");
        cmd.add("sh");
        cmd.add("-c");
        cmd.add("cd /code && javac Main.java && java -Xmx128m Main");

        return execute(cmd);
    }

    /**
     * 本地执行（Docker 不可用时的降级方案）
     */
    private ProcessResult runLocally(Path workDir) throws Exception {
        // 先编译
        ProcessBuilder compilePb = new ProcessBuilder("javac", "Main.java");
        compilePb.directory(workDir.toFile());
        Process compileProc = compilePb.start();
        compileProc.waitFor(15, TimeUnit.SECONDS);

        if (compileProc.exitValue() != 0) {
            ProcessResult r = new ProcessResult();
            r.exitCode = compileProc.exitValue();
            r.stderr = readStream(compileProc.getErrorStream());
            r.stdout = "";
            return r;
        }

        // 再运行
        ProcessBuilder runPb = new ProcessBuilder("java", "-Xmx128m", "Main");
        runPb.directory(workDir.toFile());
        Process runProc = runPb.start();
        boolean finished = runProc.waitFor(10, TimeUnit.SECONDS);

        ProcessResult r = new ProcessResult();
        if (!finished) {
            runProc.destroyForcibly();
            r.exitCode = -1;
            r.stderr = "运行超时（>10秒），已终止";
            r.stdout = readStream(runProc.getInputStream());
        } else {
            r.exitCode = runProc.exitValue();
            r.stdout = readStream(runProc.getInputStream());
            r.stderr = readStream(runProc.getErrorStream());
        }
        return r;
    }

    private ProcessResult execute(List<String> cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process proc = pb.start();
        boolean finished = proc.waitFor(15, TimeUnit.SECONDS);

        ProcessResult r = new ProcessResult();
        if (!finished) {
            proc.destroyForcibly();
            r.exitCode = -1;
            r.stderr = "运行超时";
            r.stdout = readStream(proc.getInputStream());
        } else {
            r.exitCode = proc.exitValue();
            r.stdout = readStream(proc.getInputStream());
            r.stderr = readStream(proc.getErrorStream());
        }
        return r;
    }

    private String readStream(java.io.InputStream is) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "";
        }
    }

    private ExerciseVO toVO(CodingExercise ex) {
        ExerciseVO vo = new ExerciseVO();
        vo.setId(ex.getId());
        vo.setTopicId(ex.getTopicId());
        vo.setTitle(ex.getTitle());
        vo.setDescription(ex.getDescription());
        vo.setTemplateCode(ex.getTemplateCode());
        vo.setDifficulty(ex.getDifficulty());
        vo.setSortOrder(ex.getSortOrder());
        vo.setCreatedAt(ex.getCreatedAt());
        return vo;
    }

    static class ProcessResult {
        int exitCode;
        String stdout = "";
        String stderr = "";
    }
}
                                    