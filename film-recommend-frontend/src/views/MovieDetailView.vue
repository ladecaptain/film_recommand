<template>
  <div class="movie-detail">
    <div class="back-link">
      <el-button text @click="$router.back()">← 返回</el-button>
    </div>

    <div class="detail-layout">
      <div class="poster-section">
        <div class="poster-placeholder">
          <span>海报加载中...</span>
        </div>
      </div>

      <div class="info-section">
        <h1 class="movie-title">{{ movie?.title || '电影详情' }}</h1>

        <div class="meta-info">
          <span v-if="movie?.releaseDate">{{ movie.releaseDate.substring(0, 4) }}</span>
          <span v-if="movie?.runtime">{{ movie.runtime }} 分钟</span>
          <span v-if="movie?.genres">{{ movie.genres.replace(/,/g, ' · ') }}</span>
        </div>

        <div class="rating-display">
          <span class="score">{{ movie?.voteAverage?.toFixed(1) }}</span>
          <span class="vote-count">TMDb 评分</span>
        </div>

        <p class="overview">{{ movie?.overview || '暂无简介' }}</p>

        <div class="actions" v-if="userStore.isLoggedIn">
          <el-button type="primary">看过</el-button>
          <el-button>想看</el-button>
        </div>
        <div v-else class="login-hint">
          <router-link to="/login">登录</router-link> 后即可评分和标记
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import type { Movie } from '@/types'

const userStore = useUserStore()
const movie = ref<Movie | null>(null)
</script>

<style scoped lang="scss">
.back-link {
  margin-bottom: 20px;
}

.detail-layout {
  display: flex;
  gap: 40px;
}

.poster-section {
  flex-shrink: 0;
  width: 300px;

  .poster-placeholder {
    width: 100%;
    aspect-ratio: 2/3;
    background-color: var(--bg-card);
    border-radius: 8px;
    border: 1px solid var(--border-color);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-muted);
  }
}

.info-section {
  flex: 1;
}

.movie-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
}

.meta-info {
  display: flex;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 16px;
}

.rating-display {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 20px;

  .score {
    font-size: 36px;
    font-weight: 700;
    color: var(--accent-gold);
  }

  .vote-count {
    color: var(--text-muted);
    font-size: 14px;
  }
}

.overview {
  line-height: 1.8;
  color: var(--text-secondary);
  font-size: 15px;
  margin-bottom: 24px;
}

.actions {
  display: flex;
  gap: 12px;
}

.login-hint {
  color: var(--text-muted);
  font-size: 14px;

  a {
    color: var(--accent-gold);
  }
}
</style>
