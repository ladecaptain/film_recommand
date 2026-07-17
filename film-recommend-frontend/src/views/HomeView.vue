<template>
  <div class="home">
    <!-- 猜你喜欢 -->
    <section class="recommend-section" v-if="!recError || recMovies.length > 0">
      <div class="section-header">
        <h2 class="section-title">✨ 猜你喜欢</h2>
        <el-button type="primary" round @click="openBlindBox">🎲 随机盲盒</el-button>
      </div>

      <p v-if="recReason" class="rec-reason">{{ recReason }}</p>

      <div v-if="recLoading" class="carousel-wrap">
        <div class="carousel-track">
          <div v-for="n in 5" :key="n" class="skel-h-card">
            <div class="skel-poster"></div>
            <div class="skel-line"></div>
          </div>
        </div>
      </div>

      <div v-else-if="recError && recMovies.length === 0" class="empty-hint">{{ recError }}</div>

      <div v-else class="carousel-wrap">
        <div class="carousel-stage">
          <button class="carousel-arrow left" @click="prevSlide">‹</button>

          <div class="carousel-track" ref="carouselTrack">
            <div
              v-for="item in displaySlides"
              :key="item.movie.tmdbId"
              class="carousel-item"
              :class="`pos-${item.pos}`"
              @click="$router.push(`/movie/${item.movie.tmdbId}`)"
            >
              <div class="ci-poster">
                <img v-if="item.movie.posterUrl" :src="item.movie.posterUrl" :alt="item.movie.title" />
                <div v-else class="ci-placeholder">{{ item.movie.title?.charAt(0) }}</div>
                <div v-if="item.pos === 0 && item.movie.matchScore" class="ci-match">
                  {{ item.movie.matchScore }}% 匹配 · {{ item.movie.matchGenre }}
                </div>
              </div>
              <div class="ci-info" v-if="Math.abs(item.pos) <= 1">
                <h3 class="ci-title" v-if="item.pos === 0">{{ item.movie.title }}</h3>
                <span class="ci-rating" v-if="item.pos === 0">{{ item.movie.voteAverage?.toFixed(1) }}</span>
                <p class="ci-overview" v-if="item.pos === 0">{{ item.movie.overview?.substring(0, 60) }}{{ item.movie.overview && item.movie.overview.length > 60 ? '...' : '' }}</p>
              </div>
            </div>
          </div>

          <button class="carousel-arrow right" @click="nextSlide">›</button>
        </div>

        <div class="carousel-dots">
          <span
            v-for="(movie, i) in recMovies"
            :key="movie.tmdbId"
            class="dot"
            :class="{ active: i === activeIndex }"
            @click="goToSlide(i)"
          ></span>
        </div>
      </div>
    </section>

    <!-- 随机盲盒 - 全屏遮罩 -->
    <Teleport to="body">
      <Transition name="blindbox">
        <div v-if="showRandomBox" class="blindbox-overlay" @click.self="closeBlindBox">
          <button class="bb-close" @click="closeBlindBox">✕</button>

          <!-- 阶段1：抽取中 -->
          <div v-if="bbPhase === 'spinning'" class="bb-spinning">
            <p class="bb-dice">🎲</p>
            <p class="bb-title">天意正在为你决定...</p>
            <div class="bb-swirl">
              <span v-for="i in 5" :key="i" class="swirl-dot" :style="{ animationDelay: i * 0.15 + 's' }"></span>
            </div>
          </div>

          <!-- 阶段2：卡片翻转展示 -->
          <div v-else-if="bbPhase === 'reveal' && randMovie" class="bb-card" :class="{ flipped: bbFlipped }">
            <div class="bb-card-inner">
              <div class="bb-card-front">
                <p class="bb-question">?</p>
              </div>
              <div class="bb-card-back">
                <img v-if="randMovie.posterUrl" :src="randMovie.posterUrl" :alt="randMovie.title" class="bb-poster" />
                <div v-else class="bb-placeholder">{{ randMovie.title }}</div>
                <h3 class="bb-movie-title">{{ randMovie.title }}</h3>
                <div class="bb-meta">
                  <span class="bb-score">{{ randMovie.voteAverage?.toFixed(1) }} 分</span>
                  <span v-if="randMovie.releaseDate">{{ randMovie.releaseDate?.substring(0, 4) }}</span>
                </div>
                <p class="bb-overview">{{ randMovie.overview?.substring(0, 150) }}{{ randMovie.overview && randMovie.overview.length > 150 ? '...' : '' }}</p>
                <div class="bb-actions">
                  <el-button round size="large" @click="fetchRandom">🔄 换一个</el-button>
                  <el-button type="primary" round size="large" @click="goToDetail">查看详情 →</el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 空结果 -->
          <div v-else-if="bbPhase === 'reveal' && !randMovie" class="bb-spinning">
            <p class="bb-title">暂无推荐</p>
            <p class="bb-hint">去标记几部喜欢的电影吧</p>
          </div>
        </div>
      </Transition>
    </Teleport>

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
const activeIndex = ref(0)

