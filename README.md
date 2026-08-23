# 影荐 - 电影推荐工具

![GitHub stars](https://img.shields.io/github/stars/ryderhomo/film-recommend?style=flat-square)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

一款轻量级 Web 电影推荐工具，帮助你解决"看什么"的困扰。基于用户观影类型偏好自动推荐电影，支持观影记录评分、短评、随机盲盒等功能。

**在线体验**: [https://filmrecommand.ryderhomo.com](https://filmrecommand.ryderhomo.com)

## ✨ 核心特性

- 🎬 **电影浏览** - 接入 TMDb API，支持搜索、筛选、排序
- 🧠 **智能推荐** - 基于用户观影类型频率，推荐高分同类电影
- 🎲 **随机盲盒** - 一键决策，解决选片困难
- ⭐ **观影记录** - 1-5星评分、短评、想看/看过标记
- 👤 **用户认证** - JWT 无状态登录，密码 BCrypt 加密
- 📊 **观影统计** - 统计观影数量、平均评分等数据

## 📋 目录

- [技术栈](#技术栈)
- [项目架构](#项目架构)
- [快速开始](#快速开始)
- [功能说明](#功能说明)
- [API 设计](#api-设计)
- [部署指南](#部署指南)
- [开发规范](#开发规范)

---

## 技术栈

### 前端

| 技术 | 版本 | 用途 |
|-----|------|------|
| Vue 3 | 3.4+ | 渐进式 Web 框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 极速构建工具 |
| Element Plus | 2.7+ | UI 组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP 客户端 |

### 后端

| 技术 | 版本 | 用途 |
|-----|------|------|
| Spring Boot | 3.2.x | 应用框架 |
| Spring Security + JWT | - | 身份认证 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| MySQL | 8.0 | 关系数据库 |
| Redis | 7.0+ | 缓存系统 |
| Lombok | - | 减少样板代码 |

### 外部服务

- **TMDb API v3** - 电影数据源（免费、无地域限制）
- **Docker** - 容器化部署

---

## 项目架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     前端（Vue 3 + TypeScript）                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Pages: Home | Discover | Detail | Profile | Login   │  │
│  │  Components: MovieCard | RatingStars | SearchBar    │  │
│  │  State: Pinia (user, movie cache)                    │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬──────────────────────────────────┘
                         │ Axios (with JWT)
                         ↓ /api (Vite proxy → 8080)
┌────────────────────────────────────────────────────────────┐
│           后端（Spring Boot 3.2 + MyBatis-Plus）            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Controller Layer  │ REST API endpoints               │  │
│  │  Service Layer     │ 业务逻辑、推荐算法、缓存策略     │  │
│  │  Repository Layer  │ 数据库操作（MyBatis-Plus）       │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Security  │ JWT 校验过滤器                          │  │
│  │  Cache     │ Redis 缓存（热门、发现、详情）          │  │
│  │  TMDb API  │ 外部电影数据源                          │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────┬──────────────────────────────────────┬────┘
                 │                                      │
                 ↓                                      ↓
            ┌────────────┐                    ┌─────────────────┐
            │   MySQL    │                    │     Redis       │
            │ 用户、电影 │                    │ API 响应缓存    │
            │ 观影记录   │                    │ 定时刷新热门    │
            └────────────┘                    └─────────────────┘
```

### 后端分层设计

#### Controller 层（API 接口）

```java
// 认证相关
POST   /api/auth/login          // 用户登录
POST   /api/auth/register       // 用户注册

// 电影相关（无需认证）
GET    /api/movies/popular      // 热门电影列表
GET    /api/movies/search       // 搜索电影
GET    /api/movies/{id}         // 获取电影详情
GET    /api/movies/discover     // 按类型发现

// 推荐相关（无需认证，未登录返回热门）
GET    /api/recommend/for-you   // 猜你喜欢
GET    /api/recommend/random-pick  // 随机盲盒

// 观影记录（需要认证）
GET    /api/records             // 我的观影记录
POST   /api/records             // 添加观影记录
PUT    /api/records/{id}        // 更新评分/短评
DELETE /api/records/{id}        // 删除记录
GET    /api/records/stats       // 统计数据
```

#### Service 层（业务逻辑）

**1. CacheService** - 统一缓存策略
```java
// 查询顺序：Redis → MySQL → TMDb API
public List<Movie> getMoviesByType(String genre) {
    // 1. 查询 Redis（命中则返回，30min TTL）
    // 2. Redis miss，查询 MySQL 缓存表
    // 3. MySQL miss，调用 TMDb API 并存储
    // 4. 写入 Redis，返回结果
}
```

**2. RecommendService** - 推荐核心算法
```java
public List<Movie> recommendForUser(Long userId) {
    // 1. 查询用户已看电影的类型分布（加权：看过权重2，想看权重1）
    // 2. 获取 Top 3 类型
    // 3. 如果无观影记录，返回热门榜
    // 4. 查询本地库中同类型、高分（≥7.0）、未看过的电影
    // 5. 不足则从 TMDb 补充并存入 MySQL
    // 6. 按评分排序，取前 10 条返回
}
```

**3. TmdbService** - TMDb API 封装
```java
// 核心接口：热门、搜索、详情、发现
// 请求后自动将结果 upsert 到 MySQL movie 表（本地缓存）
// 支持国内网络代理配置
```

#### 推荐算法详解

**权重计算**
```
用户的"科幻"偏好度 = (看过科幻电影数 × 2 + 想看科幻电影数) / 总观影权重
```

**推荐流程**
```
1. 统计用户各类型的观影权重
2. 取权重最高的 Top 3 类型
3. 对于每个类型，查询条件：
   - genres LIKE "%{类型}%"
   - vote_average >= 7.0
   - NOT IN 用户已看过的电影ID
4. 合并三个类型的候选池
5. 按 vote_average 倒序排列
6. 返回前 10 条
```

**盲盒逻辑**
```
1. 获取推荐候选列表（复用推荐算法）
2. 若无推荐结果，兜底为热门榜
3. 从候选池中随机抽取一条
4. 支持 exclude 参数去重（连续抽取不重复）
```

### 前端项目结构

```
src/
├── api/                    # 接口层
│   ├── auth.ts            # 认证 API
│   ├── movie.ts           # 电影查询 API
│   ├── recommend.ts       # 推荐/盲盒 API
│   └── record.ts          # 观影记录 API
│
├── views/                 # 页面级组件
│   ├── HomeView.vue       # 首页（推荐+热门）
│   ├── DiscoverView.vue   # 发现/筛选页
│   ├── MovieDetailView.vue  # 电影详情页
│   ├── ProfileView.vue    # 我的记录页
│   └── LoginView.vue      # 登录/注册页
│
├── components/            # 公共组件
│   ├── MovieCard.vue      # 电影卡片（网格）
│   ├── MovieCardHorizontal.vue  # 横向滚动卡片
│   ├── RatingStars.vue    # 星级评分组件
│   ├── SearchBar.vue      # 搜索框
│   └── Layout.vue         # 全局布局
│
├── stores/                # Pinia 状态管理
│   ├── user.ts           # 用户信息 + JWT token
│   └── movie.ts          # 电影缓存
│
├── router/                # 路由配置
├── utils/                 # 工具函数
│   ├── request.ts         # Axios 实例 + 拦截器
│   └── constants.ts       # 常量定义
│
└── types/                 # TypeScript 类型定义
```

### 数据库设计

**User 表** - 用户信息
```sql
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) UNIQUE,
  email VARCHAR(100) UNIQUE,
  password VARCHAR(100) NOT NULL,  -- BCrypt 加密
  nickname VARCHAR(50),
  avatar VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME ON UPDATE CURRENT_TIMESTAMP
);
```

**Movie 表** - 电影缓存（从 TMDb 同步）
```sql
CREATE TABLE movie (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tmdb_id BIGINT UNIQUE NOT NULL,
  title VARCHAR(200) NOT NULL,
  original_title VARCHAR(200),
  poster_path VARCHAR(255),
  overview TEXT,
  release_date DATE,
  vote_average DECIMAL(3,1),  -- TMDb 评分，如 8.5
  vote_count INT,
  runtime INT,  -- 片长（分钟）
  genres VARCHAR(200),  -- 逗号分隔，如 "28,35"（28=动作,35=喜剧）
  director VARCHAR(100),
  cast VARCHAR(500),  -- 逗号分隔
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tmdb_id (tmdb_id),
  INDEX idx_genres (genres)
);
```

**WatchRecord 表** - 观影记录
```sql
CREATE TABLE watch_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  movie_id BIGINT NOT NULL,
  rating TINYINT,  -- 1-5 星
  comment VARCHAR(500),  -- 短评
  status TINYINT NOT NULL DEFAULT 1,  -- 1=想看 / 2=看过
  watch_date DATE,  -- 观影日期
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_user_movie (user_id, movie_id),
  INDEX idx_user_id (user_id),
  INDEX idx_movie_id (movie_id)
);
```

---

## 快速开始

### 前置条件

- JDK 17+（推荐 Eclipse Temurin）
- Node.js 18+
- MySQL 8.0
- Redis 7.0+
- Maven 3.6+

### 本地开发

#### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE film_recommend CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入建表脚本
SOURCE film-recommend-backend/src/main/resources/schema.sql;
```

#### 2. 后端配置

```bash
cd film-recommend-backend/src/main/resources

# 复制本地配置文件
cp application-local.yml.example application-local.yml

# 编辑 application-local.yml，填入真实密钥
# 需要配置：
# - DB_PASSWORD: MySQL 密码
# - JWT_SECRET: JWT 秘钥（生产建议 256+ 位）
# - TMDB_ACCESS_TOKEN: TMDb API Token
# - REDIS_HOST / REDIS_PORT: Redis 连接信息
```

#### 3. 启动后端

```bash
cd film-recommend-backend
mvn spring-boot:run

# 后端启动在 http://localhost:8080
# 数据库自动执行建表
# 若 movie 表为空，自动插入 20 部经典电影种子数据
```

#### 4. 启动前端

```bash
cd film-recommend-frontend

npm install
npm run dev

# 前端启动在 http://localhost:5173
# Vite 配置中 /api 代理到 http://localhost:8080
```

#### 5. 访问应用

打开浏览器访问 http://localhost:5173

### Docker 一键启动（推荐）

```bash
# 复制本地配置
cd film-recommend-backend/src/main/resources
cp application-local.yml.example application-local.yml
# 编辑并填入真实密钥

# 启动所有服务（MySQL + Redis + 后端 + 前端）
docker compose up -d

# 停止服务
docker compose down

# 查看日志
docker compose logs -f backend
```

---

## 功能说明

### 1. 电影浏览与搜索

**热门电影列表**
- 首页默认展示 TMDb 热门榜（支持分页）
- 网格卡片展示：海报 + 片名 + 年份 + 评分
- 支持按热度、评分、日期排序

**类型筛选**
- 顶部导航栏支持类型快速筛选
- 发现页提供完整类型筛选面板
- 类型对应 TMDb 分类标准

**搜索**
- 实时搜索（防抖 300ms）
- 支持电影名、导演等关键字搜索
- 点击搜索结果快速跳转详情页

### 2. 观影记录与评分

**记录电影**
- 在详情页可标记"想看"或"看过"
- "看过"可添加 1-5 星评分和短评（≤500 字）

**我的页面**
- Tab 切换查看"我的观影"和"我的想看"
- 显示统计信息：观影总数、平均评分、想看数量
- 支持删除记录

### 3. 智能推荐（猜你喜欢）

**算法原理**
1. 统计用户已看电影的类型分布（加权计算）
2. 选取 Top 3 类型
3. 在 MySQL 中查询这些类型的高分电影（≥7.0分）
4. 排除用户已看过的电影
5. 按评分倒序，返回前 10 条

**新用户体验**
- 未观影用户推荐 TMDb 热门榜 Top 10
- 观看 3+ 部电影后启用个性化推荐

**推荐质量**
- 评分过滤：只推荐 TMDb 评分 ≥ 7.0 的电影
- 类型匹配：推荐结果标注匹配度（如"92% 匹配 · 科幻"）

### 4. 随机盲盒

**一键决策**
- 首页点击🎲按钮打开盲盒模态框
- 自动从推荐候选池随机抽取一部电影
- 支持"换一个"持续抽取（去重）

**交互细节**
- 卡片翻转动画，增强仪式感
- 可直接查看详情或标记想看
- 未登录用户随机抽取热门榜

---

## API 设计

### 认证相关

#### 登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "phone": "13800138000",
  "password": "password123"
}

Response:
{
  "code": 200,
  "data": {
    "token": "eyJhbGci...",
    "userInfo": {
      "id": 1,
      "nickname": "用户昵称",
      "avatar": "https://..."
    }
  }
}
```

#### 注册
```http
POST /api/auth/register
Content-Type: application/json

{
  "phone": "13800138000",
  "password": "password123",
  "nickname": "我的昵称"
}
```

### 电影相关

#### 热门电影
```http
GET /api/movies/popular?page=1&pageSize=20

Response:
{
  "code": 200,
  "data": {
    "total": 1000,
    "items": [
      {
        "id": 1,
        "tmdbId": 550,
        "title": "搏击俱乐部",
        "posterPath": "/poster.jpg",
        "voteAverage": 8.8,
        "releaseDate": "1999-10-15"
      }
    ]
  }
}
```

#### 搜索电影
```http
GET /api/movies/search?keyword=新手&page=1

Response: 同上
```

#### 电影详情
```http
GET /api/movies/{id}

Response:
{
  "code": 200,
  "data": {
    "id": 1,
    "tmdbId": 550,
    "title": "搏击俱乐部",
    "overview": "一个受挫的上班族...",
    "director": "大卫·芬奇",
    "cast": "布拉德·皮特,爱德华·诺顿",
    "voteAverage": 8.8,
    "runtime": 139,
    "genres": ["犯罪", "剧情"],
    "myRating": 5,  // 已登录用户的评分
    "myComment": "经典之作"  // 已登录用户的短评
  }
}
```

### 推荐相关

#### 猜你喜欢
```http
GET /api/recommend/for-you

Response:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "title": "星际穿越",
      "posterPath": "/poster.jpg",
      "voteAverage": 8.6,
      "matchDegree": 92,  // 匹配度百分比
      "reason": "因为你常看科幻片"
    }
  ]
}
```

#### 随机盲盒
```http
GET /api/recommend/random-pick?exclude=1,2,3

Response:
{
  "code": 200,
  "data": {
    "id": 5,
    "title": "降临",
    "posterPath": "/poster.jpg",
    "overview": "一句话简介...",
    "voteAverage": 7.9
  }
}
```

### 观影记录相关

#### 获取我的记录
```http
GET /api/records?status=2&page=1  # status: 1=想看, 2=看过

Headers: Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "movie": {/* 电影信息 */},
      "rating": 5,
      "comment": "很棒的电影",
      "status": 2,
      "watchDate": "2024-08-20"
    }
  ]
}
```

#### 添加/更新记录
```http
POST /api/records
Headers: Authorization: Bearer {token}

