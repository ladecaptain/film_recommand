<template>
  <div class="movie-detail">
    <div class="back-link">
      <el-button text @click="$router.back()">← 返回</el-button>
    </div>

    <div v-if="loading" class="detail-layout">
      <div class="poster-section">
        <div class="poster-placeholder skeleton-poster"></div>
      </div>
      <div class="info-section">
        <el-skeleton :rows="6" animated />
      </div>
    </div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <el-button @click="fetchDetail">重试</el-button>
    </div>

    <div v-else-if="movie" class="detail-layout">
      <div class="poster-section">
        <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title" class="poster-img" />
        <div v-else class="poster-placeholder">
          <span>{{ movie.title }}</span>
        </div>
      </div>

      <div class="info-section">
        <h1 class="movie-title">{{ movie.title }}</h1>
        <p v-if="movie.originalTitle && movie.originalTitle !== movie.title" class="orig-title">
          {{ movie.originalTitle }}
        </p>

        <div class="meta-info">
          <span v-if="movie.releaseDate">{{ movie.releaseDate.substring(0, 4) }}</span>
          <span v-if="movie.runtime">{{ movie.runtime }} 分钟</span>
          <span v-if="movie.genres">{{ movie.genres.replace(/,/g, ' · ') }}</span>
        </div>

        <div class="rating-display">
          <span class="score">{{ movie.voteAverage?.toFixed(1) }}</span>
          <div class="rating-right">
            <span class="vote-count">TMDb 评分 · {{ movie.voteCount }} 人评价</span>
          </div>
        </div>

        <p class="overview">{{ movie.overview || '暂无简介' }}</p>

        <div class="credits" v-if="movie.director || movie.cast">
          <div class="credit-item" v-if="movie.director">
            <span class="credit-label">导演</span>
            <span class="credit-value">{{ movie.director }}</span>
          </div>
          <div class="credit-item" v-if="movie.cast">
            <span class="credit-label">主演</span>
            <span class="credit-value">{{ movie.cast }}</span>
          </div>
        </div>

        <div class="actions" v-if="userStore.isLoggedIn">
          <el-button type="primary" size="large">标记看过</el-button>
          <el-button size="large">想看</el-button>
        </div>
        <div v-else class="login-hint">
          <router-link to="/login">登录</router-link> 后即可评分和标记
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { movieApi } from '@/api/movie'
import type { Movie } from '@/types'

const props = defineProps<{ id: string }>()
const router = useRouter()
const userStore = useUserStore()

const movie = ref<Movie | null>(null)
const loading = ref(true)
const error = ref('')

async function fetchDetail() {
  if (!props.id) return
  loading.value = true
  error.value = ''
  try {
    movie.value = await movieApi.getDetail(Number(props.id))
  } catch {
    error.value = '加载失败，请检查网络'
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped lang="scss">
.back-link { margin-bottom: 20px; }

.detail-layout {
  display: flex;
  gap: 40px;
}

.poster-section {
  flex-shrink: 0;
  width: 320px;

  .poster-img {
    width: 100%;
    border-radius: 12px;
    display: block;
  }

  .poster-placeholder {
    width: 100%;
    aspect-ratio: 2/3;
    background-color: var(--bg-card);
    border-radius: 12px;
    border: 1px solid var(--border-color);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-muted);
    font-size: 16px;
  }

  .skeleton-poster {
    animation: pulse 1.5s infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.info-section { flex: 1; }

.movie-title {
  font-size: 30px;
  font-weight: 700;
  margin-bottom: 4px;
}

.orig-title {
  color: var(--text-muted);
  font-size: 15px;
  margin-bottom: 16px;
}

.meta-info {
  display: flex;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;

  .score {
    font-size: 40px;
    font-weight: 700;
    color: var(--accent-gold);
    line-height: 1;
  }

  .rating-right {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .vote-count {
    color: var(--text-muted);
    font-size: 13px;
  }
}

.overview {
  line-height: 1.9;
  color: var(--text-secondary);
  font-size: 15px;
  margin-bottom: 24px;
}

.credits {
  margin-bottom: 24px;
}

.credit-item {
  margin-bottom: 10px;
  font-size: 14px;

  .credit-label {
    color: var(--text-muted);
    margin-right: 8px;
    font-weight: 500;
  }

  .credit-value {
    color: var(--text-primary);
  }
}

.actions {
  display: flex;
  gap: 12px;
}

.login-hint {
  color: var(--text-muted);
  font-size: 14px;
  a { color: var(--accent-gold); }
}

.error-state {
  text-align: center;
  padding: 80px 0;
  color: var(--text-muted);
}
</style>
