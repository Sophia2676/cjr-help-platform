<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/auth'
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '3-20位字母、数字或下划线', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入11位手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        value === form.password ? callback() : callback(new Error('两次输入的密码不一致'))
      },
      trigger: 'blur'
    }
  ]
}

const submit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <el-icon :size="40" color="#1f8a70"><Service /></el-icon>
        <h1>注册账号</h1>
        <p>加入残疾人互助交流大家庭</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3-20位字母数字下划线）" :prefix-icon="'User'" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称" :prefix-icon="'Postcard'" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（11位，用于手机号登录）" :prefix-icon="'Phone'" maxlength="11" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-20位）" show-password :prefix-icon="'Lock'" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password :prefix-icon="'Lock'" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="submit">注 册</el-button>
      </el-form>
      <div class="auth-footer">
        已有账号？<router-link to="/login" class="link">去登录</router-link>
      </div>
    </div>
  </div>
</template>

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
  margin-bottom: 24px;
}
.auth-brand h1 {
  font-size: 20px;
  margin: 10px 0 4px;
  color: #1f8a70;
}
.auth-brand p {
  color: var(--cjr-text-secondary);
  font-size: 13px;
}
.submit-btn {
  width: 100%;
}
.auth-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: var(--cjr-text-secondary);
}
.link {
  color: var(--el-color-primary);
}
</style>
