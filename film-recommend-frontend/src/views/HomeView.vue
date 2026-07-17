<template>
  <div class="home">
    <!-- 猜你喜欢 -->
    <section class="recommend-section" v-if="!recError || recMovies.length > 0">
      <div class="section-header">
        <h2 class="section-title">✨ 猜你喜欢</h2>
        <el-button type="primary" round @click="showRandomBox = true">🎲 随机盲盒</el-button>
      </div>

      <p v-if="recReason" class="rec-reason">{{ recReason }}</p>

      <div v-if="recLoading" class="recommend-scroll">
        <div v-for="n in 5" :key="n" class="skel-h-card">
          <div class="skel-poster"></div>
          <div class="skel-line"></div>
        </div>
      </div>

      <div v-else-if="recError && recMovies.length === 0" class="empty-hint">{{ recError }}</div>

      <div v-else class="recommend-scroll">
        <div
          v-for="movie in recMovies"
          :key="movie.tmdbId"
          class="rec-card"
          @click="$router.push(`/movie/${movie.tmdbId}`)"
        >
          <div class="rec-poster">
            <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title" />
            <div v-else class="poster-ph">{{ movie.title?.charAt(0) }}</div>
            <div v-if="movie.matchScore" class="match-tag">
              {{ movie.matchScore }}% 匹配 · {{ movie.matchGenre }}
            </div>
          </div>
          <div class="rec-info">
            <h4 class="rec-title">{{ movie.title }}</h4>
            <span class="rec-rating">{{ movie.voteAverage?.toFixed(1) }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 随机盲盒弹窗 -->
    <el-dialog v-model="showRandomBox" :title="null" width="420px" center :close-on-click-modal="false" @opened="fetchRandom">
      <div v-if="randLoading" class="rand-loading">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <p>正在为你挑选...</p>
      </div>
      <div v-else-if="randMovie" class="rand-result">
        <img v-if="randMovie.posterUrl" :src="randMovie.posterUrl" :alt="randMovie.title" class="rand-poster" />
        <div v-else class="rand-placeholder">{{ randMovie.title }}</div>
        <h3 class="rand-title">{{ randMovie.title }}</h3>
        <p class="rand-overview">{{ randMovie.overview?.substring(0, 120) }}{{ randMovie.overview && randMovie.overview.length > 120 ? '...' : '' }}</p>
        <div class="rand-actions">
          <el-button @click="fetchRandom" :loading="randLoading">换一个</el-button>
          <el-button type="primary" @click="goToDetail">查看详情 →</el-button>
        </div>
      </div>
      <div v-else class="rand-loading">
        <p>暂无推荐，去标记几部电影吧</p>
      </div>
    </el-dialog>

    <!-- 热门电影 -->
    <section class="popular-section">
      <div class="section-header">
        <h2 class="section-title">热门电影</h2>
        <el-radio-group v-model="sortBy" size="small" @change="resetAndFetch">
          <el-radio-button value="popularity.desc">热门</el-radio-button>
          <el-radio-button value="primary_release_date.desc">最新</el-radio-button>
          <el-radio-button value="vote_average.desc">评分最高</el-radio-button>
        </el-radio-group>
      </div>

      <div class="filter-bar">
        <div class="genre-chips">
          <span
            v-for="genre in GENRE_LIST"
            :key="genre.id"
            class="chip"
            :class="{ active: selectedGenres.includes(genre.id) }"
            @click="toggleGenre(genre.id)"
          >{{ genre.name }}</span>
        </div>
        <div class="filter-right">
          <el-select v-model="year" placeholder="年份" clearable size="small" @change="resetAndFetch" style="width: 100px;">
            <el-option v-for="y in yearOptions" :key="y" :label="String(y)" :value="String(y)" />
          </el-select>
          <el-button v-if="selectedGenres.length || year" size="small" text type="warning" @click="clearAll">
            清除
          </el-button>
        </div>
      </div>

      <div v-if="error && movies.length === 0" class="empty-hint">
        {{ error }}
        <el-button size="small" @click="resetAndFetch">重试</el-button>
      </div>

      <div class="movie-grid">
        <MovieCard v-for="movie in movies" :key="movie.id" :movie="movie" />
      </div>

      <div v-if="loading" class="movie-grid">
        <div v-for="n in 5" :key="n" class="skeleton-card">
          <div class="skel-poster"></div>
          <div class="skel-line"></div>
        </div>
      </div>

      <div ref="sentinel" class="sentinel"></div>

      <p v-if="finished && movies.length > 0" class="done-hint">已加载全部</p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { movieApi } from '@/api/movie'
import { recommendApi } from '@/api/recommend'
import { GENRE_LIST } from '@/utils/constants'
import MovieCard from '@/components/MovieCard.vue'
import type { Movie } from '@/types'

const router = useRouter()
const userStore = useUserStore()

// === 推荐区域 ===
const recMovies = ref<Movie[]>([])
const recReason = ref('')
const recLoading = ref(true)
const recError = ref('')

// === 盲盒 ===
const showRandomBox = ref(false)
const randMovie = ref<Movie | null>(null)
const randLoading = ref(false)

// === 热门区域 ===
const sortBy = ref('popularity.desc')
const selectedGenres = ref<number[]>([])
const year = ref('')
const movies = ref<Movie[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(1)
const totalPages = ref(0)
const finished = ref(false)
const sentinel = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

const yearOptions = computed(() => {
  const years = []
  for (let y = new Date().getFullYear(); y >= 1960; y--) {
    years.push(y)
  }
  return years
})

async function fetchRecommendations() {
  recLoading.value = true
  recError.value = ''
  try {
    const res = await recommendApi.getRecommendations()
    recMovies.value = res.movies
    recReason.value = res.reason
  } catch {
    recError.value = '推荐加载失败'
  } finally {
    recLoading.value = false
  }
}

async function fetchRandom() {
  randLoading.value = true
  randMovie.value = null
  try {
    randMovie.value = await recommendApi.getRandomPick()
  } catch { /* no-op */ } finally {
    randLoading.value = false
  }
}

function goToDetail() {
  if (randMovie.value) {
    showRandomBox.value = false
    router.push(`/movie/${randMovie.value.tmdbId}`)
  }
}

function toggleGenre(id: number) {
  const idx = selectedGenres.value.indexOf(id)
  if (idx >= 0) { selectedGenres.value.splice(idx, 1) }
  else { selectedGenres.value.push(id) }
  resetAndFetch()
}

function clearAll() { selectedGenres.value = []; year.value = ''; resetAndFetch() }

function resetAndFetch() {
  movies.value = []
  currentPage.value = 1
  totalPages.value = 0
  finished.value = false
  error.value = ''
  fetchMovies()
}

async function fetchMovies() {
  if (loading.value || finished.value) return
  loading.value = true
  error.value = ''
  try {
    const genreParam = selectedGenres.value.length ? selectedGenres.value.join(',') : undefined
    const res = await movieApi.discover({
      genres: genreParam,
      year: year.value || undefined,
      page: currentPage.value,
      sortBy: sortBy.value,
    })
    movies.value.push(...res.records)
    totalPages.value = res.pages || 1
    currentPage.value++
    if (currentPage.value > totalPages.value) finished.value = true
  } catch {
    error.value = '加载失败，请检查网络后重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecommendations()
  fetchMovies()

  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !loading.value && !finished.value) {
      fetchMovies()
    }
  }, { rootMargin: '200px' })

  if (sentinel.value) observer.observe(sentinel.value)
})

