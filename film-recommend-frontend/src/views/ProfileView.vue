<template>
  <div class="profile">
    <div class="profile-header">
      <el-avatar :size="64" :src="userStore.userInfo?.avatar">
        {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="user-info">
        <h2>{{ userStore.userInfo?.nickname || '用户' }}</h2>
        <p class="stats">看过 X 部 · 平均评分 X.X · 想看 X 部</p>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="我的观影记录" name="watched">
        <div class="empty-state">
          <p>还没有观影记录，去发现好电影吧 →</p>
        </div>
      </el-tab-pane>
      <el-tab-pane label="想看电影" name="wishlist">
        <div class="empty-state">
          <p>还没有想看标记，去发现好电影吧 →</p>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('watched')
</script>

<style scoped lang="scss">
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding: 24px;
  background-color: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.user-info {
  h2 {
    font-size: 22px;
    font-weight: 700;
    margin-bottom: 6px;
  }

  .stats {
    color: var(--text-muted);
    font-size: 14px;
  }
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-muted);

  p {
    font-size: 15px;
  }
}
</style>
