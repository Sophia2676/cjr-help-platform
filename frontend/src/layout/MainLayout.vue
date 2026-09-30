<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getUnreadCount } from '../api/message'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const navs = computed(() => {
  const base = [
    { path: '/home', label: '首页' },
    { path: '/post', label: '互助交流' },
    { path: '/experience', label: '康复经验' },
    { path: '/help', label: '爱心捐助' },
    { path: '/policy', label: '政策资讯' }
  ]
  // 社区工作者专属入口
  if (store.isWorker) base.push({ path: '/help/worker', label: '工作者面板' })
  return base
})

const drawerVisible = ref(false)
const unread = ref(0)
let timer = null

const activePath = computed(() => {
  if (route.path.startsWith('/experience')) return '/experience'
  if (route.path.startsWith('/help') || route.path.startsWith('/donation')) return '/help'
  if (route.path.startsWith('/policy')) return '/policy'
  if (route.path.startsWith('/post')) return '/post'
  return '/home'
})

const loadUnread = async () => {
  if (!store.isLogin) {
    unread.value = 0
    return
  }
  try {
    const data = await getUnreadCount()
    unread.value = data.count
  } catch (e) {
    /* 忽略轮询错误 */
  }
}

const navigate = (path) => {
  drawerVisible.value = false
  router.push(path)
}

const handleCommand = (cmd) => {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'myPosts') router.push('/post/my')
  else if (cmd === 'myHelps') router.push('/help/my')
  else if (cmd === 'myDonations') router.push('/donation/my')
  else if (cmd === 'message') router.push('/message')
  else if (cmd === 'admin') router.push('/admin')
  else if (cmd === 'logout') {
    store.logout()
    router.push('/home')
  }
}

onMounted(() => {
  loadUnread()
  timer = setInterval(loadUnread, 60000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="main-layout">
    <header class="main-header">
      <div class="cjr-container header-inner">
        <div class="logo" @click="navigate('/home')">
          <el-icon :size="26" color="#1f8a70"><Service /></el-icon>
          <span class="logo-text">残疾人互助交流平台</span>
        </div>

        <nav class="desktop-nav">
          <a v-for="n in navs" :key="n.path" :class="{ active: activePath === n.path }" @click="navigate(n.path)">
            {{ n.label }}
          </a>
          <a v-if="store.isAdmin" class="admin-link" @click="navigate('/admin')">管理后台</a>
        </nav>

        <div class="header-actions">
          <el-badge v-if="store.isLogin" :value="unread" :hidden="unread === 0" class="bell">
            <el-icon :size="20" class="clickable" @click="navigate('/message')"><Bell /></el-icon>
          </el-badge>

          <template v-if="store.isLogin">
            <el-dropdown @command="handleCommand">
              <span class="user-chip">
                <el-avatar :size="30" :src="store.user?.avatar || undefined">
                  {{ store.user?.nickname?.charAt(0) }}
                </el-avatar>
                <span class="nickname cjr-ellipsis">{{ store.user?.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="myPosts">我的帖子</el-dropdown-item>
                  <el-dropdown-item command="myHelps">我的求助</el-dropdown-item>
                  <el-dropdown-item command="myDonations">我的捐助</el-dropdown-item>
                  <el-dropdown-item command="message">我的消息</el-dropdown-item>
                  <el-dropdown-item v-if="store.isAdmin" command="admin" divided>管理后台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button size="small" text @click="navigate('/login')">登录</el-button>
            <el-button size="small" type="primary" @click="navigate('/register')">注册</el-button>
          </template>

          <el-icon :size="22" class="hamburger clickable" @click="drawerVisible = true"><Menu /></el-icon>
        </div>
      </div>
    </header>

    <!-- 移动端侧滑菜单 -->
    <el-drawer v-model="drawerVisible" direction="ltr" size="240px" :with-header="false">
      <div class="mobile-nav">
        <div class="mobile-logo">残疾人互助交流平台</div>
        <a v-for="n in navs" :key="n.path" :class="{ active: activePath === n.path }" @click="navigate(n.path)">
          {{ n.label }}
        </a>
        <a v-if="store.isAdmin" @click="navigate('/admin')">管理后台</a>
        <template v-if="store.isLogin">
          <a @click="navigate('/message')">我的消息<el-badge v-if="unread" :value="unread" class="drawer-badge" /></a>
          <a @click="navigate('/profile')">个人中心</a>
          <a @click="navigate('/post/my')">我的帖子</a>
          <a @click="navigate('/help/my')">我的求助</a>
          <a @click="navigate('/donation/my')">我的捐助</a>
          <a class="logout" @click="handleCommand('logout')">退出登录</a>
        </template>
        <template v-else>
          <a @click="navigate('/login')">登录</a>
          <a @click="navigate('/register')">注册</a>
        </template>
      </div>
    </el-drawer>

    <main class="main-content">
      <router-view />
    </main>

    <footer class="main-footer">
      <div class="cjr-container">
        <p>残疾人互助交流平台 · 互助友爱 携手同行</p>
        <p class="footer-sub">仅供学习交流使用，捐助行为请理性辨别</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.main-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.header-inner {
  display: flex;
  align-items: center;
  height: var(--cjr-header-height);
  gap: 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  white-space: nowrap;
}
.logo-text {
  font-size: 17px;
  font-weight: 700;
  color: #1f8a70;
}
.desktop-nav {
  flex: 1;
  display: flex;
  gap: 4px;
}
.desktop-nav a {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 15px;
  color: var(--cjr-text);
  cursor: pointer;
}
.desktop-nav a:hover {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}
.desktop-nav a.active {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 600;
}
.admin-link {
  color: #e6a23c !important;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}
.bell {
  cursor: pointer;
}
.clickable {
  cursor: pointer;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}
.nickname {
  max-width: 90px;
  font-size: 14px;
}
.hamburger {
  display: none;
}
.main-content {
  flex: 1;
}
.main-footer {
  background: #2f3a38;
  color: #cfd8d5;
  padding: 20px 0;
  text-align: center;
  font-size: 14px;
}
.footer-sub {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
}
.mobile-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.mobile-logo {
  font-weight: 700;
  color: #1f8a70;
  padding: 12px 8px 16px;
  border-bottom: 1px solid var(--cjr-border);
  margin-bottom: 8px;
}
.mobile-nav a {
  padding: 11px 12px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}
.mobile-nav a.active {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 600;
}
.mobile-nav a.logout {
  color: #f56c6c;
  margin-top: 16px;
  border-top: 1px solid var(--cjr-border);
  border-radius: 0;
}
.drawer-badge {
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .desktop-nav {
    display: none;
  }
  .hamburger {
    display: inline-flex;
  }
  .logo-text {
    font-size: 15px;
  }
  .nickname {
    display: none;
  }
  .header-inner {
    gap: 12px;
  }
}
</style>
