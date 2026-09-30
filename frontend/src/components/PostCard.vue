<script setup>
import { formatTime } from '../utils/format'
import { useRouter } from 'vue-router'

const props = defineProps({
  post: { type: Object, required: true },
  /** 跳转前缀, 默认 /post/detail/ */
  to: { type: String, default: '/post/detail/' }
})

const router = useRouter()

const open = () => {
  router.push(props.to + props.post.id)
}

const openUser = (id) => {
  if (id) router.push(`/user/${id}`)
}
</script>

<template>
  <div class="post-card cjr-card" @click="open">
    <div class="post-card-header">
      <span v-if="post.isTop === 1" class="top-badge">置顶</span>
      <el-tag size="small" effect="plain" type="info">{{ post.category }}</el-tag>
      <h3 class="post-card-title cjr-ellipsis-2">{{ post.title }}</h3>
    </div>
    <p class="post-card-summary cjr-ellipsis-2">{{ post.summary }}</p>
    <div v-if="post.cover" class="post-card-cover">
      <img :src="post.cover" loading="lazy" alt="" />
    </div>
    <div class="post-card-footer">
      <div class="author" @click.stop="openUser(post.author?.id)">
        <el-avatar :size="22" :src="post.author?.avatar || undefined">
          {{ post.author?.nickname?.charAt(0) }}
        </el-avatar>
        <span class="cjr-ellipsis">{{ post.author?.nickname }}</span>
      </div>
      <div class="meta">
        <span><el-icon><View /></el-icon> {{ post.viewCount || 0 }}</span>
        <span v-if="post.commentCount !== undefined"><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
        <span v-if="post.likeCount !== undefined"><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
        <span class="time">{{ formatTime(post.createTime) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.post-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}
.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}
.post-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.top-badge {
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 1px 8px;
  border-radius: 4px;
}
.post-card-title {
  margin: 0;
  font-size: 16px;
  line-height: 1.4;
  flex: 1;
}
.post-card-summary {
  color: var(--cjr-text-secondary);
  font-size: 13px;
  line-height: 1.6;
  margin: 10px 0;
  flex: 1;
}
.post-card-cover img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 6px;
}
.post-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 13px;
  color: var(--cjr-text-secondary);
}
.author {
  display: flex;
  align-items: center;
  gap: 6px;
  max-width: 40%;
}
.meta {
  display: flex;
  align-items: center;
  gap: 12px;
}
.meta span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.time {
  display: none;
}
@media (max-width: 480px) {
  .time {
    display: none;
  }
}
</style>
