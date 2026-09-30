<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHelpDetail, donate, completeHelp, deleteHelp } from '../../api/help'
import { confirmDonation } from '../../api/donation'
import { useUserStore } from '../../stores/user'
import { formatMoney, formatTime } from '../../utils/format'
import StatusTag from '../../components/StatusTag.vue'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const help = ref(null)
const loading = ref(true)
const donateVisible = ref(false)
const donating = ref(false)
const donateForm = reactive({ amount: null, message: '', isAnonymous: 0 })

const load = async () => {
  loading.value = true
  try {
    help.value = await getHelpDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

const isOwner = () => store.isLogin && help.value && store.user?.id === help.value.user?.id

const openDonate = () => {
  if (!store.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  donateForm.amount = null
  donateForm.message = ''
  donateForm.isAnonymous = 0
  donateVisible.value = true
}

const submitDonate = async () => {
  if (!donateForm.amount || donateForm.amount <= 0) {
    ElMessage.warning('请输入有效金额')
    return
  }
  donating.value = true
  try {
    const data = await donate(help.value.id, donateForm)
    ElMessage.success('捐助成功，感谢您的爱心！')
    donateVisible.value = false
    help.value.raisedAmount = data.raisedAmount
    help.value.donateCount = data.donateCount
    help.value.progress = data.progress
    help.value.status = data.status
    help.value.recentDonations = data.recentDonations
  } finally {
    donating.value = false
  }
}

const confirm = async (d) => {
  await ElMessageBox.confirm('确认已收到该笔捐助吗？', '确认收款', { type: 'warning' })
  await confirmDonation(d.id)
  ElMessage.success('已确认')
  help.value = await getHelpDetail(route.params.id)
}

const complete = async () => {
  await ElMessageBox.confirm('确认完成筹款吗？将通知所有捐助人', '完成筹款', { type: 'warning' })
  await completeHelp(help.value.id)
  ElMessage.success('已完成筹款')
  help.value = await getHelpDetail(route.params.id)
}

const remove = async () => {
  await ElMessageBox.confirm('确定删除该求助吗？', '提示', { type: 'warning' })
  await deleteHelp(help.value.id)
  ElMessage.success('删除成功')
  router.push('/help')
}

onMounted(load)
</script>

<template>
  <div class="cjr-container cjr-page" v-loading="loading">
    <div v-if="help" class="help-detail">
      <div class="cjr-card">
        <div class="head">
          <h1 class="title">{{ help.title }}</h1>
          <StatusTag :status="help.status" kind="help" />
        </div>
        <div class="user-row">
          <el-avatar :size="36" :src="help.user?.avatar || undefined">{{ help.user?.nickname?.charAt(0) }}</el-avatar>
          <div>
            <div class="name">{{ help.user?.nickname }}</div>
            <div class="time">{{ formatTime(help.createTime) }} 发布</div>
          </div>
          <div class="spacer" />
          <template v-if="isOwner()">
            <el-button v-if="help.status === 1" type="primary" size="small" @click="complete">完成筹款</el-button>
            <el-button v-if="help.status === 0 || help.status === 3" size="small" @click="router.push(`/help/edit/${help.id}`)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="remove">删除</el-button>
          </template>
        </div>

        <div class="amount-panel">
          <div class="raised">¥{{ formatMoney(help.raisedAmount) }}<span class="label">已筹金额</span></div>
          <div class="target">目标金额 ¥{{ formatMoney(help.targetAmount) }}</div>
          <div class="count">{{ help.donateCount }} 人次捐助</div>
        </div>
        <el-progress :percentage="help.progress" :stroke-width="14" :show-text="true" class="progress" />

        <div class="content cjr-content">{{ help.description }}</div>

        <div v-if="help.images?.length" class="images">
          <el-image
            v-for="(img, i) in help.images"
            :key="i"
            :src="img"
            :preview-src-list="help.images"
            :initial-index="i"
            fit="cover"
            class="help-img"
          />
        </div>

        <div v-if="help.status === 1" class="donate-bar">
          <el-button type="warning" size="large" round @click="openDonate">我要捐助</el-button>
          <span v-if="store.isLogin && help.contactPhone" class="phone">联系电话：{{ help.contactPhone }}</span>
          <span v-else-if="store.isLogin" class="phone">求助人未留联系电话</span>
        </div>
      </div>

      <!-- 捐助记录 -->
      <div class="cjr-card">
        <h3 class="section-title">捐助记录（{{ help.donateCount }}）</h3>
        <div v-if="help.recentDonations?.length">
          <div v-for="d in help.recentDonations" :key="d.id" class="donation-row">
            <el-avatar :size="30" :src="d.donorAvatar || undefined">{{ d.donorNickname?.charAt(0) }}</el-avatar>
            <div class="donation-main">
              <div class="donation-head">
                <span class="donor">{{ d.donorNickname }}</span>
                <span class="amount">¥{{ formatMoney(d.amount) }}</span>
              </div>
              <div v-if="d.message" class="msg">{{ d.message }}</div>
              <div class="time">{{ formatTime(d.createTime) }}
                <el-tag v-if="d.status === 2" size="small" type="success" effect="plain">已确认</el-tag>
              </div>
            </div>
            <el-button v-if="isOwner() && d.status === 1" size="small" type="success" plain @click="confirm(d)">确认收到</el-button>
          </div>
        </div>
        <div v-else class="cjr-empty">还没有捐助记录，期待您的爱心</div>
      </div>
    </div>

    <!-- 捐助弹窗 -->
    <el-dialog v-model="donateVisible" title="爱心捐助" width="92%" :max-width="'420px'">
      <div v-if="help" class="donate-dialog">
        <div class="dialog-help-title">{{ help.title }}</div>
        <div class="dialog-help-meta">已筹 ¥{{ formatMoney(help.raisedAmount) }} / 目标 ¥{{ formatMoney(help.targetAmount) }}</div>
        <el-form label-position="top">
          <el-form-item label="捐助金额（元）" required>
            <el-input-number v-model="donateForm.amount" :min="1" :precision="2" :step="50" class="amount-input" />
          </el-form-item>
          <el-form-item label="爱心留言（可选）">
            <el-input v-model="donateForm.message" type="textarea" :rows="2" maxlength="255" placeholder="给求助人的鼓励…" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="donateForm.isAnonymous" :true-value="1" :false-value="0">匿名捐助（显示为"爱心人士"）</el-checkbox>
          </el-form-item>
        </el-form>
        <el-button type="warning" class="donate-submit" :loading="donating" @click="submitDonate">确认捐助</el-button>
        <p class="donate-tip">温馨提示：请理性捐助，量力而行，本平台仅提供信息对接服务</p>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.title {
  font-size: 22px;
  margin: 0;
  flex: 1;
}
.user-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 14px 0;
  color: var(--cjr-text-secondary);
  font-size: 13px;
}
.name {
  color: var(--cjr-text);
  font-size: 14px;
}
.spacer {
  flex: 1;
}
.amount-panel {
  display: flex;
  gap: 24px;
  align-items: baseline;
  margin: 10px 0 6px;
}
.raised {
  font-size: 30px;
  font-weight: 700;
  color: #e6a23c;
}
.label {
  font-size: 13px;
  color: var(--cjr-text-secondary);
  margin-left: 6px;
}
.target,
.count {
  color: var(--cjr-text-secondary);
  font-size: 13px;
}
.progress {
  margin: 8px 0 16px;
}
.content {
  font-size: 15px;
}
.images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 14px 0;
}
.help-img {
  width: 220px;
  height: 160px;
  border-radius: 6px;
}
.donate-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  justify-content: center;
  margin-top: 18px;
  flex-wrap: wrap;
}
.phone {
  color: var(--cjr-text-secondary);
  font-size: 13px;
}
.section-title {
  margin: 0 0 14px;
}
.donation-row {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid var(--cjr-border);
  align-items: flex-start;
}
.donation-main {
  flex: 1;
  min-width: 0;
}
.donation-head {
  display: flex;
  justify-content: space-between;
}
.donor {
  font-weight: 600;
  font-size: 14px;
}
.amount {
  color: #e6a23c;
  font-weight: 600;
}
.msg {
  background: var(--cjr-bg);
  border-radius: 6px;
  padding: 6px 10px;
  margin: 6px 0;
  font-size: 13px;
}
.time {
  color: var(--cjr-text-secondary);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.dialog-help-title {
  font-weight: 600;
  margin-bottom: 4px;
}
.dialog-help-meta {
  color: var(--cjr-text-secondary);
  font-size: 13px;
  margin-bottom: 14px;
}
.amount-input {
  width: 100%;
}
.donate-submit {
  width: 100%;
}
.donate-tip {
  color: var(--cjr-text-secondary);
  font-size: 12px;
  text-align: center;
  margin-top: 12px;
}
@media (max-width: 768px) {
  .help-img {
    width: 100%;
    height: auto;
  }
  .amount-panel {
    gap: 12px;
  }
}
</style>
