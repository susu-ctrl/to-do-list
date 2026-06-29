import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    // 从 localStorage 读取用户数据
    const storedUser = localStorage.getItem('user')
    const user = ref(storedUser ? JSON.parse(storedUser) : null)
    const token = ref(localStorage.getItem('token') || '')

    const setUser = (userData) => {
        user.value = userData
        token.value = userData.token
        localStorage.setItem('token', userData.token)
        localStorage.setItem('user', JSON.stringify(userData))
    }

    const clearUser = () => {
        user.value = null
        token.value = ''
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    const isLoggedIn = () => {
        return !!token.value
    }

    return { user, token, setUser, clearUser, isLoggedIn }
})