<template>
  <div>
    <router-view />
    <!-- 一键智能语音播报（朗读所在页面文字，直到关闭） -->
    <VoiceAssistant />
  </div>
</template>

<script setup>
import VoiceAssistant from './components/VoiceAssistant.vue'
</script>
