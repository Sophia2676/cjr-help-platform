<script setup>
import { computed } from 'vue'
import { POST_STATUS, HELP_STATUS } from '../utils/constants'

const props = defineProps({
  /** 状态值 */
  status: { type: Number, default: 0 },
  /** post=帖子/经验审核状态 help=求助状态 */
  kind: { type: String, default: 'post' }
})

const info = computed(() => {
  const map = props.kind === 'help' ? HELP_STATUS : POST_STATUS
  return map[props.status] || { label: '未知', type: 'info' }
})
</script>

<template>
  <el-tag :type="info.type" size="small">{{ info.label }}</el-tag>
</template>
