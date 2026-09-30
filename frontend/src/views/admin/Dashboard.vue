<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { adminStats, adminTrend, adminCategory, adminLatest } from '../../api/admin'
import { formatMoney, formatTime } from '../../utils/format'

const router = useRouter()

// 图表配色(参考调色板, 分类色按固定顺序)
const COLORS = {
  series1: '#2a78d6', // blue - 分类槽1
  series2: '#eb6834', // orange - 分类槽2
  series3: '#1baf7a', // aqua - 分类槽3
  series4: '#eda100', // yellow - 分类槽4
  series5: '#e87ba4', // magenta - 分类槽5
  series6: '#008300', // green - 分类槽6
  ink: '#898781', // 坐标轴/刻度
  grid: '#e1e0d9', // 网格线
  text: '#52514e'
}
const CATEGORY_COLORS = [COLORS.series1, COLORS.series2, COLORS.series3, COLORS.series4, COLORS.series5, COLORS.series6]

const stats = ref(null)
const latest = ref([])
const days = ref(7)
const trendChart = ref(null)
const amountChart = ref(null)
const pieChart = ref(null)
let trendIns = null
let amountIns = null
let pieIns = null

const loadStats = async () => {
  stats.value = await adminStats()
  latest.value = await adminLatest(5)
}

const loadTrend = async () => {
  const data = await adminTrend(days.value)
  trendIns?.setOption({
    color: [COLORS.series1, COLORS.series2],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', lineStyle: { color: COLORS.grid } },
      backgroundColor: '#fff',
      borderColor: '#e1e0d9',
      textStyle: { color: COLORS.text }
    },
    legend: { top: 0, icon: 'rect', itemWidth: 10, itemHeight: 10, textStyle: { color: COLORS.text } },
    grid: { left: 8, right: 8, top: 34, bottom: 0, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#c3c2b7' } },
      axisLabel: { color: COLORS.ink }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: COLORS.ink },
      splitLine: { lineStyle: { color: COLORS.grid } }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        lineStyle: { width: 2 },
        data: data.userCounts,
        endLabel: { show: true, formatter: '{c}', color: COLORS.text, distance: 8 }
      },
      {
        name: '新增帖子',
        type: 'line',
        smooth: true,
        symbolSize: 8,
        lineStyle: { width: 2 },
        data: data.postCounts,
        endLabel: { show: true, formatter: '{c}', color: COLORS.text, distance: 8 }
      }
    ]
  })

  amountIns?.setOption({
    color: [COLORS.series1],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#fff',
      borderColor: '#e1e0d9',
      textStyle: { color: COLORS.text },
      valueFormatter: (v) => `¥${Number(v).toFixed(2)}`
    },
    grid: { left: 8, right: 8, top: 16, bottom: 0, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.dates,
      axisLine: { lineStyle: { color: '#c3c2b7' } },
      axisLabel: { color: COLORS.ink }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: COLORS.ink },
      splitLine: { lineStyle: { color: COLORS.grid } }
    },
    series: [
      {
        name: '捐助金额(元)',
        type: 'bar',
        barWidth: 18,
        data: data.donationAmounts,
        itemStyle: { borderRadius: [4, 4, 0, 0] }
      }
    ]
  })
}

const loadPie = async () => {
  const data = await adminCategory()
  pieIns?.setOption({
    color: CATEGORY_COLORS,
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e1e0d9',
      textStyle: { color: COLORS.text },
      formatter: '{b}: {c} 篇 ({d}%)'
    },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: COLORS.text } },
    series: [
      {
        name: '帖子分类',
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '44%'],
        itemStyle: { borderColor: '#fcfcfb', borderWidth: 2 },
        label: { color: COLORS.text, formatter: '{b}\n{c}篇' },
        labelLine: { lineStyle: { color: COLORS.grid } },
        data: data.map((d) => ({ name: d.name, value: d.value }))
      }
    ]
  })
}

