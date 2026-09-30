<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMessagePage, markRead, markAllRead, deleteMessage } from '../../api/message'
import { MESSAGE_TYPES } from '../../utils/constants'
import { formatTime } from '../../utils/format'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const type = ref('')
const query = ref({ pageNum: 1, pageSize: 10 })

const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (type.value) params.type = type.value
    const data = await getMessagePage(params)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const changeType = () => {
  query.value.pageNum = 1
  load()
}

const changePage = (p) => {
  query.value.pageNum = p
  load()
}

const open = async (m) => {
  if (m.isRead === 0) {
    await markRead(m.id)
    m.isRead = 1
  }
  // 关联跳转: 帖子/求助
  if (m.relatedId) {
    if (m.type === 'DONATION') {
      router.push(`/help/detail/${m.relatedId}`)
    } else if (m.type === 'AUDIT') {
      router.push('/post/my')
    }
  }
}

const readAll = async () => {
  await markAllRead(type.value || null)
  ElMessage.success('已全部标为已读')
  load()
}

const remove = async (m) => {
  await deleteMessage(m.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<template>
  <div class="cjr-container cjr-page">
    <div class="cjr-card">
      <div class="head">
        <h2 class="title">我的消息</h2>
        <el-button size="small" @click="readAll">全部已读</el-button>
      </div>

      <el-tabs v-model="type" @tab-change="changeType">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane v-for="(label, key) in MESSAGE_TYPES" :key="key" :label="label" :name="key" />
      </el-tabs>

      <div v-loading="loading">
        <div v-for="m in list" :key="m.id" class="msg-row" :class="{ unread: m.isRead === 0 }" @click="open(m)">
          <span class="dot" v-if="m.isRead === 0" />
          <div class="msg-main">
            <div class="msg-head">
              <el-tag size="small" effect="plain">{{ MESSAGE_TYPES[m.type] || m.type }}</el-tag>
              <span class="msg-title cjr-ellipsis">{{ m.title }}</span>
              <span class="time">{{ formatTime(m.createTime) }}</span>
            </div>
            <div class="msg-content cjr-ellipsis-2">{{ m.content }}</div>
          </div>
          <el-button size="small" text type="danger" @click.stop="remove(m)">删除</el-button>
        </div>
        <EmptyState v-if="!list.length && !loading" text="暂无消息" />
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
  margin-bottom: 8px;
}
.title {
  margin: 0;
  font-size: 18px;
}
.msg-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 6px;
  border-bottom: 1px solid var(--cjr-border);
  cursor: pointer;
}
.msg-row.unread .msg-title {
  font-weight: 700;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  flex-shrink: 0;
}
.msg-main {
  flex: 1;
  min-width: 0;
}
.msg-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.msg-title {
  flex: 1;
  font-size: 14px;
}
.time {
  color: var(--cjr-text-secondary);
  font-size: 12px;
  white-space: nowrap;
}
.msg-content {
  color: var(--cjr-text-secondary);
  font-size: 13px;
  margin-top: 4px;
}
</style>
