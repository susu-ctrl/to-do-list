<template>
  <div class="stats-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <h1>📊 数据统计</h1>
        <span class="subtitle">个人任务分析</span>
      </div>
      <div class="header-right">
        <el-button @click="router.push('/tasks')">返回任务</el-button>
        <el-dropdown @command="handleCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="avatarUrl">
              {{ userStore.user?.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="username">{{ userStore.user?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 统计概览卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="4" v-for="stat in stats" :key="stat.label">
        <el-card class="stat-card" :class="stat.class">
          <div class="stat-number">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 效率分析和趋势图 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>📈 效率分析</span>
          </template>
          <div class="efficiency-grid">
            <div class="efficiency-item">
              <span class="efficiency-value">{{ efficiency.avgCompletionHours || 0 }}</span>
              <span class="efficiency-label">平均完成时长 (小时)</span>
            </div>
            <div class="efficiency-item">
              <span class="efficiency-value">{{ efficiency.onTimeRate || 0 }}%</span>
              <span class="efficiency-label">按时完成率</span>
            </div>
            <div class="efficiency-item">
              <span class="efficiency-value">{{ efficiency.overdueRate || 0 }}%</span>
              <span class="efficiency-label">逾期率</span>
            </div>
            <div class="efficiency-item">
              <span class="efficiency-value">{{ efficiency.completedCount || 0 }}</span>
              <span class="efficiency-label">已完成任务数</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>📉 完成趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

// 头像 URL
const avatarUrl = computed(() => {
  const user = localStorage.getItem('user')
  if (user) {
    const parsed = JSON.parse(user)
    if (parsed.avatarUrl) {
      if (parsed.avatarUrl.startsWith('http')) {
        return parsed.avatarUrl
      }
      return `http://localhost:8080${parsed.avatarUrl}`
    }
  }
  return ''
})

// 统计数据
const stats = ref([
  { label: '总任务', value: 0, class: 'total' },
  { label: '待办', value: 0, class: 'pending' },
  { label: '进行中', value: 0, class: 'in-progress' },
  { label: '已完成', value: 0, class: 'completed' },
  { label: '逾期', value: 0, class: 'overdue' },
  { label: '完成率', value: '0%', class: 'rate' },
  { label: '今日完成', value: 0, class: 'today' }
])

const efficiency = ref({
  avgCompletionHours: 0,
  onTimeRate: 0,
  overdueRate: 0,
  completedCount: 0
})

const trendChartRef = ref(null)
let chartInstance = null

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await request.get('/api/stats/overview')
    if (res.code === 200) {
      const data = res.data
      stats.value[0].value = data.total || 0
      stats.value[1].value = data.pending || 0
      stats.value[2].value = data.inProgress || 0
      stats.value[3].value = data.completed || 0
      stats.value[4].value = data.overdue || 0
      stats.value[5].value = (data.completionRate || 0) + '%'
      stats.value[6].value = data.todayCompleted || 0
    }
  } catch (error) {
    ElMessage.error('加载统计失败')
  }
}

// 加载效率分析
const loadEfficiency = async () => {
  try {
    const res = await request.get('/api/stats/efficiency')
    if (res.code === 200) {
      efficiency.value = res.data
    }
  } catch (error) {
    // 忽略
  }
}

// 加载趋势数据
const loadTrends = async () => {
  try {
    const res = await request.get('/api/stats/trends', { params: { period: 'week' } })
    if (res.code === 200 && res.data?.data) {
      const trendData = res.data.data
      const dates = trendData.map(item => item.date)
      const completed = trendData.map(item => item.completed)

      await nextTick()
      if (trendChartRef.value) {
        if (!chartInstance) {
          chartInstance = echarts.init(trendChartRef.value)
        }
        chartInstance.setOption({
          tooltip: { trigger: 'axis' },
          xAxis: {
            type: 'category',
            data: dates,
            axisLabel: { fontSize: 11 }
          },
          yAxis: {
            type: 'value',
            minInterval: 1
          },
          series: [{
            name: '完成数',
            type: 'bar',
            data: completed,
            itemStyle: {
              color: '#6C3FF5',
              borderRadius: [4, 4, 0, 0]
            },
            barWidth: '40%'
          }],
          grid: {
            left: '8%',
            right: '8%',
            top: '10%',
            bottom: '15%'
          }
        })
        window.addEventListener('resize', () => chartInstance?.resize())
      }
    }
  } catch (error) {
    // 忽略
  }
}

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.clearUser()
    ElMessage.success('已退出')
    router.push('/login')
  }
}

onMounted(() => {
  loadStats()
  loadEfficiency()
  loadTrends()
})
</script>

<style scoped>
.stats-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px 40px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e8ecf1;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.subtitle {
  font-size: 13px;
  color: #6C3FF5;
  background: rgba(108, 63, 245, 0.1);
  padding: 2px 12px;
  border-radius: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-dropdown:hover {
  background: #e8ecf1;
}

.username {
  font-size: 14px;
  color: #333;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  border-radius: 12px;
  cursor: default;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card .stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-card .stat-label {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}

.stat-card.total .stat-number { color: #6C3FF5; }
.stat-card.pending .stat-number { color: #909399; }
.stat-card.in-progress .stat-number { color: #E6A23C; }
.stat-card.completed .stat-number { color: #67C23A; }
.stat-card.overdue .stat-number { color: #F56C6C; }
.stat-card.rate .stat-number { color: #409EFF; }
.stat-card.today .stat-number { color: #409EFF; }

.chart-row {
  margin-top: 16px;
}

.chart-card {
  border-radius: 12px;
  height: 320px;
}

.chart-card :deep(.el-card__body) {
  height: calc(100% - 55px);
}

.chart-container {
  width: 100%;
  height: 100%;
}

.efficiency-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  height: 100%;
  align-content: center;
}

.efficiency-item {
  text-align: center;
  padding: 16px;
  background: #f8f9fc;
  border-radius: 8px;
}

.efficiency-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
}

.efficiency-label {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}
</style>