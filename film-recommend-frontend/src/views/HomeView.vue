<template>
  <div class="home">
    <section class="recommend-section">
      <h2 class="section-title">猜你喜欢</h2>
      <div class="recommend-scroll">
        <div v-if="!userStore.isLoggedIn" class="empty-hint">
          登录后获得专属推荐，<router-link to="/login">立即登录</router-link>
        </div>
        <p v-else class="empty-hint">还没有观影记录，去发现页标记几部电影吧</p>
      </div>
    </section>

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
import { useUserStore } from '@/stores/user'
import { movieApi } from '@/api/movie'
import { GENRE_LIST } from '@/utils/constants'
import MovieCard from '@/components/MovieCard.vue'
import type { Movie } from '@/types'

const userStore = useUserStore()
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

function toggleGenre(id: number) {
  const idx = selectedGenres.value.indexOf(id)
  if (idx >= 0) {
    selectedGenres.value.splice(idx, 1)
  } else {
    selectedGenres.value.push(id)
  }
  resetAndFetch()
}

function clearAll() {
  selectedGenres.value = []
  year.value = ''
  resetAndFetch()
}

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
    if (currentPage.value > totalPages.value) {
      finished.value = true
    }
  } catch {
    error.value = '加载失败，请检查网络后重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMovies()

  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !loading.value && !finished.value) {
      fetchMovies()
    }
  }, { rootMargin: '200px' })

  if (sentinel.value) {
    observer.observe(sentinel.value)
  }
})

onBeforeUnmount(() => {
  observer?.disconnect()
})
</script>

<style scoped lang="scss">
.home {
  .section-title {
    font-size: 22px;
    font-weight: 700;
    position: relative;
    &::before {
      content: '';
      position: absolute;
      left: -12px;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 20px;
      background-color: var(--accent-gold);
      border-radius: 2px;
    }
  }
}

.recommend-section {
  margin-bottom: 40px;
}

.recommend-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
  margin-top: 20px;
  &::-webkit-scrollbar { height: 4px; }
  &::-webkit-scrollbar-thumb { background-color: var(--border-color); border-radius: 2px; }
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.filter-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background-color: var(--bg-card);
  border-radius: 10px;
  border: 1px solid var(--border-color);
}

.genre-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  flex: 1;
}

.chip {
  padding: 4px 12px;
  border-radius: 14px;
  cursor: pointer;
  font-size: 12px;
  color: var(--text-secondary);
  background-color: var(--bg-secondary);
  border: 1px solid transparent;
  transition: all 0.2s;
  user-select: none;

  &:hover {
    color: var(--text-primary);
    border-color: var(--accent-gold);
  }

  &.active {
    background-color: var(--accent-gold);
    color: #1a1a2e;
    font-weight: 600;
  }
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  margin-bottom: 20px;

  @media (max-width: 1200px) { grid-template-columns: repeat(4, 1fr); }
  @media (max-width: 768px)  { grid-template-columns: repeat(3, 1fr); }
  @media (max-width: 576px)  { grid-template-columns: repeat(2, 1fr); }
}

.skeleton-card {
  .skel-poster {
    aspect-ratio: 2/3;
    background-color: var(--bg-card);
    border-radius: 8px;
    animation: pulse 1.5s infinite;
  }
  .skel-line {
    height: 14px;
    margin-top: 8px;
    background-color: var(--bg-card);
    border-radius: 4px;
    width: 60%;
    animation: pulse 1.5s infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.sentinel { height: 1px; }

.empty-hint {
  color: var(--text-muted);
  font-size: 14px;
  padding: 20px 0;
}

.done-hint {
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
  padding: 16px 0;
}
</style>