const displaySlides = computed(() => {
  const len = recMovies.value.length
  if (len === 0) return []
  const slides: { movie: Movie; pos: number }[] = []
  for (let offset = -5; offset <= 4; offset++) {
    let idx = (activeIndex.value + offset) % len
    if (idx < 0) idx += len
    slides.push({ movie: recMovies.value[idx], pos: offset })
  }
  return slides
})

function prevSlide() { activeIndex.value = (activeIndex.value - 1 + recMovies.value.length) % recMovies.value.length; resetTimer() }
function nextSlide() { activeIndex.value = (activeIndex.value + 1) % recMovies.value.length; resetTimer() }
function goToSlide(i: number) { activeIndex.value = i; resetTimer() }

let carouselTimer: ReturnType<typeof setInterval> | null = null
function resetTimer() {
  if (carouselTimer) clearInterval(carouselTimer)
  carouselTimer = setInterval(() => { nextSlide() }, 5000)
}

// === 盲盒 ===
const showRandomBox = ref(false)
const randMovie = ref<Movie | null>(null)
const bbPhase = ref<'spinning' | 'reveal'>('spinning')
const bbFlipped = ref(false)
const shownIds = new Set<number>()

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
    activeIndex.value = 0
    if (res.movies.length > 1) resetTimer()
  } catch {
    recError.value = '推荐加载失败'
  } finally {
    recLoading.value = false
  }
}

async function openBlindBox() {
  if (showRandomBox.value) return
  showRandomBox.value = true
  bbPhase.value = 'spinning'
  bbFlipped.value = false
  randMovie.value = null

  try { randMovie.value = await recommendApi.getRandomPick([...shownIds]) } catch { /* no-op */ }
  if (randMovie.value?.tmdbId) shownIds.add(randMovie.value.tmdbId)

  setTimeout(() => {
    bbPhase.value = 'reveal'
    requestAnimationFrame(() => {
      requestAnimationFrame(() => { bbFlipped.value = true })
    })
  }, 1500)
}

async function fetchRandom() {
  bbPhase.value = 'spinning'
  bbFlipped.value = false
  randMovie.value = null

  try { randMovie.value = await recommendApi.getRandomPick([...shownIds]) } catch { /* no-op */ }
  if (randMovie.value?.tmdbId) shownIds.add(randMovie.value.tmdbId)

  setTimeout(() => {
    bbPhase.value = 'reveal'
    requestAnimationFrame(() => {
      requestAnimationFrame(() => { bbFlipped.value = true })
    })
  }, 1200)
}

function goToDetail() {
  if (randMovie.value) {
    closeBlindBox()
    router.push(`/movie/${randMovie.value.tmdbId}`)
  }
}

function closeBlindBox() {
  showRandomBox.value = false
  shownIds.clear()
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

onBeforeUnmount(() => {
  observer?.disconnect()
  if (carouselTimer) clearInterval(carouselTimer)
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
  color: var(--text-secondary); font-size: 13px; margin-bottom: 20px;
}

// === 推荐轮播 ===
.carousel-wrap {
  display: flex; flex-direction: column; align-items: center;
  padding: 0 40px; margin-bottom: 40px;
}

.carousel-stage {
  position: relative;
  height: 400px; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  width: 100%;
}

.carousel-track {
  display: flex; align-items: center; justify-content: center;
}

.carousel-item {
  flex-shrink: 0;
  border-radius: 10px; overflow: hidden;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  margin: 0 2px;

  &.pos-0 {
    width: 220px; z-index: 5; opacity: 1; filter: none;
    border-color: var(--accent-gold);
    box-shadow: 0 8px 32px rgba(230, 185, 30, 0.2);
  }
  &.pos--1, &.pos-1 {
    width: 180px; z-index: 4; opacity: 0.9; filter: brightness(0.8);
  }
  &.pos--2, &.pos-2 {
    width: 145px; z-index: 3; opacity: 0.7; filter: brightness(0.65);
  }
  &.pos--3, &.pos-3 {
    width: 115px; z-index: 2; opacity: 0.5; filter: brightness(0.5);
  }
  &.pos--4, &.pos-4 {
    width: 90px; z-index: 1; opacity: 0.35; filter: brightness(0.35);
  }
  &.pos--5 {
    width: 70px; z-index: 0; opacity: 0.2; filter: brightness(0.2);
  }

  &:hover {
    opacity: 1 !important; filter: none !important;
    z-index: 10 !important;
  }
}

.ci-poster {
  position: relative; background-color: var(--bg-secondary);
  img { width: 100%; display: block; }
  .ci-placeholder {
    aspect-ratio: 2/3; display: flex; align-items: center;
    justify-content: center; color: var(--text-muted); font-size: 14px;
  }
}

.ci-match {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.85));
  color: var(--accent-gold); font-size: 11px; font-weight: 600;
  padding: 20px 10px 6px;
}

