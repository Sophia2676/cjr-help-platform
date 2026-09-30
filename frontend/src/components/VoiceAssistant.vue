<template>
  <div class="voice-assistant">
    <el-tooltip :content="voiceState.enabled ? '关闭智能语音播报' : '一键智能语音播报（朗读页面文字）'" placement="left">
      <div
        class="voice-ball"
        :class="{ active: voiceState.enabled, speaking: voiceState.speaking }"
        @click="toggle"
      >
        <el-icon :size="24" v-if="voiceState.enabled"><Close /></el-icon>
        <el-icon :size="24" v-else><Microphone /></el-icon>
      </div>
    </el-tooltip>
    <span v-if="voiceState.enabled" class="voice-badge">智能语音播报中…</span>
  </div>
</template>

<script setup>
import { watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { voiceState, startVoice, closeVoice } from '../utils/voice'

const route = useRoute()

const toggle = () => {
  if (voiceState.enabled) {
    closeVoice()
    ElMessage.info('已关闭智能语音播报')
  } else {
    const ok = startVoice()
    if (!ok) {
      ElMessage.warning('当前浏览器不支持语音播报，请使用 Chrome / Edge 浏览器')
      return
    }
    ElMessage.success('智能语音播报已开启，将朗读本页文字，再次点击关闭')
  }
}

// 播报服务开启期间，切换页面自动朗读新页面
watch(
  () => route.fullPath,
  () => {
    if (voiceState.enabled) {
      setTimeout(() => import('../utils/voice').then((m) => m.speakPage()), 600)
    }
  }
)
</script>

<style scoped>
.voice-assistant {
  position: fixed;
  right: 20px;
  bottom: 88px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}
.voice-ball {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #ffffff;
  color: #1f8a70;
  border: 2px solid #1f8a70;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
  transition: all 0.2s;
}
.voice-ball.active {
  background: #1f8a70;
  color: #ffffff;
}
.voice-ball.speaking {
  animation: voice-pulse 1.2s ease-in-out infinite;
}
@keyframes voice-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.12); }
}
.voice-badge {
  font-size: 12px;
  color: #1f8a70;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  padding: 3px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}
</style>
