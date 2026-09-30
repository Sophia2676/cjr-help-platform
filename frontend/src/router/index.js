import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login', component: () => import('../views/auth/Login.vue'), meta: { guestOnly: true } },
  { path: '/register', component: () => import('../views/auth/Register.vue'), meta: { guestOnly: true } },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', component: () => import('../views/home/Home.vue') },
      { path: 'post', component: () => import('../views/post/PostList.vue') },
      { path: 'post/detail/:id', component: () => import('../views/post/PostDetail.vue') },
      { path: 'post/edit', component: () => import('../views/post/PostEdit.vue'), meta: { requiresAuth: true } },
      { path: 'post/edit/:id', component: () => import('../views/post/PostEdit.vue'), meta: { requiresAuth: true } },
      { path: 'post/my', component: () => import('../views/post/MyPosts.vue'), meta: { requiresAuth: true } },
      { path: 'experience', component: () => import('../views/experience/ExperienceList.vue') },
      { path: 'experience/detail/:id', component: () => import('../views/experience/ExperienceDetail.vue') },
      { path: 'experience/edit', component: () => import('../views/experience/ExperienceEdit.vue'), meta: { requiresAuth: true } },
      { path: 'experience/edit/:id', component: () => import('../views/experience/ExperienceEdit.vue'), meta: { requiresAuth: true } },
      { path: 'experience/my', component: () => import('../views/experience/MyExperiences.vue'), meta: { requiresAuth: true } },
      { path: 'help', component: () => import('../views/help/HelpList.vue') },
      { path: 'help/detail/:id', component: () => import('../views/help/HelpDetail.vue') },
      { path: 'help/edit', component: () => import('../views/help/HelpEdit.vue'), meta: { requiresAuth: true } },
      { path: 'help/edit/:id', component: () => import('../views/help/HelpEdit.vue'), meta: { requiresAuth: true } },
      { path: 'help/my', component: () => import('../views/help/MyHelps.vue'), meta: { requiresAuth: true } },
      { path: 'help/worker', component: () => import('../views/help/WorkerPanel.vue'), meta: { requiresAuth: true, requiresWorker: true } },
      { path: 'donation/my', component: () => import('../views/help/MyDonations.vue'), meta: { requiresAuth: true } },
      { path: 'policy', component: () => import('../views/policy/PolicyList.vue') },
      { path: 'policy/detail/:id', component: () => import('../views/policy/PolicyDetail.vue') },
      { path: 'message', component: () => import('../views/message/MessageList.vue'), meta: { requiresAuth: true } },
      { path: 'profile', component: () => import('../views/user/Profile.vue'), meta: { requiresAuth: true } },
      { path: 'user/:id', component: () => import('../views/user/UserHome.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAdmin: true },
    children: [
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'post', component: () => import('../views/admin/PostAudit.vue') },
      { path: 'experience', component: () => import('../views/admin/ExperienceAudit.vue') },
      { path: 'help', component: () => import('../views/admin/HelpAudit.vue') },
      { path: 'donation', component: () => import('../views/admin/DonationManage.vue') },
      { path: 'user', component: () => import('../views/admin/UserManage.vue') },
      { path: 'policy', component: () => import('../views/admin/PolicyManage.vue') },
      { path: 'log', component: () => import('../views/admin/LogList.vue') }
    ]
  },
  { path: '/wechat-callback', component: () => import('../views/auth/WechatCallback.vue'), meta: { guestOnly: true } },
  { path: '/no-access', component: () => import('../views/NoAccess.vue') },
  { path: '/:pathMatch(.*)*', component: () => import('../views/NotFound.vue') }
]

const router = createRouter({
  // hash 模式: 静态部署(单文件 jar)下刷新与深链不依赖服务端路由配置
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to) => {
  const store = useUserStore()
  // 刷新后 token 存在但用户信息丢失 -> 先拉取
  if (store.token && !store.user) {
    try {
      await store.fetchInfo()
    } catch (e) {
      store.logout()
    }
  }
  if (to.meta.requiresAuth && !store.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !store.isAdmin) {
    return { path: '/no-access' }
  }
  if (to.meta.requiresWorker && !store.isWorker) {
    return { path: '/no-access' }
  }
  if (to.meta.guestOnly && store.token) {
    return { path: '/' }
  }
  document.title = to.meta.title ? `${to.meta.title} - 残疾人互助交流平台` : '残疾人互助交流平台'
})

export default router
