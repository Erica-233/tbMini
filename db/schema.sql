-- schema.sql
-- Reddit-like Forum Database Schema for MySQL 8.x
-- Engine: InnoDB, Charset: utf8mb4, Collation: utf8mb4_0900_ai_ci

CREATE DATABASE IF NOT EXISTS tbmini
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE tbmini;

SET NAMES utf8mb4;

-- --------------------------------------------------------
-- 1) users
-- --------------------------------------------------------
DROP TABLE IF EXISTS moderation_actions;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS post_likes;
DROP TABLE IF EXISTS post_images;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS boards;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    username        VARCHAR(100) NOT NULL,
    nickname        VARCHAR(100),
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    avatar          VARCHAR(500),
    is_banned       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '0=正常, 1=封禁',
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_users_email    (email),
    UNIQUE KEY uk_users_username (username),
    KEY idx_users_role_created   (role, created_at),
    KEY idx_users_banned         (is_banned)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='用户表：USER/ADMIN';

-- --------------------------------------------------------
-- 2) boards
-- --------------------------------------------------------
CREATE TABLE boards (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '展示名称',
    slug        VARCHAR(100) NOT NULL COMMENT 'URL标识',
    description VARCHAR(500),
    is_active   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=启用',
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_boards_slug      (slug),
    KEY idx_boards_active_sort     (is_active, sort_order),
    KEY idx_boards_created         (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='板块（看板）表';

-- --------------------------------------------------------
-- 3) posts
-- --------------------------------------------------------
CREATE TABLE posts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id      BIGINT       NOT NULL COMMENT '所属板块',
    user_id       BIGINT       NOT NULL COMMENT '作者',
    title         VARCHAR(255) NOT NULL,
    content       TEXT COMMENT 'HTML/富文本内容',
    body_md       TEXT COMMENT '原始Markdown（可选）',
    image_url     VARCHAR(500),
    status        VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED'
                                COMMENT 'PUBLISHED/PENDING_REVIEW/REMOVED/LOCKED',
    like_count    INT          NOT NULL DEFAULT 0,
    comment_count INT          NOT NULL DEFAULT 0,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    KEY idx_posts_board_status_created (board_id, status, created_at),
    KEY idx_posts_user_created         (user_id, created_at),
    KEY idx_posts_status_created       (status, created_at),
    KEY idx_posts_created              (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='帖子表';

-- --------------------------------------------------------
-- 4) post_images
-- --------------------------------------------------------
CREATE TABLE post_images (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT       NOT NULL COMMENT '所属帖子',
    image_url   VARCHAR(500) NOT NULL COMMENT '图片URL',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    KEY idx_post_images_post_sort (post_id, sort_order),
    KEY idx_post_images_created   (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='帖子图片表（支持多图）';

-- --------------------------------------------------------
-- 5) comments（两层：根评论 + 回复根评论）
-- --------------------------------------------------------
CREATE TABLE comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT       NOT NULL COMMENT '所属帖子',
    user_id     BIGINT       NOT NULL COMMENT '作者',
    parent_id   BIGINT       NULL     COMMENT 'NULL=根评论；非NULL必须指向同一post下的根评论id',
    content     TEXT         NOT NULL,
    body_md     TEXT COMMENT '原始Markdown（可选）',
    status      VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED'
                              COMMENT 'PUBLISHED/PENDING_REVIEW/REMOVED',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    KEY idx_comments_post_status_created (post_id, status, created_at),
    KEY idx_comments_parent_created      (parent_id, created_at),
    KEY idx_comments_user_created        (user_id, created_at),
    KEY idx_comments_status_created      (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='评论表（两层结构）';

-- --------------------------------------------------------
-- 6) post_likes（幂等：同一用户对同一帖子只能点赞一次）
-- --------------------------------------------------------
CREATE TABLE post_likes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_post_likes_user_post (user_id, post_id),
    KEY idx_post_likes_post            (post_id),
    KEY idx_post_likes_created         (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='帖子点赞表';

-- --------------------------------------------------------
-- 7) reports
--    业务规则：同一用户对同一 target 只能有一条 OPEN 举报。
--    引入 is_open TINYINT(1)：OPEN=1，CLOSED=0。
--    关闭举报时 is_open 设为 0，允许历史存在多条 CLOSED 记录。
-- --------------------------------------------------------
CREATE TABLE reports (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporter_id BIGINT       NOT NULL COMMENT '举报人',
    target_type VARCHAR(20)  NOT NULL COMMENT 'POST / COMMENT',
    target_id   BIGINT       NOT NULL COMMENT '被举报对象ID',
    reason_code VARCHAR(100) NOT NULL COMMENT '举报原因编码（如 SPAM, HARASSMENT）',
    reason_text TEXT COMMENT '补充说明',
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN / RESOLVED',
    is_open     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '1=OPEN, 0=CLOSED；用于唯一约束',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    resolved_at DATETIME(3)  NULL COMMENT '处理/关闭时间',

    UNIQUE KEY uk_reports_user_target_open (reporter_id, target_type, target_id, is_open),
    KEY idx_reports_target_status_created  (target_type, target_id, status, created_at),
    KEY idx_reports_status_created         (status, created_at),
    KEY idx_reports_reporter_created       (reporter_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='举报表';

-- --------------------------------------------------------
-- 8) moderation_actions（管理审计）
-- --------------------------------------------------------
CREATE TABLE moderation_actions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    moderator_id BIGINT      NULL COMMENT '操作管理员；NULL 表示系统/自动行为',
    target_type VARCHAR(20)  NOT NULL COMMENT 'POST / COMMENT / USER',
    target_id   BIGINT       NOT NULL COMMENT '被操作对象ID',
    action      VARCHAR(50)  NOT NULL
                              COMMENT 'AUTO_FLAGGED, APPROVE, REMOVE, BAN_USER, UNBAN_USER, ...',
    reason      TEXT COMMENT '操作原因/备注',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    KEY idx_mod_actions_target_created (target_type, target_id, created_at),
    KEY idx_mod_actions_moderator_created (moderator_id, created_at),
    KEY idx_mod_actions_action_created (action, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='管理操作审计表';
