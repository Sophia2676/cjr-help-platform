<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getUser } from '../../api/user'
import { getUserPosts } from '../../api/post'
import PostCard from '../../components/PostCard.vue'
import EmptyState from '../../components/EmptyState.vue'
import { GENDERS } from '../../utils/constants'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const user = ref(null)
const posts = ref([])
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try {
    user.value = await getUser(route.params.id)
    const data = await getUserPosts(route.params.id, { pageNum: 1, pageSize: 10 })
    posts.value = data.records
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="cjr-container cjr-page" v-loading="loading">
    <div v-if="user" class="cjr-card user-head">
      <el-avatar :size="64" :src="user.avatar || undefined">{{ user.nickname?.charAt(0) }}</el-avatar>
      <div class="user-info">
        <h2 class="nickname">{{ user.nickname }}</h2>
        <div class="meta">
          <span>{{ GENDERS[user.gender] || '未设置性别' }}</span>
          <span v-if="user.disabilityType">{{ user.disabilityType }}{{ user.disabilityLevel ? ` ${user.disabilityLevel}级` : '' }}</span>
          <span>注册于 {{ formatDateTime(user.createTime).slice(0, 10) }}</span>
        </div>
        <p v-if="user.bio" class="bio">{{ user.bio }}</p>
      </div>
    </div>

    <div class="cjr-section-title">TA 的帖子</div>
    <div v-if="posts.length" class="cjr-card-grid">
      <PostCard v-for="p in posts" :key="p.id" :post="p" />
    </div>
    <EmptyState v-else-if="!loading" text="TA 还没有发布帖子" />
  </div>
</template>

<style scoped>
.user-head {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.user-info {
  flex: 1;
  min-width: 0;
}
.nickname {
  margin: 0 0 6px;
  font-size: 20px;
}
.meta {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  color: var(--cjr-text-secondary);
  font-size: 13px;
}
.bio {
  margin: 10px 0 0;
  color: var(--cjr-text);
  font-size: 14px;
  line-height: 1.7;
}
</style>
