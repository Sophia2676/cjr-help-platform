<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPolicyPage } from '../../api/policy'
import { formatTime } from '../../utils/format'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '' })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.keyword) delete params.keyword
    const data = await getPolicyPage(params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.value.pageNum = 1
  load()
}

const changePage = (p) => {
  query.value.pageNum = p
  load()
}

onMounted(load)
</script>

<template>
  <div class="cjr-container cjr-page">
    <div class="cjr-card toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索政策标题"
        clearable
        class="search-input"
        :prefix-icon="'Search'"
        @keyup.enter="search"
        @clear="search"
      />
    </div>

    <div v-loading="loading">
      <div v-if="list.length" class="policy-list">
        <div v-for="p in list" :key="p.id" class="policy-item cjr-card" @click="router.push(`/policy/detail/${p.id}`)">
          <div class="policy-main">
            <h3 class="title cjr-ellipsis-2">{{ p.title }}</h3>
            <p v-if="p.summary" class="summary cjr-ellipsis-2">{{ p.summary }}</p>
            <div class="meta">
              <span>{{ p.source }}</span>
              <span>{{ formatTime(p.publishTime) }}</span>
              <span><el-icon><View /></el-icon> {{ p.viewCount }}</span>
            </div>
          </div>
          <img v-if="p.coverImage" :src="p.coverImage" class="cover" alt="" />
        </div>
      </div>
      <EmptyState v-else-if="!loading" text="暂无政策资讯" />
    </div>

    <div v-if="total > query.pageSize" class="cjr-pagination">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="changePage" />
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  padding: 12px 16px;
}
.search-input {
  max-width: 420px;
}
.policy-item {
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: transform 0.2s;
}
.policy-item:hover {
  transform: translateY(-2px);
}
.policy-main {
  flex: 1;
  min-width: 0;
}
.title {
  margin: 0 0 6px;
  font-size: 16px;
}
.summary {
  margin: 0 0 10px;
  color: var(--cjr-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}
.meta {
  display: flex;
  gap: 14px;
  color: var(--cjr-text-secondary);
  font-size: 12px;
}
.meta span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}
.cover {
  width: 140px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}
@media (max-width: 768px) {
  .cover {
    width: 90px;
    height: 64px;
  }
}
</style>
