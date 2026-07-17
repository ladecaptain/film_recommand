# 应用技术设计

## 技术栈

| 层级 | 技术选型 | 版本 | 选型理由 |
|------|---------|------|---------|
| 前端框架 | Vue 3 | 3.4+ | 组合式 API，轻量灵活，适合单人开发 |
| 前端构建 | Vite | 5.x | 极速冷启动，热更新快 |
| 前端语言 | TypeScript | 5.x | 类型安全，减少运行时错误 |
| UI 组件库 | Element Plus | 2.7+ | 开箱即用，文档齐全，Vue 3 生态最成熟 |
| 状态管理 | Pinia | 2.x | Vue 3 官方推荐，比 Vuex 更轻量 |
| 前端路由 | Vue Router | 4.x | Vue 官方路由 |
| HTTP 客户端 | Axios | 1.x | 拦截器友好，统一处理请求/响应 |
| 后端框架 | Spring Boot | 3.2.x | 熟悉度高，生态完善 |
| ORM | MyBatis-Plus | 3.5.x | 熟悉度高，CRUD 零配置 |
| 数据库 | MySQL | 8.0 | 稳定可靠，部署方便 |
| 认证方案 | JWT + Spring Security | - | 无状态认证，适合前后端分离 |
| 密码加密 | BCrypt | - | Spring Security 内置，安全性高 |
| 外部 API | TMDb API v3 | - | 免费，无需科学上网，数据全面 |
| 连接池 | HikariCP | - | Spring Boot 默认，高性能 |
| 部署方案 | 宝塔面板 / Docker Compose | - | 一人开发，运维越简单越好 |


## 项目结构

### 后端项目结构（Spring Boot）

```
film-recommend-backend/
├── src/
│   ├── main/
│   │   ├── java/com/film/
│   │   │   ├── FilmApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java        # Spring Security + JWT 配置
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── CorsConfig.java            # 跨域配置
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java        # 登录/注册
│   │   │   │   ├── MovieController.java       # 电影查询、详情、搜索
│   │   │   │   ├── RecommendController.java   # 猜你喜欢 + 盲盒
│   │   │   │   └── RecordController.java      # 观影记录 CRUD
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── MovieService.java
│   │   │   │   ├── RecordService.java
│   │   │   │   ├── RecommendService.java      # ★ 推荐算法核心
│   │   │   │   └── TmdbService.java           # TMDb API 封装
│   │   │   ├── mapper/
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── MovieMapper.java
│   │   │   │   └── RecordMapper.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Movie.java
│   │   │   │   └── WatchRecord.java
│   │   │   ├── dto/                           # 请求/响应对象
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── MovieDTO.java
│   │   │   │   ├── RecordRequest.java
│   │   │   │   └── ...
│   │   │   ├── util/
│   │   │   │   ├── JwtUtil.java
│   │   │   │   └── TmdbApiUtil.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── BusinessException.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/                                  # 单元测试
├── pom.xml
└── README.md
```

### 前端项目结构（Vue 3）

```
film-recommend-frontend/
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── api/                         # 接口层，按模块拆分
│   │   ├── auth.ts                  # 登录/注册
│   │   ├── movie.ts                 # 电影搜索/详情
│   │   ├── recommend.ts             # 推荐/盲盒
│   │   └── record.ts                # 观影记录
│   ├── assets/                      # 静态资源
│   │   └── styles/
│   │       └── theme.scss           # 全局主题色
│   ├── components/                  # 公共组件
│   │   ├── MovieCard.vue            # 电影卡片（网格）
│   │   ├── MovieCardHorizontal.vue  # 电影卡片（横向滚动）
│   │   ├── RatingStars.vue          # 星级评分组件
│   │   ├── SearchBar.vue            # 顶部搜索框
│   │   └── Layout.vue               # 整体布局（含导航）
│   ├── views/                       # 页面
│   │   ├── HomeView.vue             # 首页（推荐+热门）
│   │   ├── DiscoverView.vue         # 发现/浏览页
│   │   ├── MovieDetailView.vue      # 电影详情页
│   │   ├── ProfileView.vue          # 我的页面
│   │   └── LoginView.vue            # 登录/注册页
│   ├── stores/                      # Pinia 状态
│   │   ├── user.ts                  # 用户信息 + token
│   │   └── movie.ts                 # 缓存电影数据
│   ├── router/
│   │   └── index.ts                 # 路由配置
│   ├── utils/
│   │   ├── request.ts               # Axios 实例 + 拦截器
│   │   └── constants.ts             # 常量（类型列表等）
│   └── types/                       # TypeScript 类型定义
│       └── index.ts
├── index.html
├── package.json
├── vite.config.ts
└── tsconfig.json
```