{
  "movieId": 1,
  "status": 2,
  "rating": 5,
  "comment": "非常好看！"
}
```

#### 统计数据
```http
GET /api/records/stats

Headers: Authorization: Bearer {token}

Response:
{
  "code": 200,
  "data": {
    "totalWatched": 42,
    "averageRating": 7.8,
    "totalWantWatch": 15,
    "genreDistribution": {
      "科幻": 15,
      "动作": 12,
      "剧情": 10
    }
  }
}
```

---

## 部署指南

### Docker 生产部署

#### 1. 配置环境

```bash
# 复制生产配置
cp docker-compose.prod.yml docker-compose.yml

# 创建 .env 文件
cat > .env << EOF
DB_PASSWORD=your_strong_password
JWT_SECRET=your_256bit_secret_key
TMDB_ACCESS_TOKEN=your_tmdb_token
REDIS_PASSWORD=your_redis_password
EOF
```

#### 2. 构建镜像

```bash
# 配置本地镜像仓库地址
export REGISTRY=docker.io  # 或 ryderhomo

# 构建并推送
./build-and-push.sh

# 或手动构建
docker build -f Dockerfile.backend -t $REGISTRY/filmrecommend-backend:latest .
docker build -f Dockerfile.frontend -t $REGISTRY/filmrecommend-frontend:latest .

