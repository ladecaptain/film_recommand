<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="logo">影荐</h1>
      <p class="subtitle">你的电影推荐助手</p>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-position="top">
            <el-form-item label="手机号 / 邮箱">
              <el-input v-model="loginForm.account" placeholder="请输入手机号或邮箱" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
            <el-button type="primary" class="submit-btn" @click="handleLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-position="top">
            <el-form-item label="昵称">
              <el-input v-model="registerForm.nickname" placeholder="给自己取个名字" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" type="email" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" placeholder="至少6位密码" show-password />
            </el-form-item>
            <el-button type="primary" class="submit-btn" @click="handleRegister">注 册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('login')

const loginForm = reactive({ account: '', password: '' })
const registerForm = reactive({ nickname: '', email: '', password: '' })

async function handleLogin() {
  try {
    await userStore.login(loginForm.account, loginForm.password)
    const redirect = (route.query.redirect as string) || '/home'
    router.push(redirect)
  } catch { /* error handled in interceptor */ }
}

async function handleRegister() {
  try {
    // Will be implemented after backend is ready
    router.push('/home')
  } catch { /* error handled in interceptor */ }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-primary);
  padding: 24px;
}

.login-card {
  width: 400px;
  padding: 40px;
  background-color: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.logo {
  text-align: center;
  font-size: 32px;
  color: var(--accent-gold);
  letter-spacing: 4px;
  margin-bottom: 4px;
}

.subtitle {
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 32px;
}

.login-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}
</style>