## 数据模型

### User（用户）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| phone | VARCHAR(20) | UNIQUE | 手机号（登录用） |
| email | VARCHAR(100) | UNIQUE | 邮箱（登录用） |
| password | VARCHAR(100) | NOT NULL | BCrypt 加密 |
| nickname | VARCHAR(50) | | 昵称 |
| avatar | VARCHAR(255) | | 头像 URL |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 注册时间 |
| update_time | DATETIME | ON UPDATE | 更新时间 |

### Movie（电影）

> 从 TMDb 同步，仅作本地缓存

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 自增主键 |
| tmdb_id | BIGINT | UNIQUE, NOT NULL | TMDb 电影ID |
| title | VARCHAR(200) | NOT NULL | 电影名称 |
| original_title | VARCHAR(200) | | 原名 |
| poster_path | VARCHAR(255) | | 海报相对路径 |
| overview | TEXT | | 剧情简介 |
| release_date | DATE | | 上映日期 |
| vote_average | DECIMAL(3,1) | | TMDb 评分（如 8.5） |
| vote_count | INT | | 评分人数 |
| runtime | INT | | 片长（分钟） |
| genres | VARCHAR(200) | | 类型，逗号分隔（如 "科幻,悬疑"） |
| director | VARCHAR(100) | | 导演 |
| cast | VARCHAR(500) | | 主要演员，逗号分隔 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 本地入库时间 |

### WatchRecord（观影记录）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 记录ID |
| user_id | BIGINT | NOT NULL, INDEX | 用户ID |
| movie_id | BIGINT | NOT NULL, INDEX | 电影ID |
| rating | TINYINT | 1-5 | 用户评分（1~5星） |
| comment | VARCHAR(500) | | 短评 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 1=想看 / 2=看过 |
| watch_date | DATE | | 观影日期（默认当天） |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 记录时间 |
| update_time | DATETIME | ON UPDATE | 更新时间 |

> **唯一约束**：(user_id, movie_id) 组合唯一，同一用户对同一电影只有一条记录（更新 status 即可）

### 表关系图

```
User (1) ──────< WatchRecord >────── (M) Movie
                    │
                    └── 通过 user_id + movie_id 关联
```


## 关键技术点

### 1. TMDb API 对接

**配置**

API 读访问令牌：eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3YzE3NjRjMzI2NTBlYzU4NTkzNTYwMzA3MjkyNWJlZCIsIm5iZiI6MTc4NDI2NTA1MC43NzUsInN1YiI6IjZhNTliOTVhMzQyNDk5OTE2ODYyNzhjMyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.j2TGAVBZ6R79n0LF3664GGYo2M6woVBUFduOPL6lhxM

API密钥：7c1764c32650ec585935603072925bed


**核心接口封装**（TmdbService.java）

```java
// 热门电影列表
public List<MovieDTO> getPopularMovies(int page) {
    // GET /movie/popular?api_key=xxx&page=1
}

// 按关键字搜索
public List<MovieDTO> searchMovies(String keyword, int page) {
    // GET /search/movie?api_key=xxx&query=keyword
}

// 获取电影详情
public MovieDetailDTO getMovieDetail(Long tmdbId) {
    // GET /movie/{id}?api_key=xxx&append_to_response=credits
}

// 按类型筛选
public List<MovieDTO> discoverByGenres(List<String> genres, int page) {
    // GET /discover/movie?api_key=xxx&with_genres=28,35
}
```

**调用策略**：
- 每次请求优先查本地 MySQL，命中则直接返回
- 未命中则调用 TMDb API，获取后存入本地库再返回
- 图片 URL 拼接：`tmdb.image-base-url + posterSize + poster_path`

**类型 ID 映射**（TMDb 使用数字 ID）

```
28=动作, 35=喜剧, 878=科幻, 53=惊悚, 18=剧情,
27=恐怖, 10749=爱情, 16=动画, 80=犯罪, 9648=悬疑
```

### 2. 推荐算法实现

**核心逻辑**（RecommendService.java）：

