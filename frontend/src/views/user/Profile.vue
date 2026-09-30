<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProfile, updatePassword } from '../../api/user'
import { uploadFile } from '../../api/file'
import { useUserStore } from '../../stores/user'
import { DISABILITY_TYPES, GENDERS } from '../../utils/constants'

const store = useUserStore()

const profileForm = reactive({
  nickname: '',
  realName: '',
  avatar: '',
  gender: 0,
  disabilityType: '',
  disabilityLevel: null,
  phone: '',
  email: '',
  bio: ''
})
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const saving = ref(false)
const pwdSaving = ref(false)
const avatarUploading = ref(false)

onMounted(async () => {
  if (!store.user) await store.fetchInfo()
  Object.assign(profileForm, {
    nickname: store.user.nickname,
    realName: store.user.realName,
    avatar: store.user.avatar,
    gender: store.user.gender || 0,
    disabilityType: store.user.disabilityType || '',
    disabilityLevel: store.user.disabilityLevel || null,
    phone: store.user.phone || '',
    email: store.user.email || '',
    bio: store.user.bio || ''
  })
})

const saveProfile = async () => {
  if (!profileForm.nickname) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    await updateProfile(profileForm)
    ElMessage.success('保存成功')
    await store.fetchInfo()
  } finally {
    saving.value = false
  }
}

const uploadAvatar = async (opt) => {
  avatarUploading.value = true
  try {
    const res = await uploadFile(opt.file)
    profileForm.avatar = res.url
    opt.onSuccess(res)
  } catch (e) {
    opt.onError()
  } finally {
    avatarUploading.value = false
  }
}

const savePassword = async () => {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  pwdSaving.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = pwdForm.newPassword = pwdForm.confirmPassword = ''
    store.logout()
    location.href = '/login'
  } finally {
    pwdSaving.value = false
  }
}
</script>

<template>
  <div class="cjr-container cjr-page">
    <el-row :gutter="16">
      <el-col :xs="24" :md="14">
        <div class="cjr-card">
          <h2 class="section-title">个人资料</h2>
          <el-form label-position="top">
            <el-form-item label="头像">
              <el-upload
                :show-file-list="false"
                :http-request="uploadAvatar"
                accept="image/*"
              >
                <el-avatar :size="64" :src="profileForm.avatar || undefined">
                  {{ profileForm.nickname?.charAt(0) }}
                </el-avatar>
                <div class="avatar-tip" v-loading="avatarUploading">点击更换头像</div>
              </el-upload>
            </el-form-item>
            <el-row :gutter="12">
              <el-col :xs="24" :sm="12">
                <el-form-item label="昵称" required>
                  <el-input v-model="profileForm.nickname" maxlength="20" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="真实姓名">
                  <el-input v-model="profileForm.realName" maxlength="20" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="性别">
                  <el-radio-group v-model="profileForm.gender">
                    <el-radio :value="1">男</el-radio>
                    <el-radio :value="2">女</el-radio>
                    <el-radio :value="0">保密</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="残疾类别">
                  <el-select v-model="profileForm.disabilityType" clearable placeholder="无残疾可不选">
                    <el-option v-for="d in DISABILITY_TYPES" :key="d" :label="d" :value="d" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="残疾等级">
                  <el-select v-model="profileForm.disabilityLevel" clearable placeholder="无残疾可不选">
                    <el-option v-for="n in 4" :key="n" :label="`${n}级`" :value="n" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone" maxlength="20" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" maxlength="100" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="个人简介">
              <el-input v-model="profileForm.bio" type="textarea" :rows="3" maxlength="500" show-word-limit />
            </el-form-item>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
          </el-form>
        </div>
      </el-col>

      <el-col :xs="24" :md="10">
        <div class="cjr-card">
          <h2 class="section-title">修改密码</h2>
          <el-form label-position="top">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码（6-20位）">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-button type="primary" :loading="pwdSaving" @click="savePassword">修改密码</el-button>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.section-title {
  margin: 0 0 18px;
  font-size: 18px;
}
.avatar-tip {
  font-size: 12px;
  color: var(--cjr-text-secondary);
  margin-top: 6px;
}
:deep(.el-select) {
  width: 100%;
}
</style>
