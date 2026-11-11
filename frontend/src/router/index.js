import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProfileView from '../views/ProfileView.vue'
import WorkspaceView from '../views/WorkspaceView.vue'
import WorkspaceLayout from '../views/WorkspaceLayout.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/workspace', component: WorkspaceLayout, meta: { requiresAuth: true },
      // 父路由只显示布局；空子路径默认跳转到笔记列表。
      children: [
        { path: '', redirect: '/workspace/notes' },
        { path: 'notes', component: WorkspaceView },
        { path: 'chat', component: WorkspaceView },
        { path: 'profile', component: ProfileView },
      ],
    },
    { path: '/', redirect: '/workspace/notes' },
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
  ],
})
// 页面守卫只负责跳转；真正的权限校验仍由 Java 和 Python 完成。
router.beforeEach((to) => {
  if (to.matched.some(record => record.meta.requiresAuth) && !useAuthStore().token) return '/login'
  if (to.path === '/login' && useAuthStore().token) return '/workspace/notes'
})

export default router
