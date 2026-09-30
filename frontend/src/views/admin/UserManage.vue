<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminUserPage, adminUserStatus, adminUserRole, adminUserDelete, adminUserResetPassword } from '../../api/admin'
import { formatDateTime } from '../../utils/format'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '', role: '', status: null })

const pwdVisible = ref(false)
const pwdForm = reactive({ id: null, password: '' })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.keyword) delete params.keyword
    if (!params.role) delete params.role
    if (params.status === null) delete params.status
    const data = await adminUserPage(params)
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

const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(
    next === 0 ? `确定禁用用户「${row.nickname}」吗？禁用后其无法登录和操作` : `确定启用用户「${row.nickname}」吗？`,
    '提示',
    { type: 'warning' }
  )
  await adminUserStatus(row.id, next)
  ElMessage.success('操作成功')
  load()
}

const changeRole = async (row) => {
  const next = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  await ElMessageBox.confirm(`确定将「${row.nickname}」设置为${next === 'ADMIN' ? '管理员' : '普通用户'}吗？`, '提示', { type: 'warning' })
  await adminUserRole(row.id, next)
  ElMessage.success('设置成功')
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确定删除用户「${row.nickname}」吗？`, '提示', { type: 'warning' })
  await adminUserDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

const openResetPwd = (row) => {
  pwdForm.id = row.id
  pwdForm.password = ''
  pwdVisible.value = true
}

const submitResetPwd = async () => {
  if (pwdForm.password.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  await adminUserResetPassword(pwdForm.id, pwdForm.password)
  ElMessage.success('密码已重置')
  pwdVisible.value = false
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索用户名/昵称" clearable class="kw" :prefix-icon="'Search'" @keyup.enter="search" @clear="search" />
      <el-select v-model="query.role" placeholder="全部角色" clearable class="sel" @change="search">
        <el-option label="普通用户" value="USER" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable class="sel" @change="search">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="用户名" width="130" prop="username" />
        <el-table-column label="昵称" width="120" prop="nickname" />
        <el-table-column label="残疾类别" width="110">
          <template #default="{ row }">
            {{ row.disabilityType ? `${row.disabilityType}${row.disabilityLevel ? row.disabilityLevel + '级' : ''}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">{{ row.role === 'ADMIN' ? '管理员' : '用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.role !== 'ADMIN'" size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button v-if="row.role !== 'ADMIN'" size="small" @click="changeRole(row)">设为管理员</el-button>
            <el-button size="small" @click="openResetPwd(row)">重置密码</el-button>
            <el-button v-if="row.role !== 'ADMIN'" size="small" type="danger" plain @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > query.pageSize" class="cjr-pagination">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="changePage" />
      </div>
    </div>

    <el-dialog v-model="pwdVisible" title="重置密码" width="92%" :max-width="'400px'">
      <el-form label-position="top">
        <el-form-item label="新密码（6-20位）">
          <el-input v-model="pwdForm.password" maxlength="20" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确认重置</el-button>
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