docker push $REGISTRY/filmrecommend-backend:latest
docker push $REGISTRY/filmrecommend-frontend:latest
```

#### 3. 服务器部署

```bash
# 上传 docker-compose.prod.yml、schema.sql 和 .env

# 一键启动
docker compose up -d

# 查看状态
docker compose ps

# 查看日志
docker compose logs -f

# 访问应用
# http://your-domain.com
```

#### Docker 架构

```
┌─────────────────────────────────────┐
│      Nginx (Port 80/443)             │
│  ├─ / → frontend:80                  │
│  └─ /api/ → backend:8080 (反向代理)  │
└─────────────────────────────────────┘
           │              │
    ┌──────┴──────┐  ┌────┴──────┐
    ↓             ↓  ↓           ↓
frontend       backend          MySQL
(Vue3)      (Spring Boot)    Redis
```

### 宝塔面板部署（备选）

1. **后端**
   - 上传 Jar 包到服务器
   - 宝塔 → Java 项目 → 新建项目
   - 配置 JDK 路径、内存大小、环境变量

2. **前端**
   - 本地 `npm run build` 生成 dist 目录
   - 通过 SFTP 上传到服务器
   - 宝塔 → 网站 → 新建站点，指向 dist 目录
   - 配置 Nginx 反向代理 `/api` 到后端

3. **数据库**
   - 宝塔自带 MySQL 管理界面
   - 导入 schema.sql 自动建表

---

## 开发规范

### Git 提交规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型**
- `feat` - 新功能
- `fix` - 修复 bug
- `docs` - 文档更新
- `style` - 代码格式调整
- `refactor` - 代码重构
- `test` - 添加测试
- `chore` - 构建、依赖等维护工作

**示例**
```
feat(recommend): 添加基于用户类型偏好的推荐算法

