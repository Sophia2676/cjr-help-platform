<template>
  <div class="cjr-container cjr-page">
    <div class="cjr-card panel-head">
      <div>
        <h2>社区工作者帮扶面板</h2>
        <p>查看全部求助信息并直接联系求助人（手机号仅社区工作者可见）</p>
      </div>
      <el-button @click="load">
        <el-icon style="margin-right: 4px"><Refresh /></el-icon>刷新
      </el-button>
    </div>

    <div class="cjr-card" v-loading="loading">
      <el-table :data="list" stripe style="width: 100%">
        <el-table-column prop="title" label="求助标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="求助人" width="130">
          <template #default="{ row }">{{ row.user?.nickname || '-' }}</template>
        </el-table-column>
        <el-table-column label="账号手机号" width="150">
          <template #default="{ row }">
            <a v-if="row.userPhone" class="phone-link" :href="'tel:' + row.userPhone">{{ row.userPhone }}</a>
            <span v-else class="muted">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column label="联系手机号" width="150">
          <template #default="{ row }">
            <a v-if="row.contactPhone" class="phone-link" :href="'tel:' + row.contactPhone">{{ row.contactPhone }}</a>
            <span v-else class="muted">未填写</span>
          </template>
        </el-table-column>
        <el-table-column label="筹款进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="120">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push('/help/detail/' + row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!list.length && !loading" description="暂无求助信息" />
      <div class="pager">
        <el-pagination
          v-if="total > query.pageSize"
          layout="prev, pager, next, total"
          :total="total"
          :page-size="query.pageSize"
          v-model:current-page="query.pageNum"
          @current-change="load"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWorkerHelpPage } from '../../api/help'
import { formatTime } from '../../utils/format'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = ref({ pageNum: 1, pageSize: 10 })

const statusText = (s) => ({ 0: '待审核', 1: '募捐中', 2: '已完成', 3: '已驳回' }[s] || '未知')
const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info')

const load = async () => {
  loading.value = true
  try {
    const data = await getWorkerHelpPage(query.value)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.panel-head h2 {
  margin: 0 0 4px;
  font-size: 18px;
}
.panel-head p {
  margin: 0;
  font-size: 13px;
  color: #8aa39a;
}
.phone-link {
  color: #1f8a70;
  font-weight: 600;
  text-decoration: none;
}
.muted {
  color: #b0bdb8;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
