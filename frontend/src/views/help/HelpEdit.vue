<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createHelp, updateHelp, getHelpDetail } from '../../api/help'
import ImageUploader from '../../components/ImageUploader.vue'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  title: '',
  description: '',
  targetAmount: null,
  contactPhone: '',
  images: []
})

const rules = {
  title: [
    { required: true, message: '请输入求助标题', trigger: 'blur' },
    { max: 50, message: '标题最长50字', trigger: 'blur' }
  ],
  description: [{ required: true, message: '请填写详细情况说明', trigger: 'blur' }],
  targetAmount: [{ required: true, message: '请填写目标金额', trigger: 'blur' }]
}

onMounted(async () => {
  if (isEdit) {
    loading.value = true
    try {
      const data = await getHelpDetail(route.params.id)
      form.title = data.title
      form.description = data.description
      form.targetAmount = Number(data.targetAmount)
      form.contactPhone = data.contactPhone || ''
      form.images = data.images || []
    } finally {
      loading.value = false
    }
  }
})

const submit = async () => {
  await formRef.value.validate()
  if (!form.targetAmount || form.targetAmount <= 0) {
    ElMessage.warning('目标金额必须大于0')
    return
  }
  submitting.value = true
  try {
    if (isEdit) {
      await updateHelp(route.params.id, form)
      ElMessage.success('修改成功，重新进入审核')
    } else {
      await createHelp(form)
      ElMessage.success('发布成功，等待审核')
    }
    router.push('/help/my')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="cjr-container cjr-page" v-loading="loading">
    <div class="cjr-card">
      <h2 class="page-title">{{ isEdit ? '编辑求助' : '发布求助' }}</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="求助标题" prop="title">
          <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="例如：电动轮椅电池更换求助" />
        </el-form-item>
        <el-form-item label="详细情况说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="8"
            maxlength="5000"
            show-word-limit
            placeholder="请如实详细说明您的困难情况、资金用途等…"
          />
        </el-form-item>
        <el-form-item label="目标金额（元）" prop="targetAmount">
          <el-input-number v-model="form.targetAmount" :min="1" :max="999999" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="联系电话（仅登录用户可见，可选）">
          <el-input v-model="form.contactPhone" maxlength="20" placeholder="方便爱心人士与您联系" />
        </el-form-item>
        <el-form-item label="证明材料图片（可选）">
          <ImageUploader v-model="form.images" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">
            {{ isEdit ? '保存修改' : '发布求助' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
      <el-alert type="warning" :closable="false" show-icon title="求助信息需管理员审核通过后进入募捐，请如实填写" />
    </div>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0 0 20px;
}
</style>
