-- ============================================================
-- 待办任务管理系统 - 数据库设计脚本
-- MySQL 8.0+ / InnoDB / utf8mb4
-- 
-- 设计依据：《待办任务管理系统需求分析文档》数据字典章节
-- 表数量：4 张核心表
-- 设计原则：
--   1. 不使用物理外键，由业务逻辑保证数据一致性
--   2. 所有时间字段使用 DATETIME，由应用层设置时区
--   3. 逻辑删除字段 is_deleted 仅用于 task 表
--   4. 索引设计覆盖核心查询路径，兼顾写入性能
-- ============================================================

-- ----------------------------
-- 创建数据库
-- ----------------------------
DROP DATABASE IF EXISTS todo_task;
CREATE DATABASE todo_task
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE todo_task;


-- ============================================================
-- 1. 用户表 (user)
-- ============================================================
-- 业务说明：
--   - 角色分为普通用户(0)和管理员(1)，管理员为预置账号，不开放注册
--   - 密码使用 BCrypt 加密存储，不可逆
--   - 头像通过文件上传接口存储到服务器，URL 存入 avatar_url
--   - status=0 表示被管理员禁用，禁用后无法登录
-- ============================================================
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID，自增主键',
    `username`    VARCHAR(50)  NOT NULL                 COMMENT '用户名，登录标识，全局唯一',
    `password`    VARCHAR(255) NOT NULL                 COMMENT '密码，BCrypt加密存储（60位哈希）',
    `avatar_url`  VARCHAR(255) DEFAULT NULL             COMMENT '头像存储路径，如 /uploads/avatar/xxx.jpg',
    `role`        TINYINT      NOT NULL DEFAULT 0       COMMENT '角色：0-普通用户，1-系统管理员',
    `status`      TINYINT      NOT NULL DEFAULT 1       COMMENT '账号状态：0-禁用，1-正常',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP    COMMENT '注册时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)               -- 用户名唯一约束，同时加速登录查询
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='用户表 - 存储注册用户信息与角色';


-- ============================================================
-- 2. 全局分类标签表 (category)
-- ============================================================
-- 业务说明：
--   - 分类由管理员统一维护，所有用户共享（公共标签库）
--   - 用户创建任务时可从分类库中选用，也可不选（category_id 置空）
--   - 删除分类前必须校验：该分类下不存在 is_deleted=0 的有效任务
--   - 分类名称全局唯一，不允许重名
--   - 颜色值为十六进制色码，用于前端视觉区分
-- ============================================================
CREATE TABLE `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT   COMMENT '分类ID，自增主键',
    `name`        VARCHAR(50) NOT NULL                  COMMENT '分类名称，全局唯一（如：学习、工作、运动）',
    `color`       VARCHAR(20) NOT NULL DEFAULT '#9E9E9E' COMMENT '分类颜色，十六进制色码（如 #FF6B6B）',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)                       -- 分类名全局唯一
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='全局分类标签表 - 管理员维护的公共标签库';


-- ============================================================
-- 3. 任务表 (task)
-- ============================================================
-- 业务说明：
--   - 核心业务表，存储用户创建的待办事项
--   - 状态流转规则（严格校验，无通用状态更新接口）：
--       待办(0) ─[开始任务]──→ 进行中(1) ─[标记完成]──→ 已完成(2)
--       已完成(2) ─[重新打开]──→ 待办(0)
--       待办(0) ─[直接完成]──→ 已完成(2)
--       待办(0)/进行中(1) ─[系统定时任务]──→ 已逾期(3)
--       已逾期(3) ─[编辑延期截止日期]──→ 待办(0)
--   - 逻辑删除：is_deleted=1 表示已删除，支持恢复
--   - 数据隔离：所有查询强制 WHERE user_id = currentUserId
--   - 分类可为空（NULL），表示"未分类"
--   - remind_offset：提醒偏移量，单位分钟
--       1440 = 截止日期前1天提醒（默认值）
--       720  = 截止日期前12小时提醒
--       0    = 不提醒
--   - Redis 提醒队列：score = due_date - remind_offset 的时间戳
-- ============================================================
CREATE TABLE `task` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '任务ID，自增主键',
    `user_id`        BIGINT       NOT NULL                 COMMENT '所属用户ID（任务创建者/所有者）',
    `title`          VARCHAR(200) NOT NULL                 COMMENT '任务标题，不超过200字',
    `description`    TEXT         DEFAULT NULL             COMMENT '任务详细描述，可选',
    `priority`       TINYINT      NOT NULL DEFAULT 1       COMMENT '优先级：0-低，1-中（默认），2-高',
    `status`         TINYINT      NOT NULL DEFAULT 0       COMMENT '状态：0-待办，1-进行中，2-已完成，3-已逾期',
    `category_id`    BIGINT       DEFAULT NULL             COMMENT '所属分类ID，NULL 表示未分类（不设物理外键）',
    `due_date`       DATETIME     DEFAULT NULL             COMMENT '截止日期，可选',
    `remind_offset`  INT          NOT NULL DEFAULT 1440    COMMENT '提醒偏移量（分钟），1440=提前1天，0=不提醒',
    `completed_time` DATETIME     DEFAULT NULL             COMMENT '任务完成时间，标记完成时自动记录',
    `is_deleted`     TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-正常，1-已删除（回收站）',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    PRIMARY KEY (`id`),

    -- ==================== 索引设计 ====================

    -- 核心查询：任务列表（按用户筛选 + 状态过滤 + 排除已删除）
    -- 覆盖 SQL: WHERE user_id = ? AND status = ? AND is_deleted = 0
    INDEX `idx_user_status` (`user_id`, `status`, `is_deleted`),

    -- 统计查询：个人任务统计（按用户 + 排除已删除）
    -- 覆盖 SQL: WHERE user_id = ? AND is_deleted = 0 GROUP BY status
    INDEX `idx_user_deleted` (`user_id`, `is_deleted`),

    -- 逾期扫描：定时任务查询已过截止日期的未完成任务
    -- 覆盖 SQL: WHERE due_date < NOW() AND status IN (0,1) AND is_deleted = 0
    INDEX `idx_due_date` (`due_date`),

    -- 分类删除校验：查询某分类下的有效任务数量
    -- 覆盖 SQL: WHERE category_id = ? AND is_deleted = 0
    INDEX `idx_category_id` (`category_id`)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='任务表 - 存储用户待办事项，核心业务表';


-- ============================================================
-- 4. 提醒记录表 (reminder_log)
-- ============================================================
-- 业务说明：
--   - 记录系统发送的站内提醒消息
--   - 由定时任务扫描 Redis 提醒队列后写入
--   - 每个任务在同一提醒周期内仅提醒一次（通过 Redis reminder:sent:{taskId} 标记）
--   - 用户可标记单条或全部已读
--   - 已提醒标记 TTL=7天，过期后自动清理
-- ============================================================
CREATE TABLE `reminder_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '记录ID，自增主键',
    `task_id`     BIGINT       NOT NULL                 COMMENT '关联的任务ID',
    `user_id`     BIGINT       NOT NULL                 COMMENT '接收提醒的用户ID',
    `message`     VARCHAR(500) NOT NULL                 COMMENT '提醒消息内容，如"任务【xxx】即将到期"',
    `is_read`     TINYINT      NOT NULL DEFAULT 0       COMMENT '已读状态：0-未读，1-已读',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提醒生成时间',

    PRIMARY KEY (`id`),

    -- 用户提醒列表查询 + 未读数量统计
    -- 覆盖 SQL: WHERE user_id = ? AND is_read = 0
    INDEX `idx_user_read` (`user_id`, `is_read`),

    -- 按任务ID查询提醒记录（防重复提醒校验）
    INDEX `idx_task_id` (`task_id`)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='提醒记录表 - 站内提醒消息存储';


