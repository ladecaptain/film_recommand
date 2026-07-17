<template>
  <div class="movie-card" @click="$router.push(`/movie/${movie.tmdbId}`)">
    <div class="poster">
      <img v-if="posterUrl" :src="posterUrl" :alt="movie.title" />
      <div v-else class="poster-placeholder">
        <span>{{ movie.title }}</span>
      </div>
      <div class="rating-badge">{{ movie.voteAverage?.toFixed(1) }}</div>
    </div>
    <div class="info">
      <h3 class="title">{{ movie.title }}</h3>
      <p class="year">{{ movie.releaseDate?.substring(0, 4) || '未知' }}</p>
      <p class="genres">{{ movie.genres?.replace(/,/g, ' · ') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Movie } from '@/types'
import { getPosterUrl, POSTER_SIZES } from '@/utils/constants'

const props = defineProps<{ movie: Movie }>()

const posterUrl = computed(() => getPosterUrl(props.movie.posterPath || null, POSTER_SIZES.medium))
</script>

<style scoped lang="scss">
.movie-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-hover);
  }
}

.poster {
  position: relative;
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
    font-size: 14px;
    padding: 16px;
    text-align: center;
  }

  .rating-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    background-color: var(--accent-gold);
    color: #1a1a2e;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 13px;
    font-weight: 700;
  }
}

.info {
  padding: 12px;
}

.title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.year {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.genres {
  font-size: 11px;
  color: var(--text-muted);
}
</style>
