<template>
  <div class="discover">
    <div class="discover-header">
      <h2 class="page-title">{{ pageTitle }}</h2>
      <el-radio-group v-model="sortBy" size="small" @change="resetAndFetch">
        <el-radio-button value="popularity.desc">热门</el-radio-button>
        <el-radio-button value="primary_release_date.desc">最新</el-radio-button>
        <el-radio-button value="vote_average.desc">评分最高</el-radio-button>
      </el-radio-group>
    </div>

    <div class="discover-body">
      <aside class="genre-sidebar">
        <h3>筛选</h3>
        <div class="filter-row">
          <el-select v-model="year" placeholder="上映年份" clearable size="small" @change="resetAndFetch" style="width:100%">
            <el-option v-for="y in yearOptions" :key="y" :label="String(y)" :value="String(y)" />
          </el-select>
        </div>
        <p class="filter-label">类型</p>
        <div class="genre-list">
          <span
            v-for="genre in GENRE_LIST"
            :key="genre.id"
            class="genre-tag"
            :class="{ active: selectedGenres.includes(genre.id) }"
            @click="toggleGenre(genre.id)"
          >{{ genre.name }}</span>
        </div>
        <el-button v-if="selectedGenres.length || year" size="small" text type="warning" @click="clearAll">
          清除全部
        </el-button>
      </aside>

      <div class="main-area">
        <div v-if="error && movies.length === 0" class="empty-state">
          <p>{{ error }}</p>
          <el-button size="small" @click="resetAndFetch">重试</el-button>
        </div>

        <div v-else-if="!loading && !movies.length" class="empty-state">
          <p>没有找到匹配的电影</p>
        </div>

        <div v-else class="movie-grid">
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
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { movieApi } from '@/api/movie'
import { GENRE_LIST } from '@/utils/constants'
import MovieCard from '@/components/MovieCard.vue'
import type { Movie } from '@/types'

const route = useRoute()

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

const pageTitle = computed(() => {
  const kw = route.query.keyword as string
  return kw ? `搜索: ${kw}` : '发现电影'
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
    const keyword = route.query.keyword as string
    let res
    if (keyword) {
      res = await movieApi.search(keyword, currentPage.value)
    } else {
      const genreParam = selectedGenres.value.length ? selectedGenres.value.join(',') : undefined
      res = await movieApi.discover({
        genres: genreParam,
        year: year.value || undefined,
        page: currentPage.value,
        sortBy: sortBy.value,
      })
    }
    movies.value.push(...res.records)
    totalPages.value = res.pages || 1
    currentPage.value++
    if (currentPage.value > totalPages.value) {
      finished.value = true
    }
  } catch {
    error.value = '加载失败，请检查网络'
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

watch(() => route.query.keyword, () => {
  resetAndFetch()
})
</script>

<style scoped lang="scss">
.discover-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
}

.discover-body {
  display: flex;
  gap: 24px;
}

.genre-sidebar {
  flex-shrink: 0;
  width: 180px;
  h3 {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 12px;
    color: var(--text-secondary);
  }
}

.filter-row {
  margin-bottom: 16px;
}

.filter-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.genre-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}

.genre-tag {
  display: block;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 0.2s;
  &:hover { background-color: var(--bg-card-hover); color: var(--text-primary); }
  &.active {
    background-color: var(--accent-gold);
    color: #1a1a2e;
    font-weight: 600;
  }
}

.main-area { flex: 1; min-width: 0; }

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

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-muted);
  grid-column: 1 / -1;
}

.done-hint {
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
  padding: 16px 0;
}
</style>
