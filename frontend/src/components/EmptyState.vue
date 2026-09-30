<script setup>
defineProps({
  text: { type: String, default: '暂无数据' }
})
</script>

<template>
  <div class="cjr-empty">
    <el-empty :description="text" :image-size="80" />
  </div>
</template>