实现推荐算法核心逻辑：
- 统计用户观影类型分布
- Top 3 类型高分推荐
- 排除已看过电影

Closes #123
```

### 命名规范

**后端**
- 包名：`com.film.模块名`（如 `com.film.service`）
- 类名：大驼峰（如 `MovieService`）
- 方法名：小驼峰（如 `getRecommendMovies`）
- 数据库：表名小写下划线（如 `watch_record`）

**前端**
- 组件文件：大驼峰（如 `MovieCard.vue`）
- 其他文件：小写下划线（如 `api/movie.ts`）
- 变量/方法：小驼峰（如 `getMovieList`）

### 代码风格

- 后端使用 Lombok 减少样板代码
- 前端使用 TypeScript 保障类型安全
- 尽可能使用 Composition API（Vue 3）
- 关键方法添加 JavaDoc / JSDoc 注释

---

## 常见问题

### Q: 如何配置 TMDb API？

**A:** 访问 [TMDb 官网](https://www.themoviedb.org/settings/api) 申请 API，获取 Bearer Access Token（非 API Key），填入 `application-local.yml` 的 `tmdb.access-token` 字段。

### Q: 国内无法访问 TMDb 怎么办？

**A:** 在 `application-local.yml` 中配置代理：
```yaml
tmdb:
  proxy-host: 127.0.0.1
  proxy-port: 58812