onBeforeUnmount(() => observer?.disconnect())
</script>

<style scoped lang="scss">
.home {
  .section-title {
    font-size: 22px;
    font-weight: 700;
    position: relative;
    &::before {
      content: '';
      position: absolute; left: -12px; top: 50%;
      transform: translateY(-50%);
      width: 4px; height: 20px;
      background-color: var(--accent-gold); border-radius: 2px;
    }
  }
}

.recommend-section { margin-bottom: 40px; }

.section-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 12px;
}

.rec-reason {
  color: var(--text-secondary); font-size: 13px; margin-bottom: 16px;
}

.recommend-scroll {
  display: flex; gap: 16px; overflow-x: auto; padding-bottom: 8px;
  &::-webkit-scrollbar { height: 4px; }
  &::-webkit-scrollbar-thumb { background-color: var(--border-color); border-radius: 2px; }
}

.rec-card {
  flex-shrink: 0; width: 150px; cursor: pointer;
  border-radius: 10px; overflow: hidden;
  background-color: var(--bg-card); border: 1px solid var(--border-color);
  transition: transform 0.2s;
  &:hover { transform: translateY(-4px); }
}

.rec-poster {
  position: relative; aspect-ratio: 2/3; background-color: var(--bg-secondary);
  img { width: 100%; height: 100%; object-fit: cover; display: block; }
  .poster-ph {
    width: 100%; height: 100%; display: flex; align-items: center;
    justify-content: center; color: var(--text-muted); font-size: 28px;
  }
}

