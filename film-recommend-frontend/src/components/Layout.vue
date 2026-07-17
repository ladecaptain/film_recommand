<template>
  <div class="layout">
    <header class="header">
      <div class="header-left">
        <router-link to="/home" class="logo">影荐</router-link>
      </div>
      <div class="header-center">
        <SearchBar />
      </div>
      <div class="header-right">
        <el-button type="primary" round @click="showRandomBox = true">随机盲盒</el-button>
        <template v-if="userStore.isLoggedIn">
          <el-dropdown>
            <span class="user-menu">
              <el-avatar :size="36" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">我的</el-dropdown-item>
                <el-dropdown-item @click="userStore.logout()">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <el-button v-else text @click="$router.push('/login')">登录</el-button>
      </div>
    </header>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import SearchBar from '@/components/SearchBar.vue'

const userStore = useUserStore()
const showRandomBox = ref(false)
</script>

<style scoped lang="scss">
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  background-color: rgba(26, 26, 46, 0.95);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border-color);
}

.header-left {
  .logo {
    font-size: 24px;
    font-weight: 700;
    color: var(--accent-gold);
    letter-spacing: 2px;
  }
}

.header-center {
  flex: 1;
  max-width: 480px;
  margin: 0 40px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.main-content {
  flex: 1;
  padding: 24px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

.user-menu {
  cursor: pointer;
}
</style>
