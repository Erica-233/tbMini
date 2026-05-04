# TbMini Forum

前后端分离的 Reddit-like 论坛 MVP。

## 技术栈

- 后端：Spring Boot 3.2、Java 17、MySQL 8、MyBatis-Plus 3.5、Spring Security、JWT、springdoc-openapi
- 前端：Vue 3、Vite 5、TypeScript、Pinia、Vue Router、Axios、Element Plus
- 图片存储：本地文件系统 `backend/uploads/`，后端静态映射 `/uploads/**`

## 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < db/schema.sql
mysql -u root -p < db/seed.sql
```

默认数据库名 `tbmini`，用户名/密码在 `backend/src/main/resources/application.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tbmini?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### 2. 启动后端

```bash
cd backend
# 确保 JAVA_HOME 指向 JDK 17
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

Swagger UI：`http://localhost:8080/swagger-ui.html`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`。

## 用户账号（seed.sql）

| 邮箱 | 用户名 | 密码 | 角色 |
|---|---|---|---|
| admin@example.com | admin | Admin123! | ADMIN |
| alice@example.com | alice | User123! | USER |
| bob@example.com | bob | User123! | USER |
| carol@example.com | carol | User123! | USER |

## API 概览

### 认证 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| POST | `/api/auth/register` | 注册 | 无 |
| POST | `/api/auth/login` | 登录 | 无 |
| GET | `/api/auth/me` | 获取当前用户信息 | 需要 |

### 板块 `/api/boards`

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| GET | `/api/boards` | 获取所有活跃板块 | 无 |
| GET | `/api/boards/{slug}` | 获取板块详情 | 无 |
| GET | `/api/boards/{slug}/posts` | 获取板块下的帖子列表 | 无 |

### 帖子 `/api/posts`

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| GET | `/api/posts` | 帖子列表（可按 boardId 筛选） | 可选 |
| GET | `/api/posts/{id}` | 帖子详情 | 可选 |
| POST | `/api/posts` | 创建帖子（multipart/form-data） | 需要 |
| PUT | `/api/posts/{id}` | 更新帖子（multipart/form-data） | 需要 |
| DELETE | `/api/posts/{id}` | 删除帖子 | 需要 |
| POST | `/api/posts/{id}/like` | 点赞 | 需要 |
| DELETE | `/api/posts/{id}/like` | 取消点赞 | 需要 |

### 评论 `/api`

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| GET | `/api/posts/{postId}/comments` | 获取帖子评论列表 | 可选 |
| POST | `/api/comments` | 创建评论 | 需要 |
| DELETE | `/api/comments/{id}` | 删除评论 | 需要 |

### 举报 `/api/reports`

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| POST | `/api/reports` | 提交举报 | 需要 |

### 管理后台 `/api/admin`（需 ADMIN 角色）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/admin/mod-queue/posts` | 获取待审核帖子列表 |
| POST | `/api/admin/posts/{id}/approve` | 审批通过帖子 |
| POST | `/api/admin/posts/{id}/remove` | 删除/移除帖子 |
| GET | `/api/admin/reports` | 获取举报列表 |
| GET | `/api/admin/actions` | 获取管理操作记录 |

## curl 验证流程

以下命令假设后端运行在 `http://localhost:8080`。

### 1. 注册

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"eve","email":"eve@example.com","password":"User123!","nickname":"Eve"}' | jq
```

### 2. 登录

```bash
# 普通用户登录
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"User123!"}' | jq

# 管理员登录
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Admin123!"}' | jq
```

将返回的 `token` 保存到变量：

```bash
TOKEN="eyJhbGciOiJIUzI1NiIs..."
ADMIN_TOKEN="eyJhbGciOiJIUzI1NiIs..."
```

### 3. 查看板块列表

```bash
curl -s http://localhost:8080/api/boards | jq
```

### 4. 发帖（含图）

纯文本帖子（使用 boardSlug 指定板块）：

```bash
curl -s -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -F 'data={"boardSlug":"general","title":"Hello","bodyMd":"World"};type=application/json' | jq
```

带图片帖子（准备一张图片 `test.jpg`）：

```bash
curl -s -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -F 'data={"boardSlug":"general","title":"With Image","bodyMd":"See the image"};type=application/json' \
  -F "images=@test.jpg" | jq
