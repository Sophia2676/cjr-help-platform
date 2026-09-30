<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExperienceDetail, deleteExperience } from '../../api/experience'
import { useUserStore } from '../../stores/user'
import { formatTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const exp = ref(null)
const loading = ref(true)

const load = async () => {
  loading.value = true
  try {
    exp.value = await getExperienceDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

const remove = async () => {
  await ElMessageBox.confirm('确定删除该经验分享吗？', '提示', { type: 'warning' })
  await deleteExperience(exp.value.id)
  ElMessage.success('删除成功')
  router.push('/experience')
}

const openUser = (id) => {
  if (id) router.push(`/user/${id}`)
}

onMounted(load)
</script>

<template>
  <div class="cjr-container cjr-page" v-loading="loading">
    <div v-if="exp" class="cjr-card">
      <h1 class="title">{{ exp.title }}</h1>
      <div class="meta">
        <el-tag size="small" effect="plain">{{ exp.category }}</el-tag>
        <div class="author" @click="openUser(exp.author?.id)">
          <el-avatar :size="30" :src="exp.author?.avatar || undefined">
            {{ exp.author?.nickname?.charAt(0) }}
          </el-avatar>
          <div>
            <div class="name">{{ exp.author?.nickname }}</div>
            <div class="time">{{ formatTime(exp.createTime) }}</div>
          </div>
        </div>
        <div class="spacer" />
        <span class="views"><el-icon><View /></el-icon> {{ exp.viewCount }} 浏览</span>
        <el-button v-if="store.user?.id === exp.author?.id" size="small" @click="router.push(`/experience/edit/${exp.id}`)">编辑</el-button>
        <el-button v-if="store.isLogin && (store.user?.id === exp.author?.id || store.isAdmin)" size="small" type="danger" plain @click="remove">删除</el-button>
      </div>

      <div class="content cjr-content">{{ exp.content }}</div>

      <div v-if="exp.images?.length" class="images">
        <el-image
          v-for="(img, i) in exp.images"
          :key="i"
          :src="img"
          :preview-src-list="exp.images"
          :initial-index="i"
          fit="cover"
          class="exp-img"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.title {
  font-size: 22px;
  margin: 0 0 14px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--cjr-text-secondary);
  font-size: 13px;
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
}
.exp-img {
  width: 220px;
  height: 160px;
  border-radius: 6px;
}
@media (max-width: 768px) {
  .exp-img {
    width: 100%;
    height: auto;
  }
}
</style>
