-- seed.sql
-- Reddit-like Forum Initial Data
-- 所有密码均使用 BCrypt 编码（明文见注释）

USE tbmini;

SET NAMES utf8mb4;

-- --------------------------------------------------------
-- 1) Users
--    密码明文：Admin123! / User123!
--    BCrypt hash 由 Spring Security 的 BCryptPasswordEncoder 生成（strength=10）
-- --------------------------------------------------------
INSERT INTO users (email, password, username, nickname, role, is_banned, created_at, updated_at)
VALUES
('admin@example.com',
 '$2a$10$RsSNkKEhIN952WsHGZ7jqOdI6j2htQdZ4ApwYARpWVT4oRQKvKdNK',
 'admin', 'Admin', 'ADMIN', 0, NOW(3), NOW(3));

INSERT INTO users (email, password, username, nickname, role, is_banned, created_at, updated_at)
VALUES
('alice@example.com',
 '$2a$10$uhf3AtWyKFsuBEzmV2hiP.R9B011YUEwuqUQ6tWIOQaFrW92gzt5a',
 'alice', 'Alice', 'USER', 0, NOW(3), NOW(3)),

('bob@example.com',
 '$2a$10$uhf3AtWyKFsuBEzmV2hiP.R9B011YUEwuqUQ6tWIOQaFrW92gzt5a',
 'bob', 'Bob', 'USER', 0, DATE_ADD(NOW(3), INTERVAL 1 SECOND), DATE_ADD(NOW(3), INTERVAL 1 SECOND)),

('carol@example.com',
 '$2a$10$uhf3AtWyKFsuBEzmV2hiP.R9B011YUEwuqUQ6tWIOQaFrW92gzt5a',
 'carol', 'Carol', 'USER', 0, DATE_ADD(NOW(3), INTERVAL 2 SECOND), DATE_ADD(NOW(3), INTERVAL 2 SECOND));

-- --------------------------------------------------------
-- 2) Boards
-- --------------------------------------------------------
INSERT INTO boards (name, slug, description, is_active, sort_order, created_at, updated_at)
VALUES
('General', 'general', 'General discussion for everything.', 1, 0, NOW(3), NOW(3)),

('Pictures', 'pictures', 'Share and discuss photos and images.', 1, 1, DATE_ADD(NOW(3), INTERVAL 1 SECOND), DATE_ADD(NOW(3), INTERVAL 1 SECOND)),

('Technology', 'technology', 'Tech news, programming, and gadgets.', 1, 2, DATE_ADD(NOW(3), INTERVAL 2 SECOND), DATE_ADD(NOW(3), INTERVAL 2 SECOND));

-- --------------------------------------------------------
-- 3) Sample Posts (with image_url for list display)
-- --------------------------------------------------------
INSERT INTO posts (board_id, user_id, title, content, body_md, image_url, status, like_count, comment_count, created_at, updated_at)
VALUES
(1, 2,
 'Welcome to the General board!',
 '<p>Feel free to introduce yourself here.</p>',
 'Feel free to introduce yourself here.',
 NULL,
 'PUBLISHED', 0, 2, NOW(3), NOW(3)),

(2, 3,
 'Sunset photography tips',
 'Share your best sunset shots and editing tips!',
 'Share your best sunset shots and editing tips!',
 'https://picsum.photos/seed/sunset/800/600.jpg',
 'PUBLISHED', 1, 1, DATE_ADD(NOW(3), INTERVAL 1 SECOND), DATE_ADD(NOW(3), INTERVAL 1 SECOND)),

(2, 2,
 'Beautiful mountain landscape',
 'Check out this amazing view!',
 'Check out this amazing view!',
 'https://picsum.photos/seed/mountain/800/600.jpg',
 'PUBLISHED', 0, 0, DATE_ADD(NOW(3), INTERVAL 2 SECOND), DATE_ADD(NOW(3), INTERVAL 2 SECOND)),

(1, 4,
 'What are you working on this weekend?',
 '<p>Just curious about side projects.</p>',
 'Just curious about side projects.',
 NULL,
 'PUBLISHED', 0, 0, DATE_ADD(NOW(3), INTERVAL 3 SECOND), DATE_ADD(NOW(3), INTERVAL 3 SECOND));

-- --------------------------------------------------------
-- 4) Sample Post Images (for detail page gallery)
-- --------------------------------------------------------
INSERT INTO post_images (post_id, image_url, sort_order, created_at)
VALUES
(2, 'https://picsum.photos/seed/sunset/800/600.jpg', 0, NOW(3)),
(3, 'https://picsum.photos/seed/mountain/800/600.jpg', 0, NOW(3));

-- --------------------------------------------------------
-- 5) Sample Comments（两层结构）
-- --------------------------------------------------------
-- post_id=1 的根评论
INSERT INTO comments (post_id, user_id, parent_id, content, body_md, status, created_at, updated_at)
VALUES
(1, 3, NULL, 'Hello everyone! Glad to be here.', 'Hello everyone! Glad to be here.', 'PUBLISHED', NOW(3), NOW(3)),

(1, 4, NULL, 'Nice to meet you all.', 'Nice to meet you all.', 'PUBLISHED', DATE_ADD(NOW(3), INTERVAL 1 SECOND), DATE_ADD(NOW(3), INTERVAL 1 SECOND));

-- post_id=1 的回复（指向第一条根评论）
INSERT INTO comments (post_id, user_id, parent_id, content, body_md, status, created_at, updated_at)
VALUES
(1, 2, 1, 'Welcome Alice!', 'Welcome Alice!', 'PUBLISHED', DATE_ADD(NOW(3), INTERVAL 2 SECOND), DATE_ADD(NOW(3), INTERVAL 2 SECOND));

-- post_id=2 的根评论
INSERT INTO comments (post_id, user_id, parent_id, content, body_md, status, created_at, updated_at)
VALUES
(2, 2, NULL, 'Great topic, looking forward to examples.', 'Great topic, looking forward to examples.', 'PUBLISHED', DATE_ADD(NOW(3), INTERVAL 3 SECOND), DATE_ADD(NOW(3), INTERVAL 3 SECOND));

-- --------------------------------------------------------
-- 6) Sample Post Likes
-- --------------------------------------------------------
INSERT INTO post_likes (post_id, user_id, created_at)
VALUES
(2, 2, NOW(3));

-- --------------------------------------------------------
-- 7) Sample Reports
-- --------------------------------------------------------
INSERT INTO reports (reporter_id, target_type, target_id, reason_code, reason_text, status, is_open, created_at)
VALUES
(3, 'POST', 3, 'SPAM', 'Looks like off-topic promotion.', 'OPEN', 1, NOW(3));

-- --------------------------------------------------------
-- 8) Sample Moderation Actions
-- --------------------------------------------------------
INSERT INTO moderation_actions (moderator_id, target_type, target_id, action, reason, created_at)
VALUES
(NULL, 'POST', 3, 'AUTO_FLAGGED', 'Report count reached threshold.', NOW(3));
