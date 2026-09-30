<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminHelpPage, adminHelpAudit, adminHelpDelete } from '../../api/admin'
import { formatMoney, formatTime } from '../../utils/format'
import StatusTag from '../../components/StatusTag.vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '', status: null })

const auditVisible = ref(false)
const auditForm = reactive({ id: null, status: 1, rejectReason: '' })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.keyword) delete params.keyword
    if (params.status === null) delete params.status
    const data = await adminHelpPage(params)
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

const openAudit = (row, status) => {
  auditForm.id = row.id
  auditForm.status = status
  auditForm.rejectReason = ''
  auditVisible.value = true
}

const submitAudit = async () => {
  if (auditForm.status === 3 && !auditForm.rejectReason.trim()) {
    ElMessage.warning('驳回必须填写原因')
    return
  }
  await adminHelpAudit(auditForm.id, { status: auditForm.status, rejectReason: auditForm.rejectReason })
  ElMessage.success('审核完成')
  auditVisible.value = false
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm('确定删除该求助吗？相关捐助记录将一并删除', '提示', { type: 'warning' })
  await adminHelpDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

const openDetail = (row) => {
  window.open(`/#/help/detail/${row.id}`, '_blank')
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索求助标题" clearable class="kw" :prefix-icon="'Search'" @keyup.enter="search" @clear="search" />
      <el-select v-model="query.status" placeholder="全部状态" clearable class="sel" @change="search">
        <el-option label="待审核" :value="0" />
        <el-option label="募捐中" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已驳回" :value="3" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="求助标题" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="openDetail(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="求助人" width="110">
          <template #default="{ row }">{{ row.user?.nickname }}</template>
        </el-table-column>
        <el-table-column label="已筹/目标" width="140">
          <template #default="{ row }">¥{{ formatMoney(row.raisedAmount) }} / ¥{{ formatMoney(row.targetAmount) }}</template>
        </el-table-column>
        <el-table-column label="人次" width="70" prop="donateCount" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><StatusTag :status="row.status" kind="help" /></template>
        </el-table-column>
        <el-table-column label="发布时间" width="140">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" type="success" @click="openAudit(row, 1)">通过</el-button>
            <el-button v-if="row.status === 0" size="small" type="warning" @click="openAudit(row, 3)">驳回</el-button>
            <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > query.pageSize" class="cjr-pagination">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="changePage" />
      </div>
    </div>

    <el-dialog v-model="auditVisible" :title="auditForm.status === 1 ? '审核通过' : '驳回'" width="92%" :max-width="'440px'">
      <el-form v-if="auditForm.status === 3" label-position="top">
        <el-form-item label="驳回原因" required>
          <el-input v-model="auditForm.rejectReason" type="textarea" :rows="3" maxlength="255" placeholder="请填写驳回原因，将通知求助人" />
        </el-form-item>
      </el-form>
      <p v-else>确认通过该求助吗？通过后进入募捐状态。</p>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>
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
.sel {
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