.ci-info {
  padding: 12px;
  transition: opacity 0.3s;
  .ci-title { font-size: 15px; font-weight: 700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .ci-rating { font-size: 14px; color: var(--accent-gold); font-weight: 600; margin: 2px 0; }
  .ci-overview {
    font-size: 11px; color: var(--text-muted); line-height: 1.5; margin-top: 4px;
    display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

// 箭头
.carousel-arrow {
  position: absolute; top: 50%; transform: translateY(-50%);
  background: rgba(31, 43, 71, 0.9); border: 1px solid var(--border-color);
  color: var(--text-primary); font-size: 28px; width: 40px; height: 40px;
  border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.2s; z-index: 20;
  &:hover { background: var(--accent-gold); color: #1a1a2e; border-color: var(--accent-gold); }
  &.left { left: 0; }
  &.right { right: 0; }
}

// 圆点
.carousel-dots {
  display: flex; gap: 8px; margin-top: 10px; 
}
.dot {
  width: 8px; height: 8px; border-radius: 50%;
  background-color: var(--border-color); cursor: pointer;
  transition: all 0.3s;
  &.active { background-color: var(--accent-gold); width: 24px; border-radius: 4px; }
}

// 骨架
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

// 盲盒全屏遮罩
.blindbox-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0, 0, 0, 0.92);
  display: flex; align-items: center; justify-content: center;
  flex-direction: column;
}

.bb-close {
  position: absolute; top: 24px; right: 32px;
  background: none; border: none; color: var(--text-muted);
  font-size: 28px; cursor: pointer; z-index: 1;
  &:hover { color: #fff; }
}

// 进入/离开动画
.blindbox-enter-active { transition: opacity 0.4s; }
.blindbox-leave-active { transition: opacity 0.3s; }
.blindbox-enter-from, .blindbox-leave-to { opacity: 0; }

// 抽取中
.bb-spinning { text-align: center; }
.bb-dice {
  font-size: 80px; animation: dice-bounce 0.6s infinite alternate;
}
@keyframes dice-bounce {
  from { transform: translateY(0) rotate(-10deg); }
  to { transform: translateY(-16px) rotate(10deg); }
}
.bb-title {
  font-size: 24px; font-weight: 700; color: var(--accent-gold); margin: 24px 0;
  animation: glow-pulse 1.5s infinite;
}
@keyframes glow-pulse {
  0%, 100% { opacity: 1; text-shadow: 0 0 8px rgba(230, 185, 30, 0.3); }
  50% { opacity: 0.7; text-shadow: 0 0 20px rgba(230, 185, 30, 0.6); }
}
.bb-hint { color: var(--text-muted); font-size: 15px; margin-top: 12px; }

.bb-swirl {
  display: flex; gap: 10px; justify-content: center; margin-top: 20px;
}
.swirl-dot {
  width: 12px; height: 12px; border-radius: 50%;
  background-color: var(--accent-gold);
  animation: dot-fade 1s infinite alternate;
}
@keyframes dot-fade {
  from { opacity: 1; transform: scale(1); }
  to { opacity: 0.2; transform: scale(0.6); }
}

// 卡片翻转
.bb-card {
  perspective: 1000px;
  width: 300px; height: 480px;
}
.bb-card-inner {
  position: relative; width: 100%; height: 100%;
  transition: transform 0.7s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
}
.bb-card.flipped .bb-card-inner { transform: rotateY(180deg); }

.bb-card-front, .bb-card-back {
  position: absolute; inset: 0;
  backface-visibility: hidden;
  border-radius: 16px;
  overflow: hidden;
}
.bb-card-front {
  background: linear-gradient(135deg, var(--bg-card), var(--bg-secondary));
  border: 2px solid var(--accent-gold);
  display: flex; align-items: center; justify-content: center;
}
.bb-question {
  font-size: 80px; color: var(--accent-gold); font-weight: 700;
  animation: glow-pulse 1.5s infinite;
}
.bb-card-back {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  transform: rotateY(180deg);
  display: flex; flex-direction: column; align-items: center;
  padding: 24px; text-align: center;
}
.bb-poster {
  width: 160px; border-radius: 10px; display: block;
  flex-shrink: 0;
}
.bb-placeholder {
  width: 160px; height: 240px; border-radius: 10px; flex-shrink: 0;
  background-color: var(--bg-secondary);
  display: flex; align-items: center; justify-content: center;
  color: var(--text-muted); font-size: 14px;
}
.bb-movie-title {
  font-size: 17px; font-weight: 700; margin-top: 14px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  max-width: 100%;
}
.bb-meta {
  display: flex; gap: 12px; margin-top: 6px;
  font-size: 13px; color: var(--text-muted); flex-shrink: 0;
  .bb-score { color: var(--accent-gold); font-weight: 600; }
}
.bb-overview {
  font-size: 12px; color: var(--text-secondary); line-height: 1.6;
  margin-top: 12px; flex: 1; min-height: 0;
  display: -webkit-box; -webkit-line-clamp: 5; -webkit-box-orient: vertical;
  overflow: hidden; text-overflow: ellipsis;
  max-width: 100%;
}
.bb-actions {
  display: flex; gap: 10px; margin-top: 16px; flex-shrink: 0;
}

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
