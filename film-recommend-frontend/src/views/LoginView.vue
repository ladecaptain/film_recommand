<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="logo">影荐</h1>
      <p class="subtitle">你的电影推荐助手</p>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top">
            <el-form-item label="手机号 / 邮箱" prop="account">
              <el-input v-model="loginForm.account" placeholder="请输入手机号或邮箱" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin" />
            </el-form-item>
            <el-button type="primary" class="submit-btn" :loading="loginLoading" @click="handleLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="registerForm.nickname" placeholder="给自己取个名字" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="registerForm.phone" placeholder="选填，用于登录" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="registerForm.email" placeholder="选填，用于登录（手机号或邮箱至少填一个）" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="至少6位密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password @keyup.enter="handleRegister" />
            </el-form-item>
            <el-button type="primary" class="submit-btn" :loading="registerLoading" @click="handleRegister">注 册</el-button>
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
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeTab = ref('login')

const loginForm = reactive({ account: '', password: '' })
const registerForm = reactive({ nickname: '', phone: '', email: '', password: '', confirmPassword: '' })
const loginLoading = ref(false)
const registerLoading = ref(false)
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginRules: FormRules = {
  account: [{ required: true, message: '请输入手机号或邮箱', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
}

const validateConfirm = (_rule: any, value: string, cb: any) => {
  if (value !== registerForm.password) {
    cb(new Error('两次输入的密码不一致'))
  } else {
    cb()
  }
}

const registerRules: FormRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loginLoading.value = true
  try {
    await userStore.login(loginForm.account, loginForm.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/home'
    router.push(redirect)
  } catch {
    // 错误消息由拦截器统一展示
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!registerForm.phone && !registerForm.email) {
    ElMessage.warning('手机号或邮箱至少填一个')
    return
  }
  registerLoading.value = true
  try {
    await authApi.register({
      phone: registerForm.phone || undefined,
      email: registerForm.email || undefined,
      password: registerForm.password,
      nickname: registerForm.nickname,
    })
    ElMessage.success('注册成功，请登录')
    loginForm.account = registerForm.phone || registerForm.email
    loginForm.password = ''
    registerForm.nickname = ''
    registerForm.phone = ''
    registerForm.email = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerFormRef.value?.resetFields()
    activeTab.value = 'login'
  } catch {
    // 错误消息由拦截器统一展示
  } finally {
    registerLoading.value = false
  }
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
