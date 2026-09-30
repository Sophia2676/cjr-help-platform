<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostPage } from '../../api/post'
import { POST_CATEGORIES } from '../../utils/constants'
import { useUserStore } from '../../stores/user'
import PostCard from '../../components/PostCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const store = useUserStore()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 9, keyword: '', category: '', sort: 'latest' })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.keyword) delete params.keyword
    if (!params.category) delete params.category
    const data = await getPostPage(params)
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
        placeholder="搜索帖子标题或内容"
        clearable
        class="search-input"
        :prefix-icon="'Search'"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.category" placeholder="全部分类" clearable class="cat-select" @change="search">
        <el-option v-for="c in POST_CATEGORIES" :key="c" :label="c" :value="c" />
      </el-select>
      <el-radio-group v-model="query.sort" @change="search">
        <el-radio-button value="latest">最新</el-radio-button>
        <el-radio-button value="hot">热门</el-radio-button>
      </el-radio-group>
      <el-button type="primary" :icon="'Edit'" @click="router.push('/post/edit')">发布帖子</el-button>
    </div>

    <div v-loading="loading">
      <div v-if="list.length" class="cjr-card-grid">
        <PostCard v-for="p in list" :key="p.id" :post="p" />
      </div>
      <EmptyState v-else-if="!loading" text="暂无帖子，来发布第一条吧" />
    </div>

    <div v-if="total > query.pageSize" class="cjr-pagination">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="changePage"
      />
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
@media (max-width: 768px) {
  .toolbar {
    gap: 8px;
  }
  .cat-select {
    width: 110px;
  }
}
</style>
