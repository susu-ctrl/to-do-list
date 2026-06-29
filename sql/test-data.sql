-- ============================================================
-- 测试数据脚本 - 仅用于开发/调试环境
-- 执行前请先运行 schema.sql
-- ============================================================

USE todo_task;

-- ============================================================
-- 测试用户（密码均为 123456，BCrypt 加密）
-- ============================================================
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
    ('zhangsan', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm0gYJfNRrY0O1u0T2Hi', 0, 1),
    ('lisi',     '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm0gYJfNRrY0O1u0T2Hi', 0, 1),
    ('wangwu',   '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm0gYJfNRrY0O1u0T2Hi', 0, 1),
    ('disabled', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm0gYJfNRrY0O1u0T2Hi', 0, 0);  -- 被禁用用户

-- ============================================================
-- 测试任务数据（zhangsan 的任务）
-- ============================================================
-- 说明：
--   user_id=2 为 zhangsan（admin 为 id=1）
--   category_id=1 学习, 2 工作, 3 运动, 4 生活, 5 其他
-- ============================================================

-- 待办任务
INSERT INTO `task` (`user_id`, `title`, `description`, `priority`, `status`, `category_id`, `due_date`, `remind_offset`) VALUES
    (2, '完成数据库课程设计',  '包括需求分析、ER图、DDL脚本', 2, 0, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), 1440),
    (2, '复习Java期末考试',   '重点：集合框架、多线程、IO',   2, 0, 1, DATE_ADD(NOW(), INTERVAL 5 DAY), 720),
    (2, '买菜做饭',          '西红柿、鸡蛋、青菜',          1, 0, 4, DATE_ADD(NOW(), INTERVAL 1 DAY), 60),
    (2, '整理书桌',          NULL,                          0, 0, NULL, NULL, 0);

-- 进行中任务
INSERT INTO `task` (`user_id`, `title`, `description`, `priority`, `status`, `category_id`, `due_date`, `remind_offset`) VALUES
    (2, '开发待办管理系统后端', 'Spring Boot + MyBatis + Redis', 2, 1, 2, DATE_ADD(NOW(), INTERVAL 7 DAY), 1440),
    (2, '准备小组汇报PPT',    '主题：企业级应用架构设计',     1, 1, 2, DATE_ADD(NOW(), INTERVAL 2 DAY), 1440);

-- 已完成任务
INSERT INTO `task` (`user_id`, `title`, `description`, `priority`, `status`, `category_id`, `due_date`, `remind_offset`, `completed_time`) VALUES
    (2, '提交操作系统作业',   '第五章课后习题',              1, 2, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 1440, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (2, '晨跑5公里',         NULL,                          0, 2, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), 0, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 已逾期任务
INSERT INTO `task` (`user_id`, `title`, `description`, `priority`, `status`, `category_id`, `due_date`, `remind_offset`) VALUES
    (2, '缴纳网费',          '本月宽带费用',                1, 3, 4, DATE_SUB(NOW(), INTERVAL 2 DAY), 1440);

-- 逻辑删除任务（回收站）
INSERT INTO `task` (`user_id`, `title`, `description`, `priority`, `status`, `category_id`, `due_date`, `remind_offset`, `is_deleted`) VALUES
    (2, '旧任务已废弃',      '这个任务不再需要了',          0, 0, NULL, NULL, 0, 1);

-- lisi 的任务（用于验证数据隔离）
INSERT INTO `task` (`user_id`, `title`, `description`, `priority`, `status`, `category_id`, `due_date`, `remind_offset`) VALUES
    (3, '阅读《Spring实战》', '第6-10章',                   1, 0, 1, DATE_ADD(NOW(), INTERVAL 10 DAY), 1440),
    (3, '写周报',            '本周工作总结与下周计划',       1, 1, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 60);

-- ============================================================
-- 测试提醒记录
-- ============================================================
INSERT INTO `reminder_log` (`task_id`, `user_id`, `message`, `is_read`) VALUES
    (9, 2, '任务【缴纳网费】即将到期，请及时处理！', 1),   -- 已读
    (5, 2, '任务【开发待办管理系统后端】即将到期，请及时处理！', 0);  -- 未读


-- ============================================================
-- 数据统计验证查询（可选）
-- ============================================================

-- 查看各用户任务分布
-- SELECT u.username, t.status, COUNT(*) as cnt
-- FROM task t JOIN user u ON t.user_id = u.id
-- WHERE t.is_deleted = 0
-- GROUP BY u.username, t.status;

-- 查看分类使用统计
-- SELECT c.name, COUNT(t.id) as task_count
-- FROM category c LEFT JOIN task t ON c.id = t.category_id AND t.is_deleted = 0
-- GROUP BY c.id, c.name;

-- 查看未读提醒
-- SELECT u.username, COUNT(*) as unread_count
-- FROM reminder_log r JOIN user u ON r.user_id = u.id
-- WHERE r.is_read = 0
-- GROUP BY u.username;
