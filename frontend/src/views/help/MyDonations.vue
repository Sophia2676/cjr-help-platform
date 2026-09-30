<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyDonations, getReceivedDonations } from '../../api/donation'
import { formatMoney, formatTime } from '../../utils/format'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const tab = ref('out')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10 })

const load = async () => {
  loading.value = true
  try {
    const fn = tab.value === 'out' ? getMyDonations : getReceivedDonations
    const data = await fn(query.value)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const changeTab = () => {
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
    <div class="cjr-card">
      <h2 class="page-title">我的捐助</h2>
      <el-tabs v-model="tab" @tab-change="changeTab">
        <el-tab-pane label="我捐出的" name="out" />
        <el-tab-pane label="我收到的" name="in" />
      </el-tabs>

      <div v-loading="loading">
        <div v-for="d in list" :key="d.id" class="row" @click="router.push(`/help/detail/${d.helpRequestId}`)">
          <div class="row-main">
            <span class="row-title cjr-ellipsis">{{ d.helpTitle || '求助信息' }}</span>
            <span class="amount">¥{{ formatMoney(d.amount) }}</span>
          </div>
          <div class="row-sub">
            <span>{{ tab === 'out' ? '捐助给' : '来自' }} {{ d.donorNickname }}</span>
            <el-tag v-if="d.status === 2" size="small" type="success" effect="plain">已确认</el-tag>
            <el-tag v-else size="small" type="warning" effect="plain">待确认</el-tag>
            <span class="time">{{ formatTime(d.createTime) }}</span>
          </div>
        </div>
        <EmptyState v-if="!list.length && !loading" :text="tab === 'out' ? '还没有捐出记录' : '还没有收到捐助'" />
      </div>

      <div v-if="total > query.pageSize" class="cjr-pagination">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="changePage" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0 0 8px;
  font-size: 18px;
}
.row {
  padding: 12px 4px;
  border-bottom: 1px solid var(--cjr-border);
  cursor: pointer;
}
.row-main {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.row-title {
  font-weight: 600;
}
.amount {
  color: #e6a23c;
  font-weight: 700;
}
.row-sub {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 6px;
  color: var(--cjr-text-secondary);
  font-size: 12px;
}
.time {
  margin-left: auto;
}
</style>
