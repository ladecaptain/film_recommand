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
      <h2 class="section-title">热门电影</h2>
      <div class="movie-grid">
        <p class="empty-hint">加载中...</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped lang="scss">
.home {
  .section-title {
    font-size: 22px;
    font-weight: 700;
    margin-bottom: 20px;
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

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--border-color);
    border-radius: 2px;
  }
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.empty-hint {
  color: var(--text-muted);
  font-size: 14px;
  padding: 20px 0;
}
</style>
