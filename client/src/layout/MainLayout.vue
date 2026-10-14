<template>
  <!-- 主框架：深色渐变侧边栏 + 顶部导航 + 内容区 -->
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '72px' : '232px'" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="22"><Shop /></el-icon>
        </div>
        <transition name="fade">
          <div v-if="!collapsed" class="logo-text">
            <strong>ZYR SHOP</strong>
            <span>商城管理后台</span>
          </div>
        </transition>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        class="side-menu"
      >
        <el-menu-item
          v-for="item in menus"
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>

      <div class="side-footer" v-if="!collapsed">
        <span>© 2026 ZYR SHOP</span>
      </div>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <span class="header-title">{{ route.meta.title }}</span>
        </div>

        <div class="header-right">
          <el-tag type="warning" effect="dark" round size="small">
            管理员
          </el-tag>

          <el-dropdown trigger="click" @command="onCommand">
            <div class="user-box">
              <el-avatar :size="34" class="user-avatar">
                <el-image
                  v-if="userStore.avatar"
                  :src="userStore.avatar"
                  fit="cover"
                />
                <el-icon v-else><UserFilled /></el-icon>
              </el-avatar>
              <span class="user-name">{{ userStore.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><Setting /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="front" divided>
                  <el-icon><Monitor /></el-icon>前台首页
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区：路由出口 -->
      <el-main class="content">
        <router-view v-slot="{ Component, route }">
          <transition name="page">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
/**
 * 主布局组件：侧边菜单、顶部用户区
 * @author zyr
 */
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const collapsed = ref(false)

// 全部菜单项（商城后台所有页面均限管理员）
const allMenus = [
  { path: '/dashboard', title: '数据看板', icon: 'DataAnalysis' },
  { path: '/product', title: '商品管理', icon: 'Goods' },
  { path: '/order', title: '订单管理', icon: 'List' },
  { path: '/user', title: '用户管理', icon: 'UserFilled' },
  { path: '/knowledge', title: '知识库管理', icon: 'FolderOpened' },
  { path: '/chat', title: '智能客服', icon: 'ChatDotRound' }
]

// 菜单（已登录管理员可见全部）
const menus = computed(() => allMenus)

/**
 * 下拉菜单命令处理
 */
const onCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'front') {
    // 跳到普通用户前台首页
    window.open('http://localhost:8081/', '_blank')
  } else if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

/* 侧边栏：深蓝紫渐变 */
.sidebar {
  background: linear-gradient(180deg, var(--zyr-sidebar-1), var(--zyr-sidebar-2));
  transition: width 0.28s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Logo 区域 */
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  color: #fff;
  flex-shrink: 0;
}
.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--zyr-primary), var(--zyr-cyan));
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.45);
}
.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.logo-text strong {
  font-size: 17px;
  letter-spacing: 1px;
}
.logo-text span {
  font-size: 11px;
  opacity: 0.65;
}

/* 菜单样式 */
.side-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 8px 12px;
}
.side-menu :deep(.el-menu-item) {
  color: #c7d2fe;
  border-radius: 10px;
  margin-bottom: 6px;
  height: 46px;
}
.side-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}
.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(120deg, var(--zyr-primary), var(--zyr-primary-2));
  color: #fff;
  box-shadow: 0 6px 18px rgba(99, 102, 241, 0.5);
}
.side-menu :deep(.el-menu--collapse .el-menu-item) {
  margin-left: 0;
}

.side-footer {
  color: #818cf8;
  font-size: 12px;
  text-align: center;
  padding: 14px 0;
  opacity: 0.7;
}

/* 顶部栏 */
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(31, 41, 55, 0.05);
  z-index: 10;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #6b7280;
}
.collapse-btn:hover {
  color: var(--zyr-primary);
}
.header-title {
  font-size: 17px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.user-box {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 10px;
  transition: background 0.2s;
}
.user-box:hover {
  background: #f4f5fb;
}
.user-avatar {
  background: linear-gradient(135deg, var(--zyr-primary), var(--zyr-cyan));
  color: #fff;
}
.user-name {
  font-size: 14px;
}

/* 内容区 */
.content {
  background: var(--zyr-bg);
  padding: 20px;
  overflow-y: auto;
  position: relative;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.page-enter-active,
.page-leave-active {
  transition: all 0.22s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
.page-leave-active {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  pointer-events: none;
}
</style>
