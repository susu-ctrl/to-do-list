<template>
  <div class="reminders-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <h1>🔔 提醒消息</h1>
        <span class="badge" v-if="unreadCount > 0">{{ unreadCount }} 条未读</span>
      </div>
      <div class="header-right">
        <el-button type="primary" size="small" @click="markAllRead" :disabled="unreadCount === 0">
          全部标记已读
        </el-button>
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

    <!-- 提醒列表 -->
    <div class="reminder-list">
      <el-table :data="reminders" style="width: 100%" v-loading="loading">
        <el-table-column prop="message" label="消息" min-width="300" />
        <el-table-column prop="createTime" label="发送时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="isRead" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'warning'" size="small">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
                v-if="!row.isRead"
                size="small"
                type="primary"
                plain
                @click="markRead(row.id)"
            >
              标记已读
            </el-button>
            <span v-else style="color: #ccc;">-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="页"
            v-model:page-size="size"
            :total="total"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadReminders"
            @current-change="loadReminders"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

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

const reminders = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const unreadCount = ref(0)

const loadReminders = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/reminders', {
      params: { page: page.value, size: size.value }
    })
    if (res.code === 200) {
      reminders.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载提醒失败')
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await request.get('/api/reminders/unread-count')
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    // 忽略
  }
}

const markRead = async (id) => {
  try {
    const res = await request.put(`/api/reminders/${id}/read`)
    if (res.code === 200) {
      ElMessage.success('已标记为已读')
      loadReminders()
      loadUnreadCount()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const markAllRead = async () => {
  try {
    const res = await request.put('/api/reminders/read-all')
    if (res.code === 200) {
      ElMessage.success('全部标记为已读')
      loadReminders()
      loadUnreadCount()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
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
  loadReminders()
  loadUnreadCount()
})
</script>

<style scoped>
.reminders-page {
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
  gap: 12px;
}

.header-left h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.badge {
  background: #F56C6C;
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
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

.reminder-list {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}
</style>