```

### 5. 评论

```bash
# 根评论
curl -s -X POST http://localhost:8080/api/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"postId":1,"bodyMd":"Nice post!"}' | jq

# 回复根评论（假设根评论 id=1）
curl -s -X POST http://localhost:8080/api/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"postId":1,"parentId":1,"bodyMd":"Thanks!"}' | jq
```

### 6. 点赞 / 取消点赞

```bash
# 点赞帖子 id=1
curl -s -X POST http://localhost:8080/api/posts/1/like \
  -H "Authorization: Bearer $TOKEN" | jq

# 取消点赞
curl -s -X DELETE http://localhost:8080/api/posts/1/like \
  -H "Authorization: Bearer $TOKEN" | jq
```

### 7. 三人举报触发自动入队

使用三个不同用户（alice, bob, carol）分别举报同一帖子：

```bash
# Alice 举报帖子 id=1
ALICE_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"alice@example.com","password":"User123!"}' | jq -r '.data.token')
curl -s -X POST http://localhost:8080/api/reports \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{"targetType":"POST","targetId":1,"reasonCode":"SPAM","reasonText":"Looks like spam"}' | jq

# Bob 举报帖子 id=1
BOB_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"bob@example.com","password":"User123!"}' | jq -r '.data.token')
curl -s -X POST http://localhost:8080/api/reports \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -d '{"targetType":"POST","targetId":1,"reasonCode":"SPAM","reasonText":"Looks like spam"}' | jq

# Carol 举报帖子 id=1
CAROL_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"carol@example.com","password":"User123!"}' | jq -r '.data.token')
curl -s -X POST http://localhost:8080/api/reports \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CAROL_TOKEN" \
  -d '{"targetType":"POST","targetId":1,"reasonCode":"SPAM","reasonText":"Looks like spam"}' | jq
```

此时帖子状态自动变为 `PENDING_REVIEW`，并写入 `moderation_actions` 审计记录（action=AUTO_FLAGGED）。

普通用户再次访问帖子详情应返回 404：

```bash
curl -s -w "\nHTTP %{http_code}\n" http://localhost:8080/api/posts/1 \
  -H "Authorization: Bearer $ALICE_TOKEN"
```

管理员可以查看：

```bash
# 查看待审队列
curl -s http://localhost:8080/api/admin/mod-queue/posts \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq

# 查看举报列表
curl -s http://localhost:8080/api/admin/reports \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq
```

### 8. 管理员 approve / remove

```bash
# approve 帖子 id=1
curl -s -X POST "http://localhost:8080/api/admin/posts/1/approve" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq

