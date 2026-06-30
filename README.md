# 待办任务管理系统

> 一个基于 Spring Boot + Vue3 的轻量级个人待办任务管理系统，提供任务全生命周期管理、智能到期提醒、逾期自动标记、分类标签与数据看板等功能。
>
> 重庆科技大学 · 企业级应用开发课程设计

---

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [默认账号](#默认账号)
- [API 接口概览](#api-接口概览)
- [核心设计说明](#核心设计说明)
- [数据库设计](#数据库设计)
- [配置说明](#配置说明)
- [注意事项](#注意事项)

---

## 功能特性

### 用户与权限

- 用户注册 / 登录 / 退出，JWT 身份认证
- 密码 BCrypt 加密存储，Token 缓存于 Redis 支持主动下线
- 基于 RBAC 的角色控制：普通用户（`role=0`）、系统管理员（`role=1`）
- 个人信息维护：修改用户名、修改密码、头像上传
- 管理员可启用/禁用账号、重置用户密码

### 任务管理

- 任务全生命周期管理：创建、编辑、详情、逻辑删除、恢复
- 严格状态机流转，非法操作返回 400
- 分页列表查询，支持按状态筛选
- 优先级（低/中/高）、分类标签（可选）、截止日期、提醒偏移
- 数据隔离：所有任务操作强制按 `user_id` 过滤，防止越权

### 分类标签

- 管理员统一维护全局分类标签库（增删改查）
- 分类名称全局唯一，支持自定义颜色
- 删除分类前校验关联任务，存在有效任务时拒绝删除（409）

### 智能提醒

- 基于 Redis Sorted Set 的延迟提醒队列
- 定时扫描到期任务，生成站内提醒消息
- 已提醒标记防重复（TTL 7 天）
- 提醒列表查看、未读计数、单条/全部标记已读

### 逾期标记

- 定时扫描已过截止日期且未完成的任务，自动标记为逾期
- 逾期任务编辑截止日期后自动回退为待办状态

### 数据统计

- 个人任务概览：各状态数量、完成率
- 个人效率分析：平均完成时长、按时完成率、逾期率
- 完成趋势：按周/月统计已完成任务数
- 管理员平台统计：用户总数、任务总数、整体完成率等
- ECharts 可视化图表展示

---

## 技术栈

| 层次 | 技术 | 版本 |
| --- | --- | --- |
| 后端框架 | Spring Boot | 3.5.15 |
| ORM | MyBatis | mybatis-spring-boot-starter 3.0.5 |
| 数据库 | MySQL | 8.0+ (InnoDB / utf8mb4) |
| 缓存/队列 | Redis | 6.0+ |
| 认证 | JWT (jjwt) | 0.12.6 |
| 密码加密 | Spring Security Crypto (BCrypt) | - |
| JDK | OpenJDK | 17+ |
| 构建工具 | Maven | - |
| 前端框架 | Vue 3 | 3.5+ |
| 构建工具 | Vite | 8.0+ |
| UI 组件库 | Element Plus | 2.14+ |
| 状态管理 | Pinia | 3.0+ |
| HTTP 客户端 | Axios | 1.18+ |
| 图表库 | ECharts | 6.1+ |
| 路由 | Vue Router | 5.1+ |

---

## 项目结构

```
to-do-list/
├── todo-system/                        # 后端 Spring Boot 项目
│   ├── src/main/java/com/org/todo/
│   │   ├── TodoSystemApplication.java  # 启动类
│   │   ├── config/                     # 配置类（Redis、Web、全局异常）
│   │   ├── controller/                 # 控制器（7 个）
│   │   ├── dto/                        # 数据传输对象
│   │   ├── entity/                     # 实体类
│   │   ├── enums/                      # 枚举（状态/优先级/角色）
│   │   ├── interceptor/                # JWT 拦截器
│   │   ├── mapper/                     # MyBatis Mapper 接口
│   │   ├── service/                    # 业务接口与实现
│   │   └── utils/                      # 工具类（JWT、Redis、密码）
│   ├── src/main/resources/
│   │   └── application.yml             # 后端配置
│   ├── uploads/avatar/                 # 头像上传目录
│   └── pom.xml
│
├── todo-frontend/                      # 前端 Vue3 项目
│   ├── src/
│   │   ├── api/                        # API 请求封装
│   │   ├── components/                 # 公共组件
│   │   ├── router/                     # 路由配置
│   │   ├── stores/                     # Pinia 状态管理
│   │   ├── utils/                      # Axios 请求实例
│   │   ├── views/                      # 页面视图（7 个）
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
├── sql/
│   ├── schema.sql                      # 建库建表脚本（含初始数据）
│   └── test-data.sql                   # 测试数据脚本
├── plan.md                             # 完整设计文档
└── README.md                           # 本文件
```

### 后端分层架构

```
Controller（接口层）
    ↓
Service / ServiceImpl（业务层）
    ↓
Mapper（数据访问层，MyBatis）
    ↓
MySQL / Redis（存储层）
```

### 前端页面

| 路由 | 页面 | 说明 |
| --- | --- | --- |
| `/login` | 登录页 | 注册 / 登录 |
| `/` | 首页 | 任务概览看板 |
| `/tasks` | 任务管理 | 任务列表、创建、编辑、状态流转、回收站 |
| `/categories` | 分类管理 | 分类标签维护（管理员可编辑） |
| `/stats` | 数据统计 | 个人效率分析、完成趋势图表 |
| `/reminders` | 提醒中心 | 站内提醒消息列表 |
| `/profile` | 个人中心 | 修改用户名、密码、头像 |

---

## 快速开始

### 环境要求

- **JDK** 17+
- **Maven** 3.6+
- **MySQL** 8.0+
- **Redis** 6.0+
- **Node.js** 22.18+（或 24.12+）
- **npm**（随 Node.js 安装）

### 1. 初始化数据库

启动 MySQL，执行建表脚本（会自动创建 `todo_task` 数据库并写入初始管理员与默认分类）：

```bash
mysql -u root -p < sql/schema.sql
```

如需测试数据，再执行：

```bash
mysql -u root -p < sql/test-data.sql
```

### 2. 启动 Redis

```bash
redis-server
```

确认 Redis 运行在 `127.0.0.1:6379`（默认配置）。

### 3. 启动后端

修改 `todo-system/src/main/resources/application.yml` 中的数据库连接信息（用户名、密码、数据库名），使其与本地环境一致。

```bash
cd todo-system
./mvnw spring-boot:run
```

后端启动后监听 **http://localhost:8080**。

### 4. 启动前端

```bash
cd todo-frontend
npm install
npm run dev
```

前端启动后访问 **http://localhost:5173**（Vite 默认端口），使用浏览器打开即可。

### 5. 构建生产版本（可选）

```bash
cd todo-frontend
npm run build      # 产物输出到 dist/
```

将 `dist/` 目录部署到 Nginx 或其他静态服务器即可。

---

## 默认账号

| 用户名 | 密码 | 角色 | 说明 |
| --- | --- | --- | --- |
| `admin` | `admin123` | 管理员 | 由 `schema.sql` 预置 |
| `zhangsan` | `123456` | 普通用户 | 由 `test-data.sql` 创建，附带测试任务 |
| `lisi` | `123456` | 普通用户 | 由 `test-data.sql` 创建，用于验证数据隔离 |
| `wangwu` | `123456` | 普通用户 | 由 `test-data.sql` 创建 |
| `disabled` | `123456` | 普通用户 | 已禁用账号，用于测试登录拦截 |

> 仅执行 `schema.sql` 时只有 `admin` 账号；测试用户来自 `test-data.sql`。

---

## API 接口概览

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

除公开接口外，请求头需携带 `Authorization: Bearer {token}`。

### 认证模块 `/api/auth`

| 方法 | 路径 | 功能 | 权限 |
| --- | --- | --- | --- |
| POST | `/register` | 用户注册 | 公开 |
| GET | `/check-username?username=` | 检查用户名是否可用 | 公开 |
| POST | `/login` | 用户登录 | 公开 |
| GET | `/info` | 获取当前用户信息 | 登录 |
| PUT | `/password` | 修改密码（需旧密码验证） | 登录 |
| POST | `/logout` | 退出登录 | 登录 |

### 用户模块 `/api/users`

| 方法 | 路径 | 功能 | 权限 |
| --- | --- | --- | --- |
| PUT | `/username` | 修改用户名 | 登录 |
| POST | `/avatar` | 上传头像（≤2MB，仅图片） | 登录 |

### 任务模块 `/api/tasks`

| 方法 | 路径 | 功能 | 状态流转 |
| --- | --- | --- | --- |
| POST | `/` | 创建任务 | - |
| GET | `/` | 任务列表（分页 + 状态筛选） | - |
| GET | `/{id}` | 任务详情 | - |
| PUT | `/{id}` | 编辑任务（逾期延期自动回退待办） | 逾期→待办 |
| DELETE | `/{id}` | 逻辑删除 | - |
| PUT | `/{id}/restore` | 恢复任务 | - |
| PUT | `/{id}/start` | 开始任务 | 待办→进行中 |
| PUT | `/{id}/complete` | 标记完成 | 进行中→已完成 |
| PUT | `/{id}/reopen` | 重新打开 | 已完成→待办 |
| PUT | `/{id}/complete-direct` | 直接完成 | 待办→已完成 |

### 分类模块 `/api/categories`

| 方法 | 路径 | 功能 | 权限 |
| --- | --- | --- | --- |
| POST | `/` | 创建分类 | 管理员 |
| GET | `/` | 获取所有分类（含使用统计） | 登录 |
| PUT | `/{id}` | 编辑分类 | 管理员 |
| DELETE | `/{id}` | 删除分类（关联任务校验） | 管理员 |

### 提醒模块 `/api/reminders`

| 方法 | 路径 | 功能 | 权限 |
| --- | --- | --- | --- |
| GET | `/` | 所有提醒列表（分页） | 登录 |
| GET | `/unread` | 未读提醒列表（分页） | 登录 |
| GET | `/unread-count` | 未读提醒数量 | 登录 |
| PUT | `/{id}/read` | 标记单条已读 | 登录 |
| PUT | `/read-all` | 全部标记已读 | 登录 |

### 统计模块 `/api/stats`

| 方法 | 路径 | 功能 | 权限 |
| --- | --- | --- | --- |
| GET | `/overview` | 个人任务概览 | 登录 |
| GET | `/efficiency` | 个人效率分析 | 登录 |
| GET | `/trends?period=week\|month` | 完成趋势数据 | 登录 |

### 管理员模块 `/api/admin`

| 方法 | 路径 | 功能 | 权限 |
| --- | --- | --- | --- |
| GET | `/stats/overview` | 平台聚合统计 | 管理员 |
| GET | `/users` | 用户列表（分页） | 管理员 |
| PUT | `/users/{id}/status` | 启用/禁用账号 | 管理员 |
| PUT | `/users/{id}/reset-password` | 重置密码（重置为 `123456`） | 管理员 |

---

## 核心设计说明

### 任务状态机

状态枚举值：`0-待办`、`1-进行中`、`2-已完成`、`3-已逾期`

```
        ┌──────────── start ────────────┐
        ▼                                │
     ┌──────┐                      ┌─────────┐
     │ 待办  │── complete-direct ──▶│ 已完成  │
     │  (0)  │                      │  (2)    │
     └──┬───┘                      └────┬────┘
        │                               │ reopen
        │                               ▼
        │                            ┌──────┐
        │                            │ 待办  │
        │                            └──────┘
        │ start
        ▼
     ┌─────────┐  complete   ┌─────────┐
     │ 进行中  │────────────▶│ 已完成  │
     │  (1)    │             │  (2)    │
     └─────────┘             └─────────┘
        │
        │ 系统定时任务（截止日期过期）
        ▼
     ┌─────────┐
     │ 已逾期  │── 编辑延期截止日期 ──▶ 待办(0)
     │  (3)    │
     └─────────┘
```

允许的流转：

- 待办(0) → 进行中(1) 　`/start`
- 待办(0) → 已完成(2) 　`/complete-direct`
- 进行中(1) → 已完成(2) 　`/complete`
- 已完成(2) → 待办(0) 　`/reopen`
- 待办(0) / 进行中(1) → 已逾期(3) 　系统定时任务自动触发
- 已逾期(3) → 待办(0) 　用户编辑任务延后截止日期时自动回退

### Redis 提醒队列

| Key | 类型 | TTL | 用途 |
| --- | --- | --- | --- |
| `task:reminder:zset` | Sorted Set | 永久 | 到期提醒队列，`member=taskId`，`score=提醒触发时间戳` |
| `token:{userId}` | String | 24h | JWT Token 缓存，支持主动下线 |
| `reminder:sent:{taskId}` | String | 7 天 | 已提醒标记，防止重复发送 |
| `user:{userId}:stats` | Hash | 主动失效 | 用户统计结果缓存 |

**提醒触发时间**计算：`score = due_date 时间戳 - remind_offset（分钟）× 60000`

- `remind_offset = 1440`（默认）：截止日期前 1 天提醒
- `remind_offset = 0`：不提醒

### 定时任务

| 任务 | 周期 | 说明 |
| --- | --- | --- |
| 提醒扫描 | 每 30 分钟 | 扫描 ZSet 中已到提醒时间的任务，生成站内消息 |
| 逾期标记 | 每 30 秒 | 扫描过截止日期且未完成的任务，批量标记为逾期 |

### 数据隔离

所有任务相关查询在 Service 层强制追加 `user_id = currentUserId` 过滤条件，当前用户 ID 通过 JWT 拦截器解析后存入 `HttpServletRequest` 属性，确保用户只能访问自己的任务数据。

---

## 数据库设计

共 4 张核心表，不使用物理外键，由业务逻辑保证数据一致性。

| 表名 | 说明 |
| --- | --- |
| `user` | 用户表（用户名、BCrypt 密码、头像、角色、状态） |
| `category` | 全局分类标签表（名称唯一、颜色） |
| `task` | 任务表（核心业务表，含状态机、逻辑删除、提醒偏移） |
| `reminder_log` | 提醒记录表（站内消息、已读状态） |

核心索引：

```sql
INDEX idx_user_status  (user_id, status, is_deleted)   -- 任务列表筛选
INDEX idx_user_deleted (user_id, is_deleted)           -- 统计查询
INDEX idx_due_date     (due_date)                      -- 逾期扫描
INDEX idx_category_id  (category_id)                   -- 分类删除校验
INDEX idx_user_read    (user_id, is_read)              -- 提醒未读统计
UNIQUE KEY uk_username (username)                      -- 用户名唯一
UNIQUE KEY uk_name     (name)                          -- 分类名唯一
```

完整表结构与字段说明见 [`sql/schema.sql`](sql/schema.sql)。

---

## 配置说明

### 后端 `application.yml`

```yaml
server:
  port: 8080                          # 后端端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/todo_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowMultiQueries=true
    username: root
    password: root                    # 按实际修改
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0

jwt:
  secret: todo-system-secret-key-2026-spring-boot-jwt
  expiration: 86400000                # 24 小时（毫秒）

upload:
  path: E:/upload/todo/avatar/        # 配置项（见注意事项）
```

### 前端

前端 Axios 实例直连后端 `http://localhost:8080`（见 `src/utils/request.js`）。如后端部署在其他地址，修改该文件的 `baseURL` 即可。

---

## 注意事项

1. **数据库名称**：`sql/schema.sql` 中创建的库名为 `todo_task`，而 `application.yml` 默认连接的是 `todo_db`。首次运行前请将 `application.yml` 中 `spring.datasource.url` 的数据库名改为 `todo_task`（或反过来统一），保持一致。

2. **头像上传路径**：`application.yml` 中的 `upload.path` 配置项实际未被代码使用。头像文件保存到后端**工作目录**下的 `uploads/avatar/`（即 `System.getProperty("user.dir") + "/uploads/avatar/"`），并通过 `/uploads/avatar/**` 静态资源映射访问。确保后端启动目录有写入权限。

3. **定时任务周期**：逾期标记任务代码注释为"每天凌晨1点执行"，但实际配置为 `fixedDelay = 30000`（每 30 秒执行一次），以代码实际行为为准。

4. **前端 Node 版本**：`package.json` 要求 Node.js `^22.18.0 || >=24.12.0`，低版本可能无法正常安装依赖。

5. **跨域**：后端已全局开启 CORS（`allowedOriginPatterns("*")`），开发环境可直接联调；生产环境建议收紧为前端实际域名。

---

## 许可证

本项目为课程设计作品，仅供学习交流使用。