const openLatest = (item) => {
  const map = { POST: '/admin/post', EXPERIENCE: '/admin/experience', HELP: '/admin/help' }
  router.push(map[item.type] || '/admin/post')
}

const resize = () => {
  trendIns?.resize()
  amountIns?.resize()
  pieIns?.resize()
}

onMounted(async () => {
  await loadStats()
  await nextTick()
  trendIns = echarts.init(trendChart.value)
  amountIns = echarts.init(amountChart.value)
  pieIns = echarts.init(pieChart.value)
  await loadTrend()
  await loadPie()
  window.addEventListener('resize', resize)
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
  trendIns?.dispose()
  amountIns?.dispose()
  pieIns?.dispose()
})
</script>

<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div v-if="stats" class="stat-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats.userCount }}</div>
        <div class="stat-label">注册用户</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.postCount }}</div>
        <div class="stat-label">互助帖子</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.experienceCount }}</div>
        <div class="stat-label">康复经验</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.helpCount }}</div>
        <div class="stat-label">募捐中求助</div>
      </div>
      <div class="stat-card highlight">
        <div class="stat-value">¥{{ formatMoney(stats.donationTotal) }}</div>
        <div class="stat-label">累计捐助金额（{{ stats.donateCount }}人次）</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.pendingPostCount + stats.pendingExperienceCount + stats.pendingHelpCount }}</div>
        <div class="stat-label">待审核内容（帖子{{ stats.pendingPostCount }}·经验{{ stats.pendingExperienceCount }}·求助{{ stats.pendingHelpCount }}）</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.todayNewUser }}</div>
        <div class="stat-label">今日新增用户</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">¥{{ formatMoney(stats.todayDonationAmount) }}</div>
        <div class="stat-label">今日捐助金额</div>
      </div>
    </div>

    <!-- 趋势图 -->
    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">近{{ days }}天用户与帖子增长</span>
          <el-radio-group v-model="days" size="small" @change="loadTrend">
            <el-radio-button :value="7">7天</el-radio-button>
            <el-radio-button :value="30">30天</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="trendChart" class="chart-box" />
      </div>
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">近{{ days }}天捐助金额（元）</span>
        </div>
        <div ref="amountChart" class="chart-box" />
      </div>
    </div>

    <!-- 分类饼图 + 待审核 -->
    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-head"><span class="chart-title">帖子分类分布</span></div>
        <div ref="pieChart" class="chart-box" />
      </div>
      <div class="chart-card">
        <div class="chart-head"><span class="chart-title">最新待审核内容</span></div>
        <div v-if="latest.length" class="pending-list">
          <div v-for="item in latest" :key="item.type + item.id" class="pending-item" @click="openLatest(item)">
            <el-tag size="small" :type="item.type === 'POST' ? 'primary' : item.type === 'HELP' ? 'warning' : 'success'">
              {{ item.type === 'POST' ? '帖子' : item.type === 'HELP' ? '求助' : '经验' }}
            </el-tag>
            <span class="pending-title cjr-ellipsis">{{ item.title }}</span>
            <span class="pending-meta">{{ item.nickname }} · {{ formatTime(item.createTime) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无待审核内容" :image-size="60" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.stat-card.highlight {
  background: linear-gradient(135deg, #1f8a70, #2b9d86);
  color: #fff;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  color: #898781;
  font-size: 13px;
  margin-top: 6px;
}
.stat-card.highlight .stat-label {
  color: rgba(255, 255, 255, 0.85);
}
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 16px;
}
.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.chart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.chart-title {
  font-weight: 600;
  font-size: 15px;
}
.chart-box {
  height: 280px;
}
.pending-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.pending-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 6px;
  border-bottom: 1px solid var(--cjr-border);
  cursor: pointer;
}
.pending-item:hover {
  background: var(--cjr-bg);
}
.pending-title {
  flex: 1;
  font-size: 14px;
}
.pending-meta {
  color: #898781;
  font-size: 12px;
  white-space: nowrap;
}
@media (max-width: 1200px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}
</style>
