import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: () => import('@/views/Login.vue'),
            meta: { requiresAuth: false }
        },
        {
            path: '/',
            name: 'home',
            component: () => import('@/views/Home.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/tasks',
            name: 'tasks',
            component: () => import('@/views/Tasks.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/categories',
            name: 'categories',
            component: () => import('@/views/Categories.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/profile',
            name: 'profile',
            component: () => import('@/views/Profile.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/stats',
            name: 'stats',
            component: () => import('@/views/Stats.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/reminders',
            name: 'reminders',
            component: () => import('@/views/Reminders.vue'),
            meta: { requiresAuth: true }
        }
    ]
})

// 路由守卫：修复版本，不使用 next()
router.beforeEach((to, from) => {
    const userStore = useUserStore()
    const isLoggedIn = userStore.isLoggedIn()

    if (to.meta.requiresAuth && !isLoggedIn) {
        return '/login'
    }
    if (to.path === '/login' && isLoggedIn) {
        return '/'
    }
    return true
})

export default router