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
      <el-button @click="loadAll">重试</el-button>
    </div>

    <div v-else-if="movie" class="detail-layout">
      <div class="poster-section">
        <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title" class="poster-img" />
        <div v-else class="poster-placeholder"><span>{{ movie.title }}</span></div>
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
            <span v-if="platformStats.reviewCount" class="platform-rating">
              本站 {{ platformStats.averageRating }} 分 · {{ platformStats.reviewCount }} 人评分
            </span>
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

        <!-- 未登录 -->
        <div v-if="!userStore.isLoggedIn" class="login-hint">
          <router-link to="/login">登录</router-link> 后即可评分和标记
        </div>

        <!-- 已登录 -->
        <div v-else class="user-actions">
          <div class="status-btns">
            <el-button
              :type="myRecord?.status === 2 ? 'primary' : 'default'"
              size="large"
              @click="toggleStatus(2)"
            >
              看过
            </el-button>
            <el-button
              :type="myRecord?.status === 1 ? 'primary' : 'default'"
              size="large"
              @click="toggleStatus(1)"
            >
              想看
            </el-button>
          </div>

          <div v-if="myRecord?.status === 2" class="rating-area">
            <div class="rating-row">
              <span class="rating-label">我的评分</span>
              <RatingStars v-model="draftRating" />
              <span v-if="draftRating" class="rating-value">{{ draftRating }} 星</span>
            </div>
            <div class="comment-row">
              <el-input
                v-model="myComment"
                type="textarea"
                :rows="2"
                maxlength="500"
                show-word-limit
                placeholder="写一句短评..."
              />
            </div>
            <div class="submit-row">
              <el-button type="primary" size="small" :loading="saving" @click="submitRating">提交</el-button>
              <span v-if="savedMsg" class="saved-msg">{{ savedMsg }}</span>
            </div>
          </div>
        </div>

        <!-- 用户短评列表 -->
        <div v-if="reviews.length > 0" class="reviews-section">
          <h3 class="section-label">用户短评 ({{ reviews.length }})</h3>
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <span class="review-rating">{{ r.rating }}星</span>
            <span class="review-comment">{{ r.comment }}</span>
            <span class="review-date">{{ r.createTime?.substring(0, 10) }}</span>
          </div>
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
import { recordApi } from '@/api/record'
import RatingStars from '@/components/RatingStars.vue'
import type { Movie, WatchRecord } from '@/types'

const props = defineProps<{ id: string }>()
const router = useRouter()
const userStore = useUserStore()

const movie = ref<Movie | null>(null)
const myRecord = ref<WatchRecord | null>(null)
const platformStats = ref({ averageRating: 0, reviewCount: 0 })
const reviews = ref<WatchRecord[]>([])
const loading = ref(true)
const error = ref('')
const myRating = ref(0)
const myComment = ref('')
const draftRating = ref(0)
const savedMsg = ref('')
const saving = ref(false)

async function loadAll() {
  if (!props.id) return
  loading.value = true
  error.value = ''
  try {
    const tmdbId = Number(props.id)
    movie.value = await movieApi.getDetail(tmdbId)

    try { platformStats.value = await recordApi.getMovieStats(tmdbId) } catch { /* no-op */ }
    try { reviews.value = await recordApi.getMovieReviews(tmdbId) } catch { /* no-op */ }

    if (userStore.isLoggedIn) {
      try {
        myRecord.value = await recordApi.getMyRecordForMovie(tmdbId)
        if (myRecord.value) {
          myRating.value = myRecord.value.rating || 0
          draftRating.value = myRecord.value.rating || 0
          myComment.value = myRecord.value.comment || ''
        }
      } catch { /* no-op */ }
    }
  } catch {
    error.value = '加载失败，请检查网络'
  } finally {
    loading.value = false
  }
}

async function toggleStatus(status: number) {
  // 再次点击同一个状态 → 取消（删除记录）
  if (myRecord.value?.status === status) {
    saving.value = true
    try {
      await recordApi.deleteRecord(myRecord.value.id)
      myRecord.value = null
      myRating.value = 0
      myComment.value = ''
      draftRating.value = 0
      savedMsg.value = ''
      const tmdbId = Number(props.id)
      platformStats.value = await recordApi.getMovieStats(tmdbId)
      reviews.value = await recordApi.getMovieReviews(tmdbId)
    } catch { /* handled in interceptor */ } finally {
      saving.value = false
    }
    return
  }

  saving.value = true
  try {
    const r = await recordApi.saveRecord({
      tmdbId: Number(props.id),
      status,
      rating: myRecord.value?.rating,
      comment: myRecord.value?.comment,
    })
    myRecord.value = r
    myRating.value = r.rating || 0
    draftRating.value = r.rating || 0
    myComment.value = r.comment || ''

    const tmdbId = Number(props.id)
    platformStats.value = await recordApi.getMovieStats(tmdbId)
    reviews.value = await recordApi.getMovieReviews(tmdbId)
  } catch { /* handled in interceptor */ } finally {
    saving.value = false
  }
}

