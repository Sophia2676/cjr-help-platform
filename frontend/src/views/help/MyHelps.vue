<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyHelps, deleteHelp } from '../../api/help'
import { formatMoney, formatTime } from '../../utils/format'
import StatusTag from '../../components/StatusTag.vue'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10 })

const load = async () => {
  loading.value = true
  try {
    const data = await getMyHelps(query.value)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const changePage = (p) => {
  query.value.pageNum = p
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm('确定删除该求助吗？', '提示', { type: 'warning' })
  await deleteHelp(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<template>
  <div class="cjr-container cjr-page">
    <div class="cjr-card">
      <div class="head">
        <h2 class="title">我的求助</h2>
        <el-button type="primary" size="small" @click="router.push('/help/edit')">发布求助</el-button>
      </div>

      <div v-loading="loading">
        <div v-for="h in list" :key="h.id" class="row" @click="router.push(`/help/detail/${h.id}`)">
          <div class="row-main">
            <StatusTag :status="h.status" kind="help" />
            <span class="row-title cjr-ellipsis">{{ h.title }}</span>
            <span class="row-meta">已筹 ¥{{ formatMoney(h.raisedAmount) }} / ¥{{ formatMoney(h.targetAmount) }} · {{ formatTime(h.createTime) }}</span>
          </div>
          <el-button size="small" type="danger" plain @click.stop="remove(h)">删除</el-button>
        </div>
        <EmptyState v-if="!list.length && !loading" text="还没有发布过求助" />
      </div>

      <div v-if="total > query.pageSize" class="cjr-pagination">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="changePage" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.title {
  margin: 0;
  font-size: 18px;
}
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 4px;
  border-bottom: 1px solid var(--cjr-border);
  cursor: pointer;
}
.row-main {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.row-title {
  flex: 1;
}
.row-meta {
  color: var(--cjr-text-secondary);
  font-size: 12px;
  white-space: nowrap;
}
@media (max-width: 768px) {
  .row-meta {
    display: none;
  }
}
</style>
