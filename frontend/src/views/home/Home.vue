<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostPage, getRecommendPostPage } from '../../api/post'
import { useUserStore } from '../../stores/user'
import { getPolicyPage } from '../../api/policy'
import { getHelpPage } from '../../api/help'
import PostCard from '../../components/PostCard.vue'

const router = useRouter()
const userStore = useUserStore()
const posts = ref([])
const recommendPosts = ref([])
const policies = ref([])
const helpCount = ref(0)
const sort = ref('latest')
const loading = ref(false)

const entries = [
  { title: '发帖交流', desc: '互帮互助，答疑解惑', icon: 'ChatDotRound', to: '/post', color: '#1f8a70' },
  { title: '康复经验', desc: '家属养护经验分享', icon: 'FirstAidKit', to: '/experience', color: '#409eff' },
  { title: '爱心捐助', desc: '一对一精准帮扶', icon: 'Handshake', to: '/help', color: '#e6a23c' },
  { title: '政策资讯', desc: '残联政策权威发布', icon: 'Memo', to: '/policy', color: '#f56c6c' }
]

const loadRecommend = async () => {
  if (!userStore.isLogin) {
    recommendPosts.value = []
    return
  }
  try {
    const data = await getRecommendPostPage({ pageNum: 1, pageSize: 6 })
    // 只展示真正有推荐逻辑的结果(与普通列表有区分度时显示)
    recommendPosts.value = data.records
  } catch (e) {
    recommendPosts.value = []
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const data = await getPostPage({ pageNum: 1, pageSize: 6, sort: sort.value })
    posts.value = data.records
  } finally {
    loading.value = false
  }
}

const loadPolicies = async () => {
  const data = await getPolicyPage({ pageNum: 1, pageSize: 5 })
  policies.value = data.records
}

const loadHelpCount = async () => {
  const data = await getHelpPage({ pageNum: 1, pageSize: 1 })
  helpCount.value = data.total
}

onMounted(() => {
  loadPosts()
  loadPolicies()
  loadHelpCount()
  loadRecommend()
})
</script>

<template>
  <div class="home">
    <!-- 顶部横幅 -->
    <section class="hero">
      <div class="cjr-container hero-inner">
        <h1>互助友爱 · 携手同行</h1>
        <p>为残疾人朋友及家属搭建温暖互助的交流平台</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="router.push('/post')">进入交流社区</el-button>
          <el-button size="large" plain class="hero-btn2" @click="router.push('/help')">参与爱心捐助</el-button>
        </div>
      </div>
    </section>

    <div class="cjr-container">
      <!-- 功能入口 -->
      <section class="entries">
        <div v-for="e in entries" :key="e.title" class="entry-card cjr-card" @click="router.push(e.to)">
          <el-icon :size="34" :color="e.color"><component :is="e.icon" /></el-icon>
          <h3>{{ e.title }}</h3>
          <p>{{ e.desc }}</p>
        </div>
      </section>

      <!-- 政策速览 -->
      <section v-if="policies.length">
        <div class="cjr-section-title">
          <span>政策资讯</span>
          <el-button text type="primary" @click="router.push('/policy')">查看更多</el-button>
        </div>
        <div class="policy-strip cjr-card">
          <div v-for="p in policies" :key="p.id" class="policy-item" @click="router.push(`/policy/detail/${p.id}`)">
            <span class="dot">●</span>
            <span class="title cjr-ellipsis">{{ p.title }}</span>
            <span class="source cjr-ellipsis">{{ p.source }}</span>
          </div>
        </div>
      </section>

      <!-- 智能推荐 -->
      <section v-if="recommendPosts.length">
        <div class="cjr-section-title">
          <span>为你推荐</span>
          <span class="reco-tip">根据你的高频搜索推送</span>
        </div>
        <div class="cjr-card-grid">
          <PostCard v-for="p in recommendPosts" :key="'r' + p.id" :post="p" />
        </div>
      </section>

      <!-- 帖子速览 -->
      <section>
        <div class="cjr-section-title">
          <span>互助交流</span>
          <el-radio-group v-model="sort" size="small" @change="loadPosts">
            <el-radio-button value="latest">最新</el-radio-button>
            <el-radio-button value="hot">热门</el-radio-button>
          </el-radio-group>
        </div>
        <div v-loading="loading" class="cjr-card-grid">
          <PostCard v-for="p in posts" :key="p.id" :post="p" />
        </div>
        <div class="more-wrap">
          <el-button @click="router.push('/post')">浏览全部帖子</el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.reco-tip { font-size: 13px; color: #8aa39a; }
.hero {
  background: linear-gradient(135deg, #1f8a70 0%, #2b9d86 50%, #57c7ad 100%);
  color: #fff;
  padding: 56px 0;
  text-align: center;
}
.hero h1 {
  font-size: 34px;
  margin: 0 0 12px;
  letter-spacing: 4px;
}
.hero p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 28px;
}
.hero-btn2 {
  color: #fff;
  border-color: #fff;
  background: transparent;
}
.entries {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin: -30px 0 8px;
  position: relative;
  z-index: 2;
}
.entry-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
}
.entry-card:hover {
  transform: translateY(-4px);
}
.entry-card h3 {
  margin: 10px 0 4px;
  font-size: 16px;
}
.entry-card p {
  margin: 0;
  color: var(--cjr-text-secondary);
  font-size: 13px;
}
.policy-strip {
  padding: 8px 20px;
}
.policy-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--cjr-border);
  cursor: pointer;
  font-size: 14px;
}
.policy-item:last-child {
  border-bottom: none;
}
.dot {
  color: var(--el-color-primary);
  font-size: 10px;
}
.title {
  flex: 1;
}
.source {
  color: var(--cjr-text-secondary);
  font-size: 12px;
  max-width: 120px;
}
.more-wrap {
  text-align: center;
  margin-top: 20px;
}
@media (max-width: 768px) {
  .hero {
    padding: 36px 0;
  }
  .hero h1 {
    font-size: 24px;
  }
  .entries {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
