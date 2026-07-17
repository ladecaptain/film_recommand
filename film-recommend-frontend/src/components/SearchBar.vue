<template>
  <div class="search-bar">
    <el-input
      v-model="keyword"
      placeholder="搜索电影..."
      :prefix-icon="Search"
      clearable
      @input="onInput"
      @keyup.enter="onSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const keyword = ref('')
let timer: ReturnType<typeof setTimeout> | null = null

function onInput() {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    if (keyword.value.trim()) {
      router.push({ name: 'Discover', query: { keyword: keyword.value.trim() } })
    }
  }, 300)
}

function onSearch() {
  if (keyword.value.trim()) {
    router.push({ name: 'Discover', query: { keyword: keyword.value.trim() } })
  }
}
</script>
