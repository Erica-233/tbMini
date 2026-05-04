import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('@/views/Home.vue') },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') },
    { path: '/boards', component: () => import('@/views/BoardList.vue') },
    { path: '/b/:slug', component: () => import('@/views/BoardDetail.vue') },
    { path: '/p/:id', component: () => import('@/views/PostDetail.vue') },
    { path: '/u/:username', component: () => import('@/views/UserProfile.vue') },
    { path: '/settings/profile', component: () => import('@/views/ProfileSettings.vue'), meta: { requiresAuth: true } },
    { path: '/submit', component: () => import('@/views/CreatePost.vue'), meta: { requiresAuth: true } },
    { path: '/admin/mod-queue', component: () => import('@/views/AdminModQueue.vue'), meta: { requiresAdmin: true } }
  ]
})

router.beforeEach((to, from, next) => {
  const user = useUserStore()
  if (to.meta.requiresAuth && !user.token) {
    return next('/login')
  }
  if (to.meta.requiresAdmin && user.role !== 'ADMIN') {
    return next('/')
  }
  next()
})

export default router
