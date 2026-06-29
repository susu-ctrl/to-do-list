<template>
  <div class="categories-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <h1>🏷️ 分类管理</h1>
        <span class="subtitle">管理员专属</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showDialog = true">
          <el-icon><Plus /></el-icon> 新建分类
        </el-button>
        <el-button @click="router.push('/tasks')">返回任务</el-button>
        <el-dropdown @command="handleCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="userStore.user?.avatarUrl || ''">
              {{ userStore.user?.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <span class="username">{{ userStore.user?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 分类列表 -->
    <div class="category-list">
      <el-table :data="categoryList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="分类名称" min-width="150">
          <template #default="{ row }">
            <span :style="{ color: row.color, fontWeight: 600 }">
              ● {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="color" label="颜色" width="120">
          <template #default="{ row }">
            <div class="color-preview" :style="{ backgroundColor: row.color }"></div>
            <span style="font-size:12px; color:#999; margin-left:8px;">{{ row.color }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="taskCount" label="任务数量" width="120" align="center" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="editCategory(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="deleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑分类弹窗 -->
    <el-dialog
        :title="editMode ? '编辑分类' : '新建分类'"
        v-model="showDialog"
        width="420px"
        @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称（如：学习、工作）" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <div class="color-picker-wrapper">
            <el-color-picker v-model="form.color" />
            <span style="margin-left: 12px; font-size: 13px; color: #999;">点击选择颜色</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCategory">
          {{ editMode ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// 检查是否为管理员
if (userStore.user?.role !== 1) {
  ElMessage.error('需要管理员权限')
  router.push('/tasks')
}

const categoryList = ref([])
const loading = ref(false)
const showDialog = ref(false)
const editMode = ref(false)
const editId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  color: '#6C3FF5'
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 20, message: '分类名称长度为1-20个字符', trigger: 'blur' }
  ]
}

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/categories')
    if (res.code === 200) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.name = ''
  form.color = '#6C3FF5'
  editMode.value = false
  editId.value = null
}

const editCategory = (row) => {
  editMode.value = true
  editId.value = row.id
  form.name = row.name
  form.color = row.color || '#6C3FF5'
  showDialog.value = true
}

const submitCategory = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const data = { name: form.name, color: form.color }
    let res
    if (editMode.value) {
      res = await request.put(`/api/categories/${editId.value}`, null, { params: data })
    } else {
      res = await request.post('/api/categories', null, { params: data })
    }
    if (res.code === 200) {
      ElMessage.success(editMode.value ? '分类已更新' : '分类已创建')
      showDialog.value = false
      loadCategories()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

const deleteCategory = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除分类「${row.name}」吗？${row.taskCount > 0 ? `\n该分类下有 ${row.taskCount} 个任务，需要先删除任务才能删除分类。` : ''}`,
        '提示',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: row.taskCount > 0 ? 'warning' : 'info'
        }
    )
    const res = await request.delete(`/api/categories/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('分类已删除')
      loadCategories()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.clearUser()
    ElMessage.success('已退出')
    router.push('/login')
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.categories-page {
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

.category-list {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.color-preview {
  display: inline-block;
  width: 30px;
  height: 30px;
  border-radius: 6px;
  border: 1px solid #e8ecf1;
  vertical-align: middle;
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
}
</style>