-- ============================================================
-- Redis 数据结构设计（文档备忘，非SQL执行）
-- ============================================================
--
-- Key                      | Type       | 用途                     | TTL
-- -------------------------|------------|--------------------------|--------
-- task:reminder:zset       | Sorted Set | 到期提醒队列             | 无（业务维护）
--   member = taskId        |            | score = 提醒触发时间戳   |
--   score  = (due_date - remind_offset) 转毫秒时间戳
--
-- token:{userId}           | String     | JWT Token 缓存           | 24h5min
--   支持主动下线：删除 key 即可强制 Token 失效
--
-- reminder:sent:{taskId}   | String     | 已提醒标记，防重复发送   | 7天
--   值 = "1"
--
-- user:{userId}:stats      | Hash       | 用户统计缓存             | 无（主动失效）
--   fields: total, pending, inProgress, completed, overdue, completionRate
--   数据变更时主动删除 key
--
-- ============================================================


-- ============================================================
-- 初始化数据
-- ============================================================

-- 预置系统管理员账号
-- 用户名: admin  密码: admin123（BCrypt 加密）
INSERT INTO `user` (`username`, `password`, `role`, `status`)
VALUES (
    'admin',
    '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm0gYJfNRrY0O1u0T2Hi',
    1,   -- 管理员角色
    1    -- 正常状态
);

-- 预置默认分类标签
INSERT INTO `category` (`name`, `color`) VALUES
    ('学习', '#409EFF'),    -- 蓝色 - Element Plus 主色
    ('工作', '#67C23A'),    -- 绿色
    ('运动', '#E6A23C'),    -- 橙色
    ('生活', '#F56C6C'),    -- 红色
    ('其他', '#909399');    -- 灰色


-- ============================================================
-- 验证脚本（可选执行，用于检查表结构是否正确）
-- ============================================================
-- SHOW TABLES;
-- DESC user;
-- DESC category;
-- DESC task;
-- DESC reminder_log;
-- SHOW INDEX FROM task;
-- SHOW INDEX FROM reminder_log;
