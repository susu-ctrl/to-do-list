<template>
  <div class="profile-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <h1>👤 个人中心</h1>
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
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 个人中心内容 -->
    <div class="profile-content">
      <el-card class="profile-card">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="uploadAvatar"
              :before-upload="beforeAvatarUpload"
          >
            <img
                v-if="avatarUrl"
                :src="avatarUrl"
                class="avatar-img"
            />
            <el-avatar
                v-else
                :size="100"
                class="avatar"
            >
              {{ userStore.user?.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="avatar-hover">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
          <div class="avatar-info">
            <h3>{{ userStore.user?.username }}</h3>
            <p>{{ userStore.user?.role === 1 ? '管理员' : '普通用户' }}</p>
          </div>
        </div>

        <el-divider />

        <!-- 表单区域 -->
        <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            class="profile-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
                v-model="form.username"
                placeholder="请输入新用户名"
                maxlength="20"
                show-word-limit
            />
            <el-button
                type="primary"
                size="small"
                style="margin-left: 12px;"
                :loading="updating"
                @click="updateUsername"
            >
              修改
            </el-button>
          </el-form-item>

          <el-form-item label="原密码" prop="oldPassword">
            <el-input
                v-model="form.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
                v-model="form.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
                type="primary"
                :loading="updatingPassword"
                @click="updatePassword"
            >
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Camera } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// 计算属性：从 localStorage 获取头像 URL
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

const formRef = ref(null)
const updating = ref(false)
const updatingPassword = ref(false)

const form = reactive({
  username: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 修改用户名
const updateUsername = async () => {
  if (!form.username) {
    ElMessage.warning('请输入新用户名')
    return
  }
  if (form.username === userStore.user?.username) {
    ElMessage.warning('新用户名与当前用户名相同')
    return
  }
  updating.value = true
  try {
    const res = await request.put('/api/users/username', null, {
      params: { username: form.username }
    })
    if (res.code === 200) {
      ElMessage.success('用户名已更新')
      userStore.user.username = form.username
      // 更新 localStorage
      const storedUser = JSON.parse(localStorage.getItem('user') || '{}')
      storedUser.username = form.username
      localStorage.setItem('user', JSON.stringify(storedUser))
      form.username = ''
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败，请稍后再试')
  } finally {
    updating.value = false
  }
}

// 修改密码
const updatePassword = async () => {
  await formRef.value?.validate()
  updatingPassword.value = true
  try {
    const res = await request.put('/api/auth/password', null, {
      params: {
        oldPassword: form.oldPassword,
        newPassword: form.newPassword
      }
    })
    if (res.code === 200) {
      ElMessage.success('密码已修改，请重新登录')
      form.oldPassword = ''
      form.newPassword = ''
      form.confirmPassword = ''
      userStore.clearUser()
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败，请稍后再试')
  } finally {
    updatingPassword.value = false
  }
}

// 上传头像
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只支持图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const uploadAvatar = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await request.post('/api/users/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      ElMessage.success('头像已更新')
      // 更新 userStore
      userStore.user.avatarUrl = res.data.avatarUrl
      // 更新 localStorage
      const storedUser = JSON.parse(localStorage.getItem('user') || '{}')
      storedUser.avatarUrl = res.data.avatarUrl
      localStorage.setItem('user', JSON.stringify(storedUser))
      // 强制刷新页面（临时方案）
      window.location.reload()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败，请稍后再试')
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
  form.username = ''
})
</script>

<style scoped>
.profile-page {
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

.header-left h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
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

.profile-content {
  display: flex;
  justify-content: center;
}

.profile-card {
  width: 600px;
  max-width: 100%;
  padding: 20px;
  border-radius: 12px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 10px 0;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
}

.avatar-uploader .avatar-img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e8ecf1;
  transition: border-color 0.3s;
}

.avatar-uploader .avatar {
  border: 3px solid #e8ecf1;
  transition: border-color 0.3s;
}

.avatar-uploader:hover .avatar-img,
.avatar-uploader:hover .avatar {
  border-color: #6C3FF5;
}

.avatar-hover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-uploader:hover .avatar-hover {
  opacity: 1;
}

.avatar-hover .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.avatar-info h3 {
  font-size: 20px;
  color: #1a1a2e;
  margin: 0 0 4px 0;
}

.avatar-info p {
  color: #888;
  font-size: 14px;
  margin: 0;
}

.profile-form {
  padding: 10px 0;
}

.profile-form .el-form-item {
  margin-bottom: 20px;
}
</style>