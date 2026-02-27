<template>
  <el-container class="app-wrapper">
    <el-header class="app-header">
      <div class="logo-container">
        <div class="logo-icon-wrapper">
          <el-icon class="logo-icon" :size="24" color="#FFD700"><Connection /></el-icon>
        </div>
        <span class="logo-text">Cloud Bridge</span>
      </div>
      <el-menu 
        mode="horizontal" 
        router 
        :default-active="$route.path" 
        class="nav-menu"
        background-color="transparent"
        text-color="#a0a0a0"
        active-text-color="#FFD700"
        :ellipsis="false"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/needs">需求大厅</el-menu-item>
        <el-menu-item index="/achievements">成果大厅</el-menu-item>
        
        <el-sub-menu index="resources">
          <template #title>资源中心</template>
          <el-menu-item index="/libraries/experts">专家库</el-menu-item>
          <el-menu-item index="/libraries/policies">政策库</el-menu-item>
          <el-menu-item index="/libraries/funds">资金库</el-menu-item>
          <el-menu-item index="/libraries/equipments">设备库</el-menu-item>
          <el-menu-item index="/libraries/patents">专利库</el-menu-item>
          <el-menu-item index="/libraries/enterprises">企业库</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/match">智能匹配</el-menu-item>
      </el-menu>
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-profile-trigger">
              <el-avatar :size="32" :src="userStore.user?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
              <span class="username">{{ userStore.user?.username || '用户' }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="messages">消息中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" plain size="small" class="login-btn" @click="$router.push('/login')">登录 / 注册</el-button>
        </template>
      </div>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
    <AICopilot />
  </el-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import AICopilot from './components/AICopilot.vue'
import { useUserStore } from './stores/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  userStore.initializeFromStorage()
  if (userStore.isLoggedIn) {
    userStore.checkAuth()
  }
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'messages') {
    router.push('/messages')
  } else if (command === 'admin') {
    router.push('/admin')
  }
}
</script>

<style>
/* Global Reset */
body {
  margin: 0;
  padding: 0;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-family: 'Inter', 'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: rgba(10, 10, 10, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 200px;
}

.logo-icon-wrapper {
  width: 36px;
  height: 36px;
  background: var(--gold-dim);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--gold-glow);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(90deg, #fff, #aaa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 0.5px;
}

.nav-menu {
  flex: 1;
  justify-content: center;
  border-bottom: none !important;
  background: transparent !important;
}

.nav-menu .el-menu-item {
  font-size: 15px;
  background: transparent !important;
  transition: all 0.3s;
}

.nav-menu .el-menu-item:hover {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.05) !important;
}

.nav-menu .el-menu-item.is-active {
  font-weight: 600;
  position: relative;
}

.nav-menu .el-menu-item.is-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background-color: var(--gold-primary);
  border-radius: 2px;
}

.header-right {
  width: 200px;
  display: flex;
  justify-content: flex-end;
}

.login-btn {
  background: transparent;
  border-color: #444;
  color: #ccc;
}

.login-btn:hover {
  border-color: var(--gold-primary);
  color: var(--gold-primary);
  background: var(--gold-dim);
}

.user-profile-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-profile-trigger:hover {
  background: rgba(255, 255, 255, 0.1);
}

.username {
  color: #e0e0e0;
  font-size: 14px;
  font-weight: 500;
}

.app-main {
  padding: 0; /* Remove default padding to allow full-width pages */
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* Global Dropdown Override */
.el-dropdown-menu__item:not(.is-disabled):focus, 
.el-dropdown-menu__item:not(.is-disabled):hover {
  background-color: var(--gold-dim) !important;
  color: var(--gold-primary) !important;
}

/* Force dark theme for submenus */
.el-menu--popup {
  background-color: rgba(10, 10, 10, 0.95) !important;
  border: 1px solid var(--border-color) !important;
  backdrop-filter: blur(10px);
}

.el-menu--popup .el-menu-item {
  background-color: transparent !important;
  color: #e0e0e0 !important;
}

.el-menu--popup .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: var(--gold-primary) !important;
}

/* Dropdown dark theme */
.el-dropdown-menu {
  background-color: rgba(10, 10, 10, 0.95) !important;
  border: 1px solid var(--border-color) !important;
}

.el-dropdown-menu__item {
  color: #e0e0e0 !important;
}

.el-dropdown-menu__item--divided {
  border-top-color: var(--border-color) !important;
}

/* Global Scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
</style>