async function submitRating() {
  if (!myRecord.value) return
  saving.value = true
  savedMsg.value = ''
  try {
    await recordApi.saveRecord({
      tmdbId: Number(props.id),
      status: myRecord.value.status,
      rating: draftRating.value || myRating.value,
      comment: myComment.value,
    })
    myRecord.value.rating = draftRating.value || myRating.value
    myRecord.value.comment = myComment.value
    myRating.value = myRecord.value.rating
    savedMsg.value = '已保存'
    platformStats.value = await recordApi.getMovieStats(Number(props.id))
    reviews.value = await recordApi.getMovieReviews(Number(props.id))
  } catch { savedMsg.value = '保存失败' } finally {
    saving.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped lang="scss">
.back-link { margin-bottom: 20px; }

.detail-layout { display: flex; gap: 40px; }

.poster-section {
  flex-shrink: 0; width: 320px;
  .poster-img { width: 100%; border-radius: 12px; display: block; }
  .poster-placeholder {
    width: 100%; aspect-ratio: 2/3; background-color: var(--bg-card);
    border-radius: 12px; border: 1px solid var(--border-color);
    display: flex; align-items: center; justify-content: center;
    color: var(--text-muted); font-size: 16px;
  }
  .skeleton-poster { animation: pulse 1.5s infinite; }
}

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }

.info-section { flex: 1; }

.movie-title { font-size: 30px; font-weight: 700; margin-bottom: 4px; }
.orig-title { color: var(--text-muted); font-size: 15px; margin-bottom: 16px; }

.meta-info {
  display: flex; gap: 16px; color: var(--text-secondary);
  font-size: 14px; margin-bottom: 20px; flex-wrap: wrap;
}

.rating-display {
  display: flex; align-items: center; gap: 16px; margin-bottom: 24px;
  .score { font-size: 40px; font-weight: 700; color: var(--accent-gold); line-height: 1; }
  .rating-right { display: flex; flex-direction: column; gap: 2px; }
  .vote-count { color: var(--text-muted); font-size: 13px; }
  .platform-rating { color: var(--accent-gold); font-size: 13px; }
}

.overview { line-height: 1.9; color: var(--text-secondary); font-size: 15px; margin-bottom: 24px; }

.credits { margin-bottom: 24px; }
.credit-item {
  margin-bottom: 10px; font-size: 14px;
  .credit-label { color: var(--text-muted); margin-right: 8px; font-weight: 500; }
  .credit-value { color: var(--text-primary); }
}

.login-hint { color: var(--text-muted); font-size: 14px; a { color: var(--accent-gold); } }

.user-actions {
  margin-bottom: 24px;
}

.status-btns { display: flex; gap: 12px; align-items: center; margin-bottom: 20px; }

.rating-area {
  padding: 20px; background-color: var(--bg-card); border-radius: 10px;
  border: 1px solid var(--border-color);
}

.rating-row {
  display: flex; align-items: center; gap: 12px; margin-bottom: 12px;
  .rating-label { font-size: 14px; color: var(--text-secondary); }
  .rating-value { font-size: 14px; color: var(--accent-gold); font-weight: 600; }
}

.comment-row { margin-bottom: 12px; }

.submit-row {
  display: flex; align-items: center; gap: 12px;
}

.saved-msg { font-size: 12px; color: var(--text-muted); margin: 0; }

.reviews-section {
  margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--border-color);
}
.section-label { font-size: 16px; font-weight: 600; margin-bottom: 16px; }

.review-item {
  display: flex; align-items: center; gap: 12px; padding: 10px 0;
  border-bottom: 1px solid var(--border-color); font-size: 14px;
  .review-rating { color: var(--accent-gold); font-weight: 600; flex-shrink: 0; }
  .review-comment { flex: 1; color: var(--text-secondary); }
  .review-date { color: var(--text-muted); font-size: 12px; flex-shrink: 0; }
}

.error-state { text-align: center; padding: 80px 0; color: var(--text-muted); }
</style>
