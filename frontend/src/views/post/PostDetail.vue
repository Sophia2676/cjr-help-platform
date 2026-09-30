<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail, toggleLike, deletePost } from '../../api/post'
import { getPostComments, createComment, deleteComment } from '../../api/comment'
import { useUserStore } from '../../stores/user'
import { formatTime } from '../../utils/format'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const post = ref(null)
const comments = ref([])
const loading = ref(true)
const commentText = ref('')
const submitting = ref(false)
const likeLoading = ref(false)
// 回复状态: { parentId, replyTo, replyUserId }
const replyTo = ref(null)

const load = async () => {
  loading.value = true
  try {
    post.value = await getPostDetail(route.params.id)
    comments.value = await getPostComments(route.params.id)
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  if (!store.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  likeLoading.value = true
  try {
    const data = await toggleLike(post.value.id)
    post.value.isLiked = data.liked
    post.value.likeCount = data.likeCount
  } finally {
    likeLoading.value = false
  }
}

const submitComment = async () => {
  if (!store.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await createComment({
      postId: post.value.id,
      content: commentText.value,
      parentId: replyTo.value?.parentId || 0,
      replyUserId: replyTo.value?.replyUserId || null
    })
    ElMessage.success('评论成功')
    commentText.value = ''
    replyTo.value = null
    comments.value = await getPostComments(post.value.id)
    post.value.commentCount += 1
  } finally {
    submitting.value = false
  }
}

const setReply = (c, replyUserId) => {
  replyTo.value = { parentId: c.parentId === 0 ? c.id : c.parentId, replyTo: c.user?.nickname, replyUserId }
  commentText.value = ''
}

const cancelReply = () => {
  replyTo.value = null
}

const removeComment = async (c) => {
  await ElMessageBox.confirm('确定删除该评论吗？其下回复将一并删除', '提示', { type: 'warning' })
  await deleteComment(c.id)
  ElMessage.success('删除成功')
  comments.value = await getPostComments(post.value.id)
  post.value.commentCount = comments.value.reduce((n, top) => n + 1 + (top.replies?.length || 0), 0)
}

const canDelete = (c) => store.isAdmin || (store.isLogin && store.user?.id === c.user?.id)

const removePost = async () => {
  await ElMessageBox.confirm('确定删除该帖子吗？评论与点赞将一并删除', '提示', { type: 'warning' })
  await deletePost(post.value.id)
  ElMessage.success('删除成功')
  router.push('/post')
}

const openUser = (id) => {
  if (id) router.push(`/user/${id}`)
}
</script>

<template>
  <div class="cjr-container cjr-page" v-loading="loading">
    <div v-if="post" class="cjr-card">
      <div class="post-head">
        <h1 class="title">{{ post.title }}</h1>
        <div class="meta">
          <el-tag size="small" effect="plain">{{ post.category }}</el-tag>
          <span v-if="post.isTop === 1" class="top-badge">置顶</span>
          <div class="author" @click="openUser(post.author?.id)">
            <el-avatar :size="30" :src="post.author?.avatar || undefined">
              {{ post.author?.nickname?.charAt(0) }}
            </el-avatar>
            <div>
              <div class="name">{{ post.author?.nickname }}</div>
              <div class="time">{{ formatTime(post.createTime) }}</div>
            </div>
          </div>
          <div class="spacer" />
          <span class="views"><el-icon><View /></el-icon> {{ post.viewCount }} 浏览</span>
          <el-button
            v-if="store.isLogin && (store.user?.id === post.author?.id || store.isAdmin)"
            size="small"
            type="danger"
            plain
            @click="removePost"
          >删除</el-button>
          <el-button
            v-if="store.user?.id === post.author?.id"
            size="small"
            @click="router.push(`/post/edit/${post.id}`)"
          >编辑</el-button>
        </div>
      </div>

      <div class="content cjr-content">{{ post.content }}</div>

      <div v-if="post.images?.length" class="images">
        <el-image
          v-for="(img, i) in post.images"
          :key="i"
          :src="img"
          :preview-src-list="post.images"
          :initial-index="i"
          fit="cover"
          class="post-img"
        />
      </div>

      <div class="like-bar">
        <el-button
          :type="post.isLiked ? 'primary' : 'default'"
          round
          :loading="likeLoading"
          @click="handleLike"
        >
          <el-icon><Star /></el-icon>
          {{ post.isLiked ? '已点赞' : '点赞' }} {{ post.likeCount }}
        </el-button>
      </div>
    </div>

    <!-- 评论区 -->
    <div v-if="post" class="cjr-card">
      <h3 class="comment-title">评论（{{ post.commentCount }}）</h3>

      <div class="comment-editor">
        <div v-if="replyTo" class="reply-tip">
          回复 <b>{{ replyTo.replyTo }}</b>
          <el-icon class="clickable" @click="cancelReply"><Close /></el-icon>
        </div>
        <el-input
          v-model="commentText"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="友善交流，传递温暖…"
        />
        <div class="editor-actions">
          <el-button type="primary" :loading="submitting" @click="submitComment">发表评论</el-button>
        </div>
      </div>

      <div v-if="comments.length" class="comment-list">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <el-avatar :size="32" :src="c.user?.avatar || undefined">{{ c.user?.nickname?.charAt(0) }}</el-avatar>
          <div class="comment-body">
            <div class="comment-head">
              <span class="name">{{ c.user?.nickname }}</span>
              <span class="time">{{ formatTime(c.createTime) }}</span>
            </div>
            <div class="comment-content">{{ c.content }}</div>
            <div class="comment-actions">
              <el-button link size="small" @click="setReply(c, c.user?.id)">回复</el-button>
              <el-button v-if="canDelete(c)" link size="small" type="danger" @click="removeComment(c)">删除</el-button>
            </div>
            <div v-if="c.replies?.length" class="replies">
              <div v-for="r in c.replies" :key="r.id" class="reply-item">
                <el-avatar :size="24" :src="r.user?.avatar || undefined">{{ r.user?.nickname?.charAt(0) }}</el-avatar>
                <div>
                  <div class="comment-head">
                    <span class="name">{{ r.user?.nickname }}</span>
                    <span v-if="r.replyTo" class="reply-to">回复 {{ r.replyTo }}</span>
                    <span class="time">{{ formatTime(r.createTime) }}</span>
                  </div>
                  <div class="comment-content">{{ r.content }}</div>
                  <div class="comment-actions">
                    <el-button link size="small" @click="setReply(r, r.user?.id)">回复</el-button>
                    <el-button v-if="canDelete(r)" link size="small" type="danger" @click="removeComment(r)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <EmptyState v-else text="还没有评论，来抢沙发吧" />
    </div>
  </div>
</template>

<style scoped>
.post-head .title {
  font-size: 22px;
  margin: 0 0 14px;
  line-height: 1.4;
}
.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--cjr-text-secondary);
  font-size: 13px;
}
.top-badge {
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 1px 8px;
  border-radius: 4px;
}
.author {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.name {
  font-size: 14px;
  color: var(--cjr-text);
}
.spacer {
  flex: 1;
}
.views {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.content {
  margin: 18px 0;
  font-size: 15px;
}
.images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}
.post-img {
  width: 200px;
  height: 140px;
  border-radius: 6px;
}
.like-bar {
  text-align: center;
  padding-top: 8px;
}
.comment-title {
  margin: 0 0 16px;
}
.reply-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--el-color-primary);
  margin-bottom: 8px;
}
.clickable {
  cursor: pointer;
}
.editor-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
.comment-item {
  display: flex;
  gap: 10px;
  padding: 14px 0;
  border-bottom: 1px solid var(--cjr-border);
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.comment-head .name {
  font-weight: 600;
  font-size: 13px;
}
.reply-to {
  color: var(--el-color-primary);
  font-size: 12px;
}
.time {
  color: var(--cjr-text-secondary);
  font-size: 12px;
}
.comment-content {
  margin: 6px 0 2px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}
.replies {
  background: var(--cjr-bg);
  border-radius: 6px;
  padding: 8px 12px;
  margin-top: 8px;
}
.reply-item {
  display: flex;
  gap: 8px;
  padding: 6px 0;
}
@media (max-width: 768px) {
  .post-img {
    width: 100%;
    height: auto;
  }
}
</style>
