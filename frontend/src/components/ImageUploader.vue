<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '../api/file'

const props = defineProps({
  /** v-model: 图片路径数组 */
  modelValue: { type: Array, default: () => [] },
  limit: { type: Number, default: 6 }
})
const emit = defineEmits(['update:modelValue'])

const fileList = ref([])

// 编辑回显: modelValue 变化时同步 fileList
watch(
  () => props.modelValue,
  (val) => {
    const urls = val || []
    fileList.value = urls.map((url, i) => ({ name: `image-${i}`, url }))
  },
  { immediate: true, deep: true }
)

const beforeUpload = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  return true
}

const doUpload = async (opt) => {
  try {
    const res = await uploadFile(opt.file)
    opt.onSuccess(res)
  } catch (e) {
    opt.onError()
  }
}

const handleSuccess = (res, file) => {
  emit('update:modelValue', [...(props.modelValue || []), res.url])
}

const handleRemove = (file) => {
  const url = file.response?.url || file.url
  emit('update:modelValue', (props.modelValue || []).filter((u) => u !== url))
}
</script>

<template>
  <el-upload
    v-model:file-list="fileList"
    list-type="picture-card"
    :http-request="doUpload"
    :before-upload="beforeUpload"
    :on-success="handleSuccess"
    :on-remove="handleRemove"
    :limit="limit"
    multiple
  >
    <el-icon><Plus /></el-icon>
  </el-upload>
</template>