```java
public List<Movie> recommendForUser(Long userId) {
    // 1. 查询用户所有已看电影的类型分布
    List<String> genres = recordMapper.getTopGenresByUser(userId, 3);
    // 返回如 ["科幻", "悬疑", "动画"]
    
    // 2. 如果没有记录 → 返回热门榜
    if (genres.isEmpty()) {
        return tmdbService.getPopularMovies(1);
    }
    
    // 3. 查询用户已看过/想看的电影ID（用于排除）
    List<Long> watchedIds = recordMapper.getMovieIdsByUser(userId);
    
    // 4. 在本地库中找：包含这些类型、且未看过的高分电影
    List<Movie> candidates = movieMapper.selectRecommend(
        genres,          // 类型列表
        watchedIds,      // 已看排除
        7.0,             // 最低评分
        20               // 取前20条
    );
    
    // 5. 如果本地库不够 → 从 TMDb 补充
    //    （调用 TMDb 的 discover 接口，按类型+评分筛选，存入本地库）
    
    // 6. 按评分倒序排列，取前10
    return candidates.stream()
        .sorted((a,b) -> b.getVoteAverage() - a.getVoteAverage())
        .limit(10)
        .collect(Collectors.toList());
}
```

**相关 SQL**（Mapper 中）：

```sql
-- 查询用户最常看的类型 Top 3
SELECT genres FROM watch_record wr 
JOIN movie m ON wr.movie_id = m.id 
WHERE wr.user_id = #{userId} AND wr.status = 2
GROUP BY m.genres 
ORDER BY COUNT(*) DESC LIMIT 3;

-- 基于类型推荐（模糊匹配）
SELECT * FROM movie 
WHERE (genres LIKE CONCAT('%', #{genre1}, '%') 
    OR genres LIKE CONCAT('%', #{genre2}, '%')
    OR genres LIKE CONCAT('%', #{genre3}, '%'))
  AND id NOT IN (<watchedIds>)
  AND vote_average >= #{minScore}
ORDER BY vote_average DESC LIMIT #{limit};
```

### 3. 盲盒随机逻辑

```java
public Movie getRandomPick(Long userId) {
    // 1. 获取推荐候选列表（复用推荐逻辑）
    List<Movie> candidates = recommendService.recommendForUser(userId);
    
    // 2. 如果为空，兜底返回热门榜随机
    if (candidates.isEmpty()) {
        candidates = tmdbService.getPopularMovies(1);
    }
    
    // 3. 随机抽取一条
    int randomIndex = ThreadLocalRandom.current().nextInt(candidates.size());
    return candidates.get(randomIndex);
}
```

### 4. 认证与鉴权（JWT）

**依赖**（pom.xml）：
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

**流程**：
1. 用户登录 → 验证账号密码 → 生成 JWT 返回前端
2. 前端存储 JWT（localStorage），后续请求在 Header 中携带：`Authorization: Bearer <token>`
3. 后端过滤器校验 Token 有效性，解析用户 ID 存入 SecurityContext

**Token 过期时间**：7 天（无需 refresh token，简化设计）

### 5. 缓存策略

由于单人开发 + 初期用户量小，**暂不引入 Redis**，采用以下方式：

| 数据类型 | 缓存方式 | 失效策略 |
|---------|---------|---------|
| TMDb 热门电影列表 | 本地 MySQL（落库） | 每次调用时更新（Upsert） |
| 电影详情 | 本地 MySQL | 首次查询时写入，后续复用 |
| 用户推荐结果 | 不缓存 | 每次实时计算（数据量小，毫秒级） |
| 类型列表 | 前端硬编码 | 不变 |

> 后续如需性能优化，可直接引入 Spring Cache + Redis，改动成本低。

### 6. 前端状态管理

**Pinia Store 设计**（stores/user.ts）：

```typescript
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null as UserInfo | null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    login(phone: string, password: string) { /* 调用 API，存储 token */ },
    logout() { /* 清除 token 和用户信息 */ },
    fetchUserInfo() { /* 获取当前用户资料 */ },
  },
});
```

### 7. 前端请求拦截器

```typescript
// utils/request.ts
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// 请求拦截器：自动添加 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // token 过期，跳转登录
    }
    return Promise.reject(error);
  }
);
```

### 8. 数据库连接配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/film_recommend?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: root
    password: 123456
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发时开启SQL日志
  global-config:
    db-config:
      logic-delete-field: deleted
```

### 9. 前端环境变量配置

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api

# .env.production
VITE_API_BASE_URL=https://your-domain.com/api
```

### 10. 部署方案（宝塔面板）

1. **后端**：打包成 Jar 包，在宝塔中配置 Spring Boot 项目（JDK 17+）
2. **前端**：`npm run build` 生成 dist 目录，在宝塔中配置 Nginx 指向该目录
3. **数据库**：宝塔自带 MySQL 管理，导入 SQL 脚本创建表
4. **Nginx 配置关键点**：将 `/api` 请求反向代理到后端端口（如 8080）