# remove 帖子 id=1（带原因）
curl -s -X POST "http://localhost:8080/api/admin/posts/1/remove?reason=违反社区规范" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq
```

### 9. 查看审计记录

```bash
curl -s http://localhost:8080/api/admin/actions \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq
```

## 项目结构

```
tbMini/
├── backend/                     # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/example/tbmini/
│       ├── TbminiApplication.java
│       ├── config/
│       │   ├── MybatisPlusConfig.java
│       │   ├── SecurityConfig.java
│       │   └── WebConfig.java
│       ├── controller/
│       │   ├── AdminController.java
│       │   ├── AuthController.java
│       │   ├── BoardController.java
│       │   ├── CommentController.java
│       │   ├── PostController.java
│       │   └── ReportController.java
│       ├── domain/
│       │   ├── common/
│       │   │   ├── BizException.java
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   ├── PageResult.java
│       │   │   └── Result.java
│       │   └── entity/
│       │       ├── Board.java
│       │       ├── Comment.java
│       │       ├── ModerationAction.java
│       │       ├── Post.java
│       │       ├── PostImage.java
│       │       ├── PostLike.java
│       │       ├── Report.java
│       │       ├── Role.java
│       │       └── User.java
│       ├── dto/
│       │   ├── BoardDetailResponse.java
│       │   ├── BoardVo.java
│       │   ├── CommentRequest.java
│       │   ├── CommentVo.java
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   ├── ModerationActionVo.java
│       │   ├── ModerationRequest.java
│       │   ├── PostCreateRequest.java
│       │   ├── PostDetailResponse.java
│       │   ├── PostListItemVo.java
│       │   ├── PostVo.java
│       │   ├── RegisterRequest.java
│       │   ├── ReportRequest.java
│       │   ├── ReportVo.java
│       │   └── UserResponse.java
│       ├── mapper/
│       │   ├── BoardMapper.java
│       │   ├── CommentMapper.java
│       │   ├── ModerationActionMapper.java
│       │   ├── PostImageMapper.java
│       │   ├── PostLikeMapper.java
│       │   ├── PostMapper.java
│       │   ├── ReportMapper.java
│       │   └── UserMapper.java
│       ├── security/
│       │   ├── CustomUserDetailsService.java
│       │   ├── JwtAuthenticationEntryPoint.java
│       │   ├── JwtAuthenticationFilter.java
│       │   ├── JwtUtil.java
│       │   └── UserPrincipal.java
│       └── service/
│           ├── AdminService.java
│           ├── AuthService.java
│           ├── BoardService.java
│           ├── CommentService.java
│           ├── PostService.java
│           ├── ReportService.java
│           └── UploadService.java
├── frontend/                    # Vue 3 前端
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── router/
│       ├── stores/
│       ├── api/
│       ├── views/
│       └── components/
├── db/
│   ├── schema.sql
│   └── seed.sql
└── README.md
```

## 数据库表结构

| 表名 | 说明 |
|---|---|
| users | 用户表（USER/ADMIN），含 username、email、is_banned |
| boards | 板块表，slug 作为 URL 标识 |
| posts | 帖子表，含 content（HTML）和 body_md（Markdown） |
| post_images | 帖子图片表（支持多图） |
| comments | 评论表（两层结构：根评论 + 回复根评论） |
| post_likes | 帖子点赞表（幂等：同一用户对同一帖子只能点赞一次） |
| reports | 举报表，同一用户对同一 target 只能有一条 OPEN 举报 |
| moderation_actions | 管理操作审计表，moderator_id 为 NULL 表示系统自动行为 |

## 业务规则摘要

1. **角色**：Guest、User、Admin（RBAC）
2. **审核策略 C**：帖子/评论默认 `PUBLISHED`；当某个帖子或评论的 `OPEN` 举报数 >= 3 时，自动将该内容 `status` 置为 `PENDING_REVIEW` 并写审计 `action=AUTO_FLAGGED`
3. **可见性规则**：
   - Guest/USER 只能访问 `PUBLISHED` 内容
   - 对于 `PENDING_REVIEW` 或 `REMOVED` 的帖子/评论，Guest/USER 访问详情接口返回 404（统一视为不可见）
   - ADMIN 可以访问所有状态
4. **评论结构**：两层（根评论 + 回复根评论），禁止回复回复
5. **点赞**：仅帖子点赞 like/unlike，幂等
6. **举报**：同一用户对同一 target 只能存在一条 OPEN 举报；重复举报返回幂等成功
7. **管理后台 API**：
   - 查看待审帖子队列（`status=PENDING_REVIEW`）
   - approve：`PENDING_REVIEW -> PUBLISHED`
   - remove：任何状态 -> `REMOVED`（带 reason）
   - 查看举报列表（`status=OPEN`）
   - 所有管理动作写 `moderation_actions` 审计表
8. **统一响应格式**：`{code,message,data}`；全局异常处理；参数校验