```

### Q: 如何修改推荐算法？

**A:** 编辑后端 `RecommendService.java`，核心方法为 `recommendForUser()`。调整权重计算、评分阈值、推荐数量等。

### Q: 前端如何开发和调试？

**A:** 启动 dev server：`npm run dev`，Vite 自动开启热更新。修改文件自动刷新浏览器。

### Q: 如何关闭某个功能？

**A:** 功能逻辑集中在 `service/` 层，直接注释或删除相应方法调用即可。前端对应页面删除或隐藏组件。

---

## 常用命令

### 后端

```bash
# 开发启动
mvn spring-boot:run

# 打包
mvn package -DskipTests

# 构建 Docker 镜像
docker build -t filmrecommend-backend:latest .

# 查看依赖树
mvn dependency:tree
```

### 前端

```bash
# 安装依赖
npm install

# 开发服务器（热更新）
npm run dev

# 类型检查
npm run type-check

# 生产构建
npm run build

# 预览生产构建
npm run preview

# 代码规范检查（如配置 ESLint）
npm run lint
```

### Docker

```bash
# 启动所有服务
docker compose up -d

# 停止并移除容器
docker compose down

# 查看日志
docker compose logs -f backend

# 重启单个服务
docker compose restart backend

# 进入容器
docker compose exec backend bash
```

---

## 项目进度

- [x] 基础用户认证（JWT）
- [x] 电影浏览与搜索
- [x] 观影记录与评分
- [x] 智能推荐算法
- [x] 随机盲盒功能
- [x] Redis 缓存层
- [x] Docker 容器化部署
- [ ] 观影统计与数据可视化
- [ ] 好友推荐功能
- [ ] 片单管理

---

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送到分支：`git push origin feature/amazing-feature`
5. 开启 Pull Request

---

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

---

## 联系方式

- 作者：Ryder
- Email: your_email@example.com
- 在线体验：[filmrecommand.ryderhomo.com](https://filmrecommand.ryderhomo.com)

---

**⭐ 如果觉得这个项目对你有帮助，请给一个 Star！**
