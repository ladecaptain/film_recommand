<template>
  <div class="movie-card-horizontal" @click="$router.push(`/movie/${movie.tmdbId}`)">
    <div class="poster">
      <img v-if="posterUrl" :src="posterUrl" :alt="movie.title" />
      <div v-else class="poster-placeholder">{{ movie.title }}</div>
    </div>
    <div class="info">
      <h4 class="title">{{ movie.title }}</h4>
      <el-tag v-if="matchScore" type="warning" size="small">{{ matchScore }}% 匹配 · {{ matchGenre }}</el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RecommendMovie } from '@/types'
import { getPosterUrl, POSTER_SIZES } from '@/utils/constants'

const props = defineProps<{ movie: RecommendMovie; matchScore?: number; matchGenre?: string }>()

const posterUrl = computed(() => props.movie.posterUrl || getPosterUrl(props.movie.posterPath || null, POSTER_SIZES.small))
</script>

<style scoped lang="scss">
.movie-card-horizontal {
  cursor: pointer;
  min-width: 160px;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-4px);
  }
}

.poster {
  aspect-ratio: 2/3;
  background-color: var(--bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .poster-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-muted);
    font-size: 12px;
    padding: 8px;
    text-align: center;
  }
}

.info {
  padding: 8px;
}

.title {
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}
</style>
