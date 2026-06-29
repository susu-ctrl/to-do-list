import request from '@/utils/request'

// 登录
export const login = (data) => {
    return request.post('/api/auth/login', data)
}

// 注册
export const register = (data) => {
    return request.post('/api/auth/register', data)
}

// 检查用户名
export const checkUsername = (username) => {
    return request.get('/api/auth/check-username', { params: { username } })
}