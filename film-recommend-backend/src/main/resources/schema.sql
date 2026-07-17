-- 影荐 数据库建表脚本

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt加密',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `movie` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tmdb_id BIGINT UNIQUE NOT NULL COMMENT 'TMDb电影ID',
    title VARCHAR(200) NOT NULL COMMENT '电影名称',
    original_title VARCHAR(200) COMMENT '原名',
    poster_path VARCHAR(255) COMMENT '海报相对路径',
    overview TEXT COMMENT '剧情简介',
    release_date DATE COMMENT '上映日期',
    vote_average DECIMAL(3,1) COMMENT 'TMDb评分',
    vote_count INT COMMENT '评分人数',
    runtime INT COMMENT '片长(分钟)',
    genres VARCHAR(200) COMMENT '类型，逗号分隔',
    director VARCHAR(100) COMMENT '导演',
    cast VARCHAR(500) COMMENT '主要演员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '本地入库时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影缓存表';

CREATE TABLE IF NOT EXISTS `watch_record` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    movie_id BIGINT NOT NULL COMMENT '电影ID',
    rating TINYINT COMMENT '评分1-5',
    comment VARCHAR(500) COMMENT '短评',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=想看 2=看过',
    watch_date DATE COMMENT '观影日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_movie (user_id, movie_id),
    INDEX idx_user_id (user_id),
    INDEX idx_movie_id (movie_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观影记录表';
