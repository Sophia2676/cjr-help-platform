<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminLogPage, adminLogClear } from '../../api/admin'
import { formatDateTime } from '../../utils/format'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '', startTime: '', endTime: '' })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.keyword) delete params.keyword
    if (!params.startTime) delete params.startTime
    if (!params.endTime) delete params.endTime
    const data = await adminLogPage(params)
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

const clearLogs = async () => {
  await ElMessageBox.confirm('将清理30天前的操作日志，确定继续吗？', '提示', { type: 'warning' })
  await adminLogClear()
  ElMessage.success('清理完成')
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索操作/用户名/接口" clearable class="kw" :prefix-icon="'Search'" @keyup.enter="search" @clear="search" />
      <el-date-picker v-model="query.startTime" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" class="date" @change="search" />
      <el-date-picker v-model="query.endTime" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" class="date" @change="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <div class="spacer" />
      <el-button type="danger" plain @click="clearLogs">清理30天前日志</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="操作人" width="110">
          <template #default="{ row }">{{ row.username || '匿名' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" prop="operation" />
        <el-table-column label="接口" width="200" prop="requestUri" show-overflow-tooltip />
        <el-table-column label="请求参数" min-width="220" prop="requestParams" show-overflow-tooltip />
        <el-table-column label="IP" width="130" prop="ip" show-overflow-tooltip />
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '异常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时(ms)" width="90" prop="costTime" />
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div v-if="total > query.pageSize" class="cjr-pagination">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="changePage" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
  align-items: center;
}
.kw {
  flex: 1;
  min-width: 180px;
  max-width: 300px;
}
.date {
  width: 140px;
}
.spacer {
  flex: 1;
}
.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}
</style>
