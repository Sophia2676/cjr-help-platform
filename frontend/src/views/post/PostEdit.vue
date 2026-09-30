<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost, updatePost, getPostDetail } from '../../api/post'
import { POST_CATEGORIES } from '../../utils/constants'
import ImageUploader from '../../components/ImageUploader.vue'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  title: '',
  category: '',
  content: '',
  images: []
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 50, message: '标题最长50字', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

onMounted(async () => {
  if (isEdit) {
    loading.value = true
    try {
      const data = await getPostDetail(route.params.id)
      form.title = data.title
      form.category = data.category
      form.content = data.content
      form.images = data.images || []
    } finally {
      loading.value = false
    }
  }
})

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit) {
      await updatePost(route.params.id, form)
      ElMessage.success('修改成功，重新进入审核')
    } else {
      await createPost(form)
      ElMessage.success('发布成功，等待审核')
    }
    router.push('/post/my')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="cjr-container cjr-page" v-loading="loading">
    <div class="cjr-card">
      <h2 class="page-title">{{ isEdit ? '编辑帖子' : '发布帖子' }}</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="一句话说清你的问题或分享" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option v-for="c in POST_CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            maxlength="5000"
            show-word-limit
            placeholder="详细描述你的情况、问题或经验…"
          />
        </el-form-item>
        <el-form-item label="图片（可选）">
          <ImageUploader v-model="form.images" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">
            {{ isEdit ? '保存修改' : '发布' }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" show-icon title="发布内容需管理员审核通过后才会公开展示" />
    </div>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0 0 20px;
}
</style>
