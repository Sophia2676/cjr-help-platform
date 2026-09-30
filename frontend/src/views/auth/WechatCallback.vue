<template>
  <div class="callback-page">
    <div class="callback-card">
      <el-icon v-if="!done" class="loading-icon" :size="36"><Loading /></el-icon>
      <template v-else>
        <el-icon :size="40" color="#1f8a70"><SuccessFilled /></el-icon>
        <p>微信登录成功，正在跳转…</p>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { wechatLogin } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()
const done = ref(false)

onMounted(async () => {
  const code = route.query.code
  if (!code) {
    ElMessage.error('微信授权失败，请重试')
    router.replace('/login')
    return
  }
  try {
    const data = await wechatLogin(code)
    store.token = data.token
    store.user = data.user
    localStorage.setItem('cjr_token', data.token)
    done.value = true
    setTimeout(() => router.replace('/home'), 600)
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '微信登录失败，请重试')
    router.replace('/login')
  }
})
</script>

<style scoped>
.callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f8a70 0%, #57c7ad 100%);
}
.callback-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  color: #2b3a36;
}
.loading-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
