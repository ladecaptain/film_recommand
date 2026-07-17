<template>
  <div class="discover">
    <div class="discover-header">
      <h2 class="page-title">发现电影</h2>
      <el-radio-group v-model="sortBy" size="small">
        <el-radio-button value="popularity.desc">热门</el-radio-button>
        <el-radio-button value="primary_release_date.desc">最新</el-radio-button>
        <el-radio-button value="vote_average.desc">评分最高</el-radio-button>
      </el-radio-group>
    </div>

    <div class="discover-body">
      <aside class="genre-sidebar">
        <h3>类型筛选</h3>
        <div class="genre-list">
          <el-checkbox
            v-for="genre in GENRE_LIST"
            :key="genre.id"
            :model-value="selectedGenres.includes(genre.id)"
            @change="(val: boolean) => toggleGenre(genre.id, val)"
            :label="genre.name"
          />
        </div>
      </aside>

      <div class="movie-grid">
        <p class="empty-hint">加载中...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { GENRE_LIST } from '@/utils/constants'

const sortBy = ref('popularity.desc')
const selectedGenres = ref<number[]>([])

function toggleGenre(id: number, val: boolean) {
  if (val) {
    selectedGenres.value.push(id)
  } else {
    selectedGenres.value = selectedGenres.value.filter(g => g !== id)
  }
}
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

.genre-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.movie-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 20px;
}

.empty-hint {
  color: var(--text-muted);
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
}
</style>
