<template>
  <div class="home">
    <h1>📋 欢迎使用待办任务管理系统</h1>
    <p>登录成功！</p>
    <el-button type="primary" @click="router.push('/tasks')">进入任务列表</el-button>
    <el-button type="danger" @click="logout">退出登录</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const logout = async () => {
  try {
    // 调用后端退出接口
    const token = localStorage.getItem('token')
    if (token) {
      await fetch('http://localhost:8080/api/auth/logout', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
    }
  } catch (error) {
    // 忽略退出接口错误
  } finally {
    userStore.clearUser()
    ElMessage.success('已退出')
    router.push('/login')
  }
}
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f7fa;
}
.home h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 16px;
}
.home p {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}
</style>