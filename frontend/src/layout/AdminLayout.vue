<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const menus = [
  { path: '/admin/dashboard', label: '数据概览', icon: 'Odometer' },
  { path: '/admin/post', label: '帖子审核', icon: 'Document' },
  { path: '/admin/experience', label: '经验审核', icon: 'Reading' },
  { path: '/admin/help', label: '求助审核', icon: 'Handshake' },
  { path: '/admin/donation', label: '捐助管理', icon: 'Money' },
  { path: '/admin/user', label: '用户管理', icon: 'User' },
  { path: '/admin/policy', label: '政策管理', icon: 'Memo' },
  { path: '/admin/log', label: '操作日志', icon: 'List' }
]

const drawerVisible = ref(false)
const activePath = computed(() => route.path)

const navigate = (path) => {
  drawerVisible.value = false
  router.push(path)
}

const handleCommand = (cmd) => {
  if (cmd === 'home') router.push('/home')
  else if (cmd === 'logout') {
    store.logout()
    router.push('/login')
  }
}
</script>

<template>
  <div class="admin-layout">
    <!-- 桌面侧边栏 -->
    <aside class="admin-aside">
      <div class="admin-logo" @click="navigate('/admin/dashboard')">
        <el-icon :size="24"><Service /></el-icon>
        <span>平台管理后台</span>
      </div>
      <el-menu :default-active="activePath" class="admin-menu" @select="navigate">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="admin-body">
      <header class="admin-header">
        <el-icon :size="22" class="hamburger clickable" @click="drawerVisible = true"><Menu /></el-icon>
        <div class="spacer" />
        <el-dropdown @command="handleCommand">
          <span class="user-chip">
            <el-avatar :size="28" :src="store.user?.avatar || undefined">
              {{ store.user?.nickname?.charAt(0) }}
            </el-avatar>
            <span>{{ store.user?.nickname }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="home">返回前台</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <!-- 移动端侧滑菜单 -->
      <el-drawer v-model="drawerVisible" direction="ltr" size="240px" :with-header="false">
        <div class="admin-logo" @click="navigate('/admin/dashboard')">平台管理后台</div>
        <el-menu :default-active="activePath" class="admin-menu" @select="navigate">
          <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
            <el-icon><component :is="m.icon" /></el-icon>
            <span>{{ m.label }}</span>
          </el-menu-item>
        </el-menu>
      </el-drawer>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}
.admin-aside {
  width: 220px;
  background: #263238;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
}
.admin-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-weight: 700;
  padding: 18px 20px;
  cursor: pointer;
}
.admin-menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #b0bec5;
  --el-menu-hover-bg-color: #37474f;
  --el-menu-active-color: #4db6ac;
}
.admin-body {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.admin-header {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 9;
}
.spacer {
  flex: 1;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}
.hamburger {
  display: none;
}
.clickable {
  cursor: pointer;
}
.admin-content {
  padding: 20px;
  overflow-x: auto;
}

@media (max-width: 992px) {
  .admin-aside {
    display: none;
  }
  .admin-body {
    margin-left: 0;
  }
  .hamburger {
    display: inline-flex;
  }
  .admin-content {
    padding: 12px;
  }
}
</style>