.match-tag {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.8));
  color: var(--accent-gold); font-size: 11px; font-weight: 600;
  padding: 20px 8px 6px;
}

.rec-info {
  padding: 10px;
  .rec-title { font-size: 13px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 2px; }
  .rec-rating { font-size: 12px; color: var(--accent-gold); }
}

.skel-h-card {
  flex-shrink: 0; width: 150px;
  .skel-poster {
    aspect-ratio: 2/3; background-color: var(--bg-card);
    border-radius: 10px; animation: pulse 1.5s infinite;
  }
  .skel-line {
    height: 12px; margin-top: 8px; background-color: var(--bg-card);
    border-radius: 4px; width: 60%; animation: pulse 1.5s infinite;
  }
}

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }

// 盲盒弹窗
.rand-loading {
  text-align: center; padding: 40px; color: var(--text-muted);
  .loading-icon { font-size: 40px; animation: spin 1s linear infinite; }
  p { margin-top: 12px; }
}
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.rand-result { text-align: center; }
.rand-poster {
  width: 200px; border-radius: 12px; display: block; margin: 0 auto 20px;
}
.rand-placeholder {
  width: 200px; aspect-ratio: 2/3; margin: 0 auto 20px;
  background-color: var(--bg-secondary); border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-muted);
}
.rand-title { font-size: 20px; font-weight: 700; margin-bottom: 8px; }
.rand-overview { color: var(--text-secondary); font-size: 13px; line-height: 1.6; margin-bottom: 24px; }
.rand-actions { display: flex; gap: 12px; justify-content: center; }

// 热门
.filter-bar {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 16px; margin-bottom: 24px; padding: 16px;
  background-color: var(--bg-card); border-radius: 10px; border: 1px solid var(--border-color);
}
.genre-chips { display: flex; flex-wrap: wrap; gap: 6px; flex: 1; }
.chip {
  padding: 4px 12px; border-radius: 14px; cursor: pointer; font-size: 12px;
  color: var(--text-secondary); background-color: var(--bg-secondary);
  border: 1px solid transparent; transition: all 0.2s; user-select: none;
  &:hover { color: var(--text-primary); border-color: var(--accent-gold); }
  &.active { background-color: var(--accent-gold); color: #1a1a2e; font-weight: 600; }
}
.filter-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }

.movie-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 20px; margin-bottom: 20px;
  @media (max-width: 1200px) { grid-template-columns: repeat(4, 1fr); }
  @media (max-width: 768px)  { grid-template-columns: repeat(3, 1fr); }
  @media (max-width: 576px)  { grid-template-columns: repeat(2, 1fr); }
}

.skeleton-card {
  .skel-poster {
    aspect-ratio: 2/3; background-color: var(--bg-card);
    border-radius: 8px; animation: pulse 1.5s infinite;
  }
  .skel-line {
    height: 14px; margin-top: 8px; background-color: var(--bg-card);
    border-radius: 4px; width: 60%; animation: pulse 1.5s infinite;
  }
}

.sentinel { height: 1px; }

.empty-hint { color: var(--text-muted); font-size: 14px; padding: 20px 0; }
.done-hint { text-align: center; color: var(--text-muted); font-size: 13px; padding: 16px 0; }
</style>
