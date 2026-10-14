/**
 * 路由配置与登录/角色守卫
 * @author zyr
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis', admin: true }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/Product.vue'),
        meta: { title: '商品管理', icon: 'Goods', admin: true }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/Order.vue'),
        meta: { title: '订单管理', icon: 'List', admin: true }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', admin: true }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/Knowledge.vue'),
        meta: { title: '知识库管理', icon: 'FolderOpened', admin: true }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/Chat.vue'),
        meta: { title: '智能客服', icon: 'ChatDotRound', admin: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'Setting', hidden: true }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置守卫：
 * 1. 未登录访问业务页面 → 跳转登录页
 * 2. 普通用户访问管理员页面 → 跳转登录页（商城后台仅管理员可用）
 */
router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    return '/login'
  }
  // 商城管理后台所有页面均需管理员
  if (to.meta.admin && userStore.role !== 'ADMIN') {
    return '/login'
  }
  if (to.path === '/login' && userStore.token && userStore.role === 'ADMIN') {
    return '/dashboard'
  }
  return true
})

export default router
