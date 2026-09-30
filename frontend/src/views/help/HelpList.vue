<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHelpPage } from '../../api/help'
import { formatMoney, formatTime } from '../../utils/format'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 9, keyword: '' })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.keyword) delete params.keyword
    const data = await getHelpPage(params)
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
        placeholder="搜索求助信息"
        clearable
        class="search-input"
        :prefix-icon="'Search'"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button type="primary" :icon="'Edit'" @click="router.push('/help/edit')">发布求助</el-button>
      <el-button plain @click="router.push('/donation/my')">我的捐助</el-button>
    </div>

    <div v-loading="loading">
      <div v-if="list.length" class="cjr-card-grid">
        <div v-for="h in list" :key="h.id" class="help-card cjr-card" @click="router.push(`/help/detail/${h.id}`)">
          <div class="help-head">
            <el-avatar :size="36" :src="h.user?.avatar || undefined">{{ h.user?.nickname?.charAt(0) }}</el-avatar>
            <div class="help-head-text">
              <div class="title cjr-ellipsis">{{ h.title }}</div>
              <div class="user">{{ h.user?.nickname }} · {{ formatTime(h.createTime) }}</div>
            </div>
          </div>
          <p class="desc cjr-ellipsis-2">{{ h.description }}</p>
          <div class="progress-row">
            <el-progress :percentage="h.progress" :stroke-width="10" />
            <div class="amount-row">
              <span class="raised">已筹 <b>¥{{ formatMoney(h.raisedAmount) }}</b></span>
              <span class="target">目标 ¥{{ formatMoney(h.targetAmount) }}</span>
            </div>
          </div>
          <div class="donate-btn">
            <el-button type="warning" round @click.stop="router.push(`/help/detail/${h.id}`)">我要捐助</el-button>
          </div>
        </div>
      </div>
      <EmptyState v-else-if="!loading" text="暂无进行中的求助" />
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
.help-card {
  cursor: pointer;
  transition: transform 0.2s;
  display: flex;
  flex-direction: column;
}
.help-card:hover {
  transform: translateY(-3px);
}
.help-head {
  display: flex;
  gap: 10px;
  align-items: center;
}
.help-head-text {
  flex: 1;
  min-width: 0;
}
.title {
  font-weight: 600;
  font-size: 15px;
}
.user {
  color: var(--cjr-text-secondary);
  font-size: 12px;
  margin-top: 2px;
}
.desc {
  color: var(--cjr-text-secondary);
  font-size: 13px;
  line-height: 1.6;
  margin: 12px 0;
  flex: 1;
}
.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-top: 8px;
}
.raised b {
  color: #e6a23c;
}
.target {
  color: var(--cjr-text-secondary);
}
.donate-btn {
  text-align: center;
  margin-top: 14px;
}
</style>
