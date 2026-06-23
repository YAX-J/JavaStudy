-- ============================================
-- Study Tracker 建表脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS study_tracker
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE study_tracker;

-- 知识模块
CREATE TABLE IF NOT EXISTS module (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 知识点
CREATE TABLE IF NOT EXISTS topic (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id            BIGINT       NOT NULL,
    title                VARCHAR(100) NOT NULL,
    difficulty           TINYINT      NOT NULL DEFAULT 1 COMMENT '难度 1-3',
    priority             TINYINT      NOT NULL DEFAULT 1 COMMENT 'P0/P1/P2',
    status               TINYINT      NOT NULL DEFAULT 0 COMMENT '0未开始 1进行中 2已掌握',
    mastery_level        TINYINT      NOT NULL DEFAULT 0 COMMENT '掌握程度 1-5',
    status_change_count  INT          NOT NULL DEFAULT 0 COMMENT '状态变更次数',
    last_review_at       DATETIME     NULL,
    next_review_at       DATETIME     NULL,
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_module (module_id),
    INDEX idx_status (status),
    INDEX idx_next_review (next_review_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 学习笔记
CREATE TABLE IF NOT EXISTS note (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id   BIGINT       NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_topic (topic_id),
    FULLTEXT INDEX ft_note (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 学习计划
CREATE TABLE IF NOT EXISTS plan (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    target_date   DATE         NULL,
    daily_minutes INT          NOT NULL DEFAULT 60,
    status        TINYINT      NOT NULL DEFAULT 0 COMMENT '0进行中 1已完成 2已放弃',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 计划-知识点关联
CREATE TABLE IF NOT EXISTS plan_item (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id    BIGINT NOT NULL,
    topic_id   BIGINT NOT NULL,
    day_number INT    NOT NULL DEFAULT 1 COMMENT '第几天学',
    INDEX idx_plan (plan_id),
    INDEX idx_topic (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 打卡记录
CREATE TABLE IF NOT EXISTS check_in (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id          BIGINT   NOT NULL,
    topic_id         BIGINT   NOT NULL,
    check_date       DATE     NOT NULL,
    duration_minutes INT      NOT NULL DEFAULT 0,
    feeling          TINYINT  NULL COMMENT '1-5',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date_topic (check_date, topic_id),
    INDEX idx_plan_date (plan_id, check_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 编程练习
CREATE TABLE IF NOT EXISTS coding_exercise (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic_id      BIGINT       NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   TEXT         NULL,
    template_code TEXT         NULL,
    test_code     TEXT         NULL,
    difficulty    TINYINT      NOT NULL DEFAULT 1,
    sort_order    INT          NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_topic (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 代码提交记录
CREATE TABLE IF NOT EXISTS coding_submission (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    exercise_id    BIGINT   NOT NULL,
    user_code      TEXT     NULL,
    status         TINYINT  NOT NULL DEFAULT 0 COMMENT '0未通过 1通过 2运行错误 3超时',
    output         TEXT     NULL,
    error_message  TEXT     NULL,
    submitted_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exercise (exercise_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 面试会话
CREATE TABLE IF NOT EXISTS interview_session (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(200)   NOT NULL,
    module_ids     VARCHAR(200)   NULL COMMENT '考察模块',
    difficulty     TINYINT        NOT NULL DEFAULT 1,
    question_count INT            NOT NULL DEFAULT 0,
    total_score    DECIMAL(5,2)   NULL,
    status         TINYINT        NOT NULL DEFAULT 0 COMMENT '0进行中 1已结束',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at       DATETIME       NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 面试对话记录
CREATE TABLE IF NOT EXISTS interview_message (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  BIGINT       NOT NULL,
    role        VARCHAR(10)  NOT NULL COMMENT 'interviewer/candidate',
    content     TEXT         NULL,
    question_id BIGINT       NULL,
    score       TINYINT      NULL COMMENT '1-10',
    feedback    TEXT         NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- AI 配置
CREATE TABLE IF NOT EXISTS ai_config (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider   VARCHAR(20)  NOT NULL,
    api_key    VARCHAR(200) NOT NULL,
    model      VARCHAR(50)  NOT NULL,
    base_url   VARCHAR(300) NULL,
    is_active  TINYINT      NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
