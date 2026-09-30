<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <el-icon :size="40" color="#1f8a70"><Service /></el-icon>
        <h1>残疾人互助交流平台</h1>
        <p>互助友爱 · 携手同行</p>
      </div>

      <el-tabs v-model="tab" stretch>
        <el-tab-pane label="账号登录" name="account">
          <el-form ref="formRef" :model="accountForm" :rules="accountRules" size="large" @keyup.enter="submitAccount">
            <el-form-item prop="username">
              <el-input v-model="accountForm.username" placeholder="用户名" :prefix-icon="'User'" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="accountForm.password" type="password" placeholder="密码" show-password :prefix-icon="'Lock'" />
            </el-form-item>
            <el-button type="primary" class="submit-btn" :loading="loading" @click="submitAccount">登 录</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="手机号登录" name="phone">
          <el-form ref="formRef" :model="phoneForm" :rules="phoneRules" size="large" @keyup.enter="submitPhone">
            <el-form-item prop="phone">
              <el-input v-model="phoneForm.phone" placeholder="手机号" :prefix-icon="'Phone'" maxlength="11" />
            </el-form-item>
            <el-form-item v-if="phoneMode === 'password'" prop="password">
              <el-input v-model="phoneForm.password" type="password" placeholder="密码" show-password :prefix-icon="'Lock'" />
            </el-form-item>
            <el-form-item v-else prop="code">
              <div class="code-row">
                <el-input v-model="phoneForm.code" placeholder="短信验证码" :prefix-icon="'Key'" maxlength="6" />
                <el-button :loading="codeLoading" @click="getCode">{{ codeSent ? '重新获取' : '获取验证码' }}</el-button>
              </div>
            </el-form-item>
            <el-button type="primary" class="submit-btn" :loading="loading" @click="submitPhone">登 录</el-button>
            <div class="mode-link" @click="phoneMode = phoneMode === 'password' ? 'code' : 'password'">
              {{ phoneMode === 'password' ? '使用验证码登录' : '使用密码登录' }}
            </div>
          </el-form>
        </el-tab-pane>

      </el-tabs>

      <div class="auth-footer">
        还没有账号？<router-link to="/register" class="link">立即注册</router-link>
      </div>
      <div class="demo-tip">
        演示账号：admin/123456（管理员）、worker/123456（社区工作者）、zhang/123456、li/123456、wang/123456
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { sendSmsCode } from '../../api/auth'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const tab = ref('account')
const formRef = ref()
const loading = ref(false)
const codeLoading = ref(false)
const codeSent = ref(false)

const accountForm = reactive({ username: '', password: '' })
const phoneForm = reactive({ phone: '', password: '', code: '' })
const phoneMode = ref('password') // password | code

const accountRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const afterLogin = (data) => {
  store.token = data.token
  store.user = data.user
  localStorage.setItem('cjr_token', data.token)
  ElMessage.success('登录成功，欢迎回来！')
  router.push('/home')
}

const submitAccount = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await store.login(accountForm)
    afterLogin(data)
  } finally {
    loading.value = false
  }
}

const submitPhone = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const { phoneLogin } = await import('../../api/auth')
    const payload = { phone: phoneForm.phone }
    if (phoneMode.value === 'code') payload.code = phoneForm.code
    else payload.password = phoneForm.password
    const data = await phoneLogin(payload)
    afterLogin(data)
  } finally {
    loading.value = false
  }
}

const getCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  codeLoading.value = true
  try {
    const data = await sendSmsCode(phoneForm.phone)
    codeSent.value = true
    // 演示模式：验证码直接返回，正式接入短信服务商后删除此行
    ElMessage.info('验证码（演示模式）：' + data.code)
  } finally {
    codeLoading.value = false
  }
}

</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f8a70 0%, #2b9d86 40%, #57c7ad 100%);
  padding: 16px;
}
.auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 36px 32px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}
.auth-brand {
  text-align: center;
  margin-bottom: 20px;
}
.auth-brand h1 {
  font-size: 22px;
  margin: 8px 0 4px;
  color: #2b3a36;
}
.auth-brand p {
  font-size: 13px;
  color: #8aa39a;
}
.submit-btn {
  width: 100%;
  margin-top: 4px;
  background: #1f8a70;
  border-color: #1f8a70;
}
.submit-btn:hover {
  background: #2b9d86;
  border-color: #2b9d86;
}
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.code-row .el-button {
  white-space: nowrap;
}
.mode-link {
  margin-top: 10px;
  text-align: center;
  font-size: 13px;
  color: #1f8a70;
  cursor: pointer;
}
.auth-footer {
  text-align: center;
  margin-top: 14px;
  font-size: 14px;
  color: #6b7f78;
}
.link {
  color: #1f8a70;
  text-decoration: none;
}
.demo-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #a8b8b2;
  text-align: center;
  line-height: 1.6;
}
</style>
