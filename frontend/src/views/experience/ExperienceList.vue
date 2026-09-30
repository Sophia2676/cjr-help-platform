<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getExperiencePage } from '../../api/experience'
import { EXPERIENCE_CATEGORIES } from '../../utils/constants'
import PostCard from '../../components/PostCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 9, keyword: '', category: '' })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.keyword) delete params.keyword
    if (!params.category) delete params.category
    const data = await getExperiencePage(params)
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
        placeholder="搜索经验标题或内容"
        clearable
        class="search-input"
        :prefix-icon="'Search'"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.category" placeholder="全部分类" clearable class="cat-select" @change="search">
        <el-option v-for="c in EXPERIENCE_CATEGORIES" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" :icon="'Edit'" @click="router.push('/experience/edit')">分享经验</el-button>
    </div>

    <div v-loading="loading">
      <div v-if="list.length" class="cjr-card-grid">
        <PostCard v-for="e in list" :key="e.id" :post="e" to="/experience/detail/" />
      </div>
      <EmptyState v-else-if="!loading" text="暂无经验分享" />
    </div>

    <div v-if="total > query.pageSize" class="cjr-pagination">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="changePage" />
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.search-input {
  flex: 1;
  min-width: 200px;
}
.cat-select {
  width: 140px;
}
</style>
