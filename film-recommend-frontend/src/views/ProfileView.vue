<template>
  <div class="profile">
    <div class="profile-header">
      <el-avatar :size="64" :src="userStore.userInfo?.avatar">
        {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="user-info">
        <h2>{{ userStore.userInfo?.nickname || '用户' }}</h2>
        <p class="stats">看过 {{ stats.watched }} 部 · 平均评分 {{ stats.averageRating }} · 想看 {{ stats.wishlist }} 部</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="我的观影记录" name="watched">
        <div v-if="loading" class="loading-wrap">
          <el-skeleton v-for="n in 3" :key="n" :rows="2" animated style="margin-bottom: 16px" />
        </div>

        <div v-else-if="!records.length" class="empty-state">
          <p>还没有观影记录，去发现好电影吧 →</p>
          <el-button type="primary" @click="$router.push('/discover')">发现电影</el-button>
        </div>

        <div v-else class="record-list">
          <div
            v-for="r in records"
            :key="r.id"
            class="record-item"
            @click="$router.push(`/movie/${r.movie?.tmdbId}`)"
          >
            <div class="poster-wrap">
              <img v-if="r.movie?.posterUrl" :src="r.movie.posterUrl" :alt="r.movie.title" />
              <div v-else class="poster-ph">{{ r.movie?.title?.charAt(0) || '?' }}</div>
            </div>
            <div class="record-body">
              <div class="record-top">
                <h3 class="record-title">{{ r.movie?.title || '未知电影' }}</h3>
                <el-button
                  class="delete-btn"
                  size="small"
                  type="danger"
                  text
                  @click.stop="handleDelete(r)"
                >删除</el-button>
              </div>
              <div class="record-meta">
                <span v-if="r.rating" class="record-rating">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
                <span v-else class="no-rating">未评分</span>
                <span class="record-date">{{ r.watchDate || r.createTime?.substring(0, 10) }}</span>
              </div>
              <p v-if="r.comment" class="record-comment">{{ r.comment }}</p>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="想看电影" name="wishlist">
        <div v-if="loading" class="loading-wrap">
          <el-skeleton v-for="n in 3" :key="n" :rows="2" animated style="margin-bottom: 16px" />
        </div>

        <div v-else-if="!records.length" class="empty-state">
          <p>还没有想看标记，去发现好电影吧 →</p>
          <el-button type="primary" @click="$router.push('/discover')">发现电影</el-button>
        </div>

        <div v-else class="record-list">
          <div
            v-for="r in records"
            :key="r.id"
            class="record-item"
            @click="$router.push(`/movie/${r.movie?.tmdbId}`)"
          >
            <div class="poster-wrap">
              <img v-if="r.movie?.posterUrl" :src="r.movie.posterUrl" :alt="r.movie.title" />
              <div v-else class="poster-ph">{{ r.movie?.title?.charAt(0) || '?' }}</div>
            </div>
            <div class="record-body">
              <div class="record-top">
                <h3 class="record-title">{{ r.movie?.title || '未知电影' }}</h3>
                <el-button
                  class="delete-btn"
                  size="small"
                  type="danger"
                  text
                  @click.stop="handleDelete(r)"
                >删除</el-button>
              </div>
              <span class="record-date">{{ r.createTime?.substring(0, 10) }}</span>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { recordApi } from '@/api/record'
import type { WatchRecord } from '@/types'

const userStore = useUserStore()
const activeTab = ref('watched')
const records = ref<WatchRecord[]>([])
const loading = ref(true)
const stats = ref({ watched: 0, wishlist: 0, averageRating: 0 })

async function loadRecords() {
  loading.value = true
  try {
    const status = activeTab.value === 'watched' ? 2 : 1
    records.value = await recordApi.getMyRecords(status)
    stats.value = await recordApi.getMyStats()
  } catch { /* handled in interceptor */ } finally {
    loading.value = false
  }
}

function onTabChange() {
  loadRecords()
}

async function handleDelete(r: WatchRecord) {
  try {
    await recordApi.deleteRecord(r.id)
    records.value = records.value.filter(item => item.id !== r.id)
    stats.value = await recordApi.getMyStats()
  } catch { /* handled in interceptor */ }
}

onMounted(loadRecords)
</script>

<style scoped lang="scss">
.profile-header {
  display: flex; align-items: center; gap: 20px; margin-bottom: 32px;
  padding: 24px; background-color: var(--bg-card); border-radius: 12px;
  border: 1px solid var(--border-color);
}

.user-info {
  h2 { font-size: 22px; font-weight: 700; margin-bottom: 6px; }
  .stats { color: var(--text-muted); font-size: 14px; }
}

.loading-wrap { padding: 20px 0; }

.empty-state {
  text-align: center; padding: 60px 0; color: var(--text-muted);
  p { font-size: 15px; margin-bottom: 16px; }
}

.record-list { display: flex; flex-direction: column; gap: 12px; }

.record-item {
  display: flex; gap: 16px; padding: 16px; background-color: var(--bg-card);
  border-radius: 10px; border: 1px solid var(--border-color);
  cursor: pointer; transition: background-color 0.2s;
  &:hover { background-color: var(--bg-card-hover); }
}

.poster-wrap {
  flex-shrink: 0; width: 70px; height: 100px; border-radius: 6px; overflow: hidden;
  background-color: var(--bg-secondary);
  img { width: 100%; height: 100%; object-fit: cover; }
  .poster-ph {
    width: 100%; height: 100%; display: flex; align-items: center;
    justify-content: center; color: var(--text-muted); font-size: 20px;
  }
}

.record-body { flex: 1; min-width: 0; }

.record-top {
  display: flex; align-items: flex-start; justify-content: space-between;
  margin-bottom: 8px;
  .record-title { font-size: 16px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .delete-btn { opacity: 0; transition: opacity 0.2s; }
}

.record-item:hover .delete-btn { opacity: 1; }

.record-meta {
  display: flex; align-items: center; gap: 16px; margin-bottom: 6px;
  .record-rating { color: var(--accent-gold); font-size: 14px; letter-spacing: 2px; }
  .no-rating { color: var(--text-muted); font-size: 12px; }
  .record-date { color: var(--text-muted); font-size: 12px; }
}

.record-comment { color: var(--text-secondary); font-size: 14px; margin: 4px 0 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
