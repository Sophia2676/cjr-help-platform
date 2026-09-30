<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminPolicyPage, adminPolicyCreate, adminPolicyUpdate, adminPolicyStatus, adminPolicyDelete } from '../../api/admin'
import { formatDateTime } from '../../utils/format'
import ImageUploader from '../../components/ImageUploader.vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10, keyword: '' })

const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ id: null, title: '', summary: '', content: '', coverImage: '', source: '' })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.keyword) delete params.keyword
    const data = await adminPolicyPage(params)
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

const openEdit = (row) => {
  editForm.id = row?.id || null
  editForm.title = row?.title || ''
  editForm.summary = row?.summary || ''
  editForm.content = row?.content || ''
  editForm.coverImage = row?.coverImage || ''
  editForm.source = row?.source || ''
  editVisible.value = true
}

const submitEdit = async () => {
  if (!editForm.title.trim() || !editForm.content.trim()) {
    ElMessage.warning('标题与正文不能为空')
    return
  }
  saving.value = true
  try {
    if (editForm.id) {
      await adminPolicyUpdate(editForm.id, editForm)
      ElMessage.success('保存成功')
    } else {
      await adminPolicyCreate(editForm)
      ElMessage.success('发布成功')
    }
    editVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await adminPolicyStatus(row.id, next)
  ElMessage.success(next === 1 ? '已上架' : '已下架')
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm('确定删除该政策资讯吗？', '提示', { type: 'warning' })
  await adminPolicyDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索政策标题" clearable class="kw" :prefix-icon="'Search'" @keyup.enter="search" @clear="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" :icon="'Plus'" @click="openEdit(null)">发布政策</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="标题" min-width="220" prop="title" show-overflow-tooltip />
        <el-table-column label="来源" width="160" prop="source" show-overflow-tooltip />
        <el-table-column label="浏览" width="80" prop="viewCount" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已发布' : '已下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > query.pageSize" class="cjr-pagination">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="changePage" />
      </div>
    </div>

    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑政策' : '发布政策'" width="92%" :max-width="'640px'">
      <el-form label-position="top">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="editForm.summary" maxlength="255" show-word-limit type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="正文" required>
          <el-input v-model="editForm.content" type="textarea" :rows="8" maxlength="20000" show-word-limit />
        </el-form-item>
        <el-form-item label="封面图">
          <ImageUploader :model-value="editForm.coverImage ? [editForm.coverImage] : []" :limit="1" @update:model-value="(v) => (editForm.coverImage = v[0] || '')" />
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="editForm.source" maxlength="100" placeholder="如：市残疾人联合会" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">{{ editForm.id ? '保存' : '发布' }}</el-button>
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
