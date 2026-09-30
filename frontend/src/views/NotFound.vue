<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="error-page">
    <h1>404</h1>
    <p>页面不存在或已被移除</p>
    <el-button type="primary" @click="router.push('/home')">返回首页</el-button>
  </div>
</template>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: var(--cjr-bg);
}
.error-page h1 {
  font-size: 72px;
  color: var(--el-color-primary);
  margin: 0;
}
.error-page p {
  color: var(--cjr-text-secondary);
}
</style>
