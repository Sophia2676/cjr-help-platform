/**
 * 智能语音播报服务（浏览器 Web Speech 语音合成）
 * 开启后自动朗读当前页面主要文字，页面切换自动朗读新页面，直到用户关闭
 */
import { reactive } from 'vue'

export const voiceState = reactive({
  enabled: false, // 播报服务是否开启
  speaking: false, // 当前是否正在朗读
})

let queue = []
let timer = null
let utterance = null
let voicesReady = false

function pickChineseVoice() {
  const voices = window.speechSynthesis?.getVoices() || []
  const zh = voices.find((v) => v.lang && v.lang.toLowerCase().startsWith('zh'))
  return zh || voices.find((v) => v.default) || null
}

function ensureVoices() {
  if (voicesReady) return
  voicesReady = true
  if (window.speechSynthesis && window.speechSynthesis.getVoices) {
    window.speechSynthesis.getVoices()
    // 部分浏览器异步加载语音列表
    window.speechSynthesis.onvoiceschanged = () => window.speechSynthesis.getVoices()
  }
}

/** 提取页面主要文字（过滤按钮/表单/导航/图标等非内容节点） */
function extractPageText() {
  const SKIP = ['SCRIPT', 'STYLE', 'NOSCRIPT', 'BUTTON', 'INPUT', 'TEXTAREA', 'SELECT', 'OPTION', 'NAV', 'HEADER', 'FOOTER', 'SVG', 'PATH', 'IFRAME', 'TEXTAREA']
  const parts = []
  const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, {
    acceptNode(node) {
      let parent = node.parentElement
      while (parent) {
        if (parent.tagName && (SKIP.includes(parent.tagName) || parent.getAttribute('aria-hidden') === 'true')) {
          return NodeFilter.FILTER_REJECT
        }
        parent = parent.parentElement
      }
      const text = node.textContent.trim()
      return text && /[一-龥a-zA-Z0-9]/.test(text) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT
    },
  })
  while (walker.nextNode()) {
    const t = walker.currentNode.textContent.trim()
    if (t) parts.push(t)
  }
  return parts
}

/** 把长文本切成适合朗读的短句 */
function toChunks(parts) {
  const chunks = []
  let cur = ''
  for (const p of parts) {
    const cleaned = p.replace(/\s+/g, ' ').trim()
    if (!cleaned) continue
    if (cur.length + cleaned.length > 60) {
      if (cur) chunks.push(cur)
      cur = cleaned
    } else {
      cur = cur ? cur + '，' + cleaned : cleaned
    }
  }
  if (cur) chunks.push(cur)
  return chunks.slice(0, 120) // 单页最多读 120 句，防止过长
}

function speakChunk(text) {
  return new Promise((resolve) => {
    if (!window.speechSynthesis) {
      resolve()
      return
    }
    const u = new SpeechSynthesisUtterance(text)
    u.lang = 'zh-CN'
    u.rate = 1.0
    const v = pickChineseVoice()
    if (v) u.voice = v
    u.onend = () => resolve()
    u.onerror = () => resolve()
    utterance = u
    window.speechSynthesis.speak(u)
  })
}

async function runQueue() {
  while (queue.length && voiceState.enabled) {
    voiceState.speaking = true
    await speakChunk(queue.shift())
  }
  voiceState.speaking = false
}

/** 朗读指定页面文本（默认当前页面） */
export function speakPage() {
  if (!voiceState.enabled) return
  if (!window.speechSynthesis) {
    voiceState.enabled = false
    return
  }
  stopSpeak()
  queue = toChunks(extractPageText())
  runQueue()
}

/** 停止当前朗读 */
export function stopSpeak() {
  queue = []
  if (window.speechSynthesis) window.speechSynthesis.cancel()
  voiceState.speaking = false
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

/** 开启智能语音服务 */
export function startVoice() {
  if (!window.speechSynthesis) return false
  ensureVoices()
  voiceState.enabled = true
  timer = setTimeout(() => speakPage(), 300) // 等页面稳定后开始
  return true
}

/** 关闭智能语音服务（直到关闭才停止） */
export function closeVoice() {
  voiceState.enabled = false
  stopSpeak()
}
