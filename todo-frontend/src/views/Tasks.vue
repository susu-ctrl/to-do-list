<template>
  <div class="tasks-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <h1>📋 我的任务</h1>
        <span class="task-count">共 {{ total }} 个任务</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon> 新建任务
        </el-button>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0">
          <el-button @click="router.push('/reminders')">提醒</el-button>
        </el-badge>
        <el-button @click="router.push('/categories')">分类管理</el-button>
        <el-button @click="router.push('/stats')">数据统计</el-button>
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

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card" :class="{ active: filterStatus === null }" @click="filterStatus = null">
        <span class="stat-num">{{ total }}</span>
        <span class="stat-label">全部</span>
      </div>
      <div class="stat-card pending" :class="{ active: filterStatus === 0 }" @click="filterStatus = 0">
        <span class="stat-num">{{ statusCounts[0] || 0 }}</span>
        <span class="stat-label">待办</span>
      </div>
      <div class="stat-card in-progress" :class="{ active: filterStatus === 1 }" @click="filterStatus = 1">
        <span class="stat-num">{{ statusCounts[1] || 0 }}</span>
        <span class="stat-label">进行中</span>
      </div>
      <div class="stat-card completed" :class="{ active: filterStatus === 2 }" @click="filterStatus = 2">
        <span class="stat-num">{{ statusCounts[2] || 0 }}</span>
        <span class="stat-label">已完成</span>
      </div>
      <div class="stat-card overdue" :class="{ active: filterStatus === 3 }" @click="filterStatus = 3">
        <span class="stat-num">{{ statusCounts[3] || 0 }}</span>
        <span class="stat-label">逾期</span>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="task-list">
      <el-table :data="taskList" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="任务" min-width="200">
          <template #default="{ row }">
            <div class="task-title">
              <span :class="['priority-dot', getPriorityClass(row.priority)]"></span>
              <span :class="{ done: row.status === 2 }">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100">
          <template #default="{ row }">
            <span v-if="row.categoryName" :style="{ color: row.categoryColor }">
              {{ row.categoryName }}
            </span>
            <span v-else style="color: #ccc;">未分类</span>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="160">
          <template #default="{ row }">
            <span :class="{ overdue: row.isOverdue }">
              {{ row.dueDate ? formatDate(row.dueDate) : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="primary" @click="startTask(row.id)">开始</el-button>
              <el-button size="small" type="success" @click="completeDirect(row.id)">完成</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button size="small" type="success" @click="completeTask(row.id)">完成</el-button>
            </template>
            <template v-else-if="row.status === 2">
              <el-button size="small" @click="reopenTask(row.id)">重新打开</el-button>
            </template>
            <template v-else-if="row.status === 3">
              <el-button size="small" type="warning" @click="editTask(row)">延期</el-button>
            </template>
            <el-button size="small" type="primary" plain @click="editTask(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="deleteTask(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :total="total"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadTasks"
            @current-change="loadTasks"
        />
      </div>
    </div>

    <!-- 创建/编辑任务弹窗 -->
    <el-dialog
        :title="editMode ? '编辑任务' : '新建任务'"
        v-model="showCreateDialog"
        width="500px"
        @close="resetForm"
    >
      <el-form :model="taskForm" :rules="taskRules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="taskForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="请输入任务描述（可选）" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="taskForm.priority">
            <el-radio-button :label="0">低</el-radio-button>
            <el-radio-button :label="1">中</el-radio-button>
            <el-radio-button :label="2">高</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="taskForm.categoryId" placeholder="选择分类（可选）" clearable>
            <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
            >
              <span :style="{ color: cat.color }">{{ cat.name }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker
              v-model="taskForm.dueDate"
              type="datetime"
              placeholder="选择截止日期（可选）"
              value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="提醒时间" prop="remindOffset">
          <el-select v-model="taskForm.remindOffset">
            <el-option label="不提醒" :value="0" />
            <el-option label="提前15分钟" :value="15" />
            <el-option label="提前30分钟" :value="30" />
            <el-option label="提前1小时" :value="60" />
            <el-option label="提前3小时" :value="180" />
            <el-option label="提前1天" :value="1440" />
            <el-option label="提前3天" :value="4320" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTask">
          {{ editMode ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// 状态
const taskList = ref([])
const loading = ref(false)
const total = ref(0)
const unreadCount = ref(0)
const page = ref(1)
const size = ref(10)
const filterStatus = ref(null)
const categories = ref([])
const statusCounts = ref({})

// 创建/编辑弹窗
const showCreateDialog = ref(false)
const editMode = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const taskForm = reactive({
  title: '',
  description: '',
  priority: 1,
  categoryId: null,
  dueDate: null,
  remindOffset: 1440
})

const taskRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级' }]
}

// 方法
const loadTasks = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterStatus.value !== null) {
      params.status = filterStatus.value
    }
    const res = await request.get('/api/tasks', { params })
    if (res.code === 200) {
      taskList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

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

const loadUnreadCount = async () => {
  try {
    const res = await request.get('/api/reminders/unread-count')
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {}
}


const loadCategories = async () => {
  try {
    const res = await request.get('/api/categories')
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (error) {
    // 忽略分类加载失败
  }
}

const loadStats = async () => {
  try {
    const res = await request.get('/api/stats/overview')
    if (res.code === 200) {
      statusCounts.value = {
        0: res.data.pending || 0,
        1: res.data.inProgress || 0,
        2: res.data.completed || 0,
        3: res.data.overdue || 0
      }
    }
  } catch (error) {
    // 忽略统计加载失败
  }
}

const resetForm = () => {
  taskForm.title = ''
  taskForm.description = ''
  taskForm.priority = 1
  taskForm.categoryId = null
  taskForm.dueDate = null
  taskForm.remindOffset = 1440
  editMode.value = false
  editId.value = null
}

const editTask = (row) => {
  editMode.value = true
  editId.value = row.id
  taskForm.title = row.title
  taskForm.description = row.description || ''
  taskForm.priority = row.priority
  taskForm.categoryId = row.categoryId
  taskForm.dueDate = row.dueDate
  taskForm.remindOffset = row.remindOffset || 1440
  showCreateDialog.value = true
}

const submitTask = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const data = { ...taskForm }
    if (!data.dueDate) delete data.dueDate
    if (!data.categoryId) delete data.categoryId

    let res
    if (editMode.value) {
      res = await request.put(`/api/tasks/${editId.value}`, data)
    } else {
      res = await request.post('/api/tasks', data)
    }
    if (res.code === 200) {
      ElMessage.success(editMode.value ? '任务已更新' : '任务已创建')
      showCreateDialog.value = false
      loadTasks()
      loadStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

const startTask = async (id) => {
  try {
    const res = await request.put(`/api/tasks/${id}/start`)
    if (res.code === 200) {
      ElMessage.success('任务已开始')
      loadTasks()
      loadStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const completeTask = async (id) => {
  try {
    const res = await request.put(`/api/tasks/${id}/complete`)
    if (res.code === 200) {
      ElMessage.success('任务已完成')
      loadTasks()
      loadStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const completeDirect = async (id) => {
  try {
    const res = await request.put(`/api/tasks/${id}/complete-direct`)
    if (res.code === 200) {
      ElMessage.success('任务已完成')
      loadTasks()
      loadStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const reopenTask = async (id) => {
  try {
    const res = await request.put(`/api/tasks/${id}/reopen`)
    if (res.code === 200) {
      ElMessage.success('任务已重新打开')
      loadTasks()
      loadStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteTask = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.delete(`/api/tasks/${id}`)
    if (res.code === 200) {
      ElMessage.success('任务已删除')
      loadTasks()
      loadStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
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

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const getPriorityClass = (priority) => {
  return ['low', 'medium', 'high'][priority] || ''
}

const getStatusType = (status) => {
  const types = ['info', 'warning', 'success', 'danger']
  return types[status] || ''
}

// 监听筛选变化
import { watch } from 'vue'
watch(filterStatus, () => {
  page.value = 1
  loadTasks()
})

// 初始化
onMounted(() => {
  loadTasks()
  loadCategories()
  loadStats()
  loadUnreadCount()
})

</script>

<style scoped>
.tasks-page {
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

.task-count {
  color: #888;
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
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
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  text-align: center;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.stat-card.active { border-color: #6C3FF5; }
.stat-card .stat-num { display: block; font-size: 28px; font-weight: 700; color: #1a1a2e; }
.stat-card .stat-label { font-size: 13px; color: #888; }
.stat-card.pending .stat-num { color: #909399; }
.stat-card.in-progress .stat-num { color: #E6A23C; }
.stat-card.completed .stat-num { color: #67C23A; }
.stat-card.overdue .stat-num { color: #F56C6C; }

.task-list {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.task-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.priority-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.priority-dot.low { background: #67C23A; }
.priority-dot.medium { background: #E6A23C; }
.priority-dot.high { background: #F56C6C; }

.done { text-decoration: line-through; color: #ccc; }

.overdue { color: #F56C6C; font-weight: 600; }

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}
</style>