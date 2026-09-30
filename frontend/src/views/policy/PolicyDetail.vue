<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPolicyDetail } from '../../api/policy'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const policy = ref(null)
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try {
    policy.value = await getPolicyDetail(route.params.id)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="cjr-container cjr-page" v-loading="loading">
    <div v-if="policy" class="cjr-card detail-card">
      <h1 class="title">{{ policy.title }}</h1>
      <div class="meta">
        <span>来源：{{ policy.source || '残联' }}</span>
        <span>发布时间：{{ formatDateTime(policy.publishTime) }}</span>
        <span>浏览：{{ policy.viewCount }}</span>
      </div>
      <el-divider />
      <img v-if="policy.coverImage" :src="policy.coverImage" class="cover" alt="" />
      <div class="content cjr-content">{{ policy.content }}</div>
    </div>
  </div>
</template>

<style scoped>
.detail-card {
  padding: 30px;
}
.title {
  font-size: 24px;
  margin: 0 0 12px;
  line-height: 1.4;
}
.meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  color: var(--cjr-text-secondary);
  font-size: 13px;
}
.cover {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 16px;
}
.content {
  font-size: 15px;
}
@media (max-width: 768px) {
  .detail-card {
    padding: 18px;
  }
  .title {
    font-size: 20px;
  }
}
</style>
