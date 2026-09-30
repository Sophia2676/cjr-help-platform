<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDonationPage, adminDonationDelete } from '../../api/admin'
import { formatMoney, formatTime } from '../../utils/format'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '', helpRequestId: null })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.keyword) delete params.keyword
    if (!params.helpRequestId) delete params.helpRequestId
    const data = await adminDonationPage(params)
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

const remove = async (row) => {
  await ElMessageBox.confirm(
    '确定撤销该笔捐助吗？将回滚求助的已筹金额与人次',
    '提示',
    { type: 'warning' }
  )
  await adminDonationDelete(row.id)
  ElMessage.success('已撤销并回滚金额')
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索求助标题" clearable class="kw" :prefix-icon="'Search'" @keyup.enter="search" @clear="search" />
      <el-input v-model="query.helpRequestId" placeholder="求助ID" clearable class="id-input" @keyup.enter="search" @clear="search" />
      <el-button type="primary" @click="search">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="求助标题" min-width="180" prop="helpTitle" />
        <el-table-column label="捐助人" width="120" prop="donorNickname" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="留言" min-width="140" prop="message" show-overflow-tooltip />
        <el-table-column label="匿名" width="70">
          <template #default="{ row }">{{ row.isAnonymous === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 2" size="small" type="success">已确认</el-tag>
            <el-tag v-else size="small" type="warning">待确认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="捐助时间" width="140">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" plain @click="remove(row)">撤销</el-button>
          </template>
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
}
.kw {
  flex: 1;
  min-width: 180px;
  max-width: 300px;
}
.id-input {
  width: 130px;
}
.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}
</style>
