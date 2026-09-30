<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyExperiences, deleteExperience } from '../../api/experience'
import { formatTime } from '../../utils/format'
import StatusTag from '../../components/StatusTag.vue'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, status: null })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (params.status === null) delete params.status
    const data = await getMyExperiences(params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const changeStatus = (s) => {
  query.value.status = s
  query.value.pageNum = 1
  load()
}

const changePage = (p) => {
  query.value.pageNum = p
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm('确定删除该经验分享吗？', '提示', { type: 'warning' })
  await deleteExperience(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<template>
  <div class="cjr-container cjr-page">
    <div class="cjr-card">
      <div class="head">
        <h2 class="title">我的经验分享</h2>
        <el-radio-group v-model="query.status" @change="changeStatus">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="0">待审核</el-radio-button>
          <el-radio-button :value="1">已通过</el-radio-button>
          <el-radio-button :value="2">已驳回</el-radio-button>
        </el-radio-group>
      </div>

      <div v-loading="loading">
        <div v-for="e in list" :key="e.id" class="row">
          <div class="row-main" @click="router.push(`/experience/detail/${e.id}`)">
            <StatusTag :status="e.status" />
            <span class="row-title cjr-ellipsis">{{ e.title }}</span>
            <span class="row-meta">{{ formatTime(e.createTime) }} · {{ e.viewCount }}浏览</span>
          </div>
          <div class="row-actions">
            <el-button size="small" @click="router.push(`/experience/edit/${e.id}`)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="remove(e)">删除</el-button>
          </div>
        </div>
        <EmptyState v-if="!list.length && !loading" text="还没有分享过经验" />
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
  flex-wrap: wrap;
  gap: 10px;
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
}
.row-main {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
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
