<template>
  <div class="ai-copilot">
    <!-- Floating Button -->
    <el-button 
      class="copilot-btn" 
      type="primary" 
      circle 
      size="large"
      @click="toggleChat"
    >
      <el-icon :size="24"><ChatDotRound /></el-icon>
    </el-button>

    <!-- Chat Window -->
    <transition name="slide-up">
      <div v-if="isOpen" class="chat-window">
        <div class="chat-header">
          <div class="header-left">
            <span class="header-title">云转桥 Agent</span>
            <el-tag v-if="agentStatus" size="small" type="warning" class="status-tag">
              {{ agentStatus }}
            </el-tag>
          </div>
          <div class="header-actions">
            <el-button link type="warning" size="small" @click="resetChat" :disabled="isLoading">
              重新开始
            </el-button>
            <el-icon class="close-btn" @click="toggleChat"><Close /></el-icon>
          </div>
        </div>
        <div class="chat-body" ref="chatBody">
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="avatar" v-if="msg.role === 'ai'">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="content" v-if="msg.role === 'ai'">
              <div v-html="renderMarkdown(msg.text)"></div>
              <div v-if="msg.action" class="nav-action">
                <el-button type="primary" size="small" @click="navigateTo(msg.action.path)">
                  点击跳转 → {{ getPathName(msg.action.path) }}
                </el-button>
              </div>
            </div>
            <div class="content" v-else>{{ msg.text }}</div>
          </div>
          <div v-if="isWaiting" class="message ai waiting-message">
            <div class="avatar"><el-icon><Cpu /></el-icon></div>
            <div class="content waiting-content">
              <span class="pulse-dot"></span>
              <span class="waiting-text">{{ waitingText }}</span>
            </div>
          </div>
          <div v-if="isLoading && !isWaiting" class="message ai">
            <div class="avatar"><el-icon><Cpu /></el-icon></div>
            <div class="content streaming">
              <span v-if="streamBuffer">{{ streamBuffer }}</span>
              <span class="cursor-blink">|</span>
            </div>
          </div>
        </div>
        <div class="chat-actions">
          <el-button type="warning" plain size="small" @click="goToSmartMatch">
            去智能匹配 →
          </el-button>
          <span class="action-hint">如需精准匹配科技成果，建议前往智能匹配页面</span>
        </div>
        <div class="chat-footer">
          <el-input
            v-model="input"
            placeholder="描述您的技术需求，Agent 帮您精准对接..."
            @keyup.enter="sendMessage"
            :disabled="isLoading"
          >
            <template #append>
              <el-button @click="sendMessage" :disabled="isLoading">
                {{ isLoading ? '思考中' : '发送' }}
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Close, Cpu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isOpen = ref(false)
const input = ref('')
const isLoading = ref(false)
const streamBuffer = ref('')
const agentStatus = ref('')
const chatBody = ref<HTMLElement | null>(null)

// 会话ID，用于多轮对话记忆
const sessionId = ref('')

// 等待加载状态：发消息后、Agent 首次返回 token 之前
const isWaiting = ref(false)
const waitingStage = ref(0) // 0=分析需求, 1=搜索成果, 2=生成方案
let waitingTimer: ReturnType<typeof setInterval> | null = null

const waitingText = computed(() => {
  if (waitingStage.value === 2) return '正在生成转化方案'
  if (waitingStage.value === 1) return '正在搜索匹配的科技成果'
  return '正在分析您的需求'
})

const startWaiting = () => {
  isWaiting.value = true
  waitingStage.value = 0
  const startTime = Date.now()
  waitingTimer = setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000
    if (elapsed >= 8) {
      waitingStage.value = 2
    } else if (elapsed >= 3) {
      waitingStage.value = 1
    }
  }, 500)
}

const stopWaiting = () => {
  isWaiting.value = false
  if (waitingTimer !== null) {
    clearInterval(waitingTimer)
    waitingTimer = null
  }
}

onUnmounted(() => {
  stopWaiting()
})

interface Action {
  type: string;
  path: string;
}

interface Message {
  role: 'user' | 'ai';
  text: string;
  action?: Action;
}

const welcomeMsg = '你好！我是云转桥智能助手，可以帮你了解平台功能和解答使用问题。如需精准匹配科技成果，请前往**智能匹配**页面操作。'

const messages = ref<Message[]>([
  { role: 'ai', text: welcomeMsg }
])

const toggleChat = () => {
  isOpen.value = !isOpen.value
}

const goToSmartMatch = () => {
  toggleChat()
  router.push('/smart-match')
}

const navigateTo = (path: string) => {
  toggleChat()
  router.push(path)
  ElMessage.success('已为您跳转到目标页面')
}

const getPathName = (path: string): string => {
  const pathNames: Record<string, string> = {
    '/achievements': '成果大厅',
    '/match': '智能匹配',
    '/libraries/policies': '政策库',
    '/needs/publish': '发布需求',
    '/needs': '需求大厅',
    '/libraries/enterprises': '企业库',
    '/libraries/experts': '专家库',
    '/libraries/equipments': '设备库',
    '/libraries/patents': '专利库',
    '/libraries/funds': '资金库',
    '/profile': '个人中心',
    '/messages': '消息中心',
    '/': '首页',
    '/smart-match': '智能匹配'
  }
  return pathNames[path] || path
}

const resetChat = async () => {
  if (sessionId.value) {
    try {
      await fetch('/api/ai/agent/reset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId: sessionId.value })
      })
    } catch (e) {
      // ignore
    }
  }
  sessionId.value = ''
  messages.value = [{ role: 'ai', text: welcomeMsg }]
  streamBuffer.value = ''
  agentStatus.value = ''
  isLoading.value = false
  stopWaiting()
  ElMessage.success('对话已重置')
}

const sendMessage = async () => {
  if (!input.value.trim() || isLoading.value) return

  const userMsg = input.value.trim()
  messages.value.push({ role: 'user', text: userMsg })
  input.value = ''
  isLoading.value = true
  streamBuffer.value = ''
  agentStatus.value = ''
  startWaiting()
  scrollToBottom()

  // 添加临时 AI 消息用于流式填充
  const aiMsgIndex = messages.value.length
  messages.value.push({ role: 'ai', text: '' })

  try {
    const res = await fetch('/api/ai/agent/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        sessionId: sessionId.value || undefined,
        message: userMsg
      })
    })

    if (!res.ok) {
      throw new Error(`HTTP ${res.status}`)
    }

    const reader = res.body?.getReader()
    if (!reader) {
      throw new Error('No response body')
    }

    const decoder = new TextDecoder()
    let buffer = ''
    let fullResponse = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('event:')) {
          const eventType = line.substring(6).trim()
          if (eventType === 'token' && isWaiting.value) {
            stopWaiting()
          }
          agentStatus.value = getStatusForEvent(eventType)
        } else if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          try {
            const parsed = JSON.parse(data)
            if (parsed.sessionId && !sessionId.value) {
              sessionId.value = parsed.sessionId
            }
          } catch (e) {
            fullResponse += data
            streamBuffer.value += data
            messages.value[aiMsgIndex].text += data
          }
        }
      }
      scrollToBottom()
    }

    // 流结束，尝试解析完整响应为 JSON
    try {
      const parsedResponse = JSON.parse(fullResponse)
      if (parsedResponse.reply) {
        messages.value[aiMsgIndex].text = parsedResponse.reply
      }
      if (parsedResponse.action && parsedResponse.action.type === 'NAVIGATE' && parsedResponse.action.path) {
        messages.value[aiMsgIndex].action = {
          type: parsedResponse.action.type,
          path: parsedResponse.action.path
        }
      }
    } catch (e) {
      // 非 JSON 格式，保持原样
    }

    agentStatus.value = ''
    streamBuffer.value = ''

  } catch (e: any) {
    console.error('Agent chat error:', e)
    messages.value[aiMsgIndex].text = '抱歉，Agent 服务暂时不可用，请稍后再试。'
    agentStatus.value = ''
    streamBuffer.value = ''
  } finally {
    isLoading.value = false
    stopWaiting()
    scrollToBottom()
  }
}

const getStatusForEvent = (event: string): string => {
  switch (event) {
    case 'session': return '已连接'
    case 'token': return '生成中…'
    case 'done': return ''
    case 'error': return '出错了'
    default: return ''
  }
}

/**
 * 简易 Markdown 渲染（支持粗体、标题、列表、分段）
 */
const renderMarkdown = (text: string): string => {
  if (!text) return ''
  let html = text
    // 转义 HTML
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // 粗体 **text**
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    // 标题 ## text
    .replace(/^### (.+)$/gm, '<h3 class="md-h3">$1</h3>')
    .replace(/^## (.+)$/gm, '<h2 class="md-h2">$1</h2>')
    .replace(/^# (.+)$/gm, '<h1 class="md-h1">$1</h1>')
    // 无序列表 - item
    .replace(/^- (.+)$/gm, '<li class="md-li">$1</li>')
    // 有序列表 1. item
    .replace(/^\d+\.\s+(.+)$/gm, '<li class="md-li">$1</li>')
    // 换行
    .replace(/\n\n/g, '<br/><br/>')
    .replace(/\n/g, '<br/>')

  return html
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.ai-copilot {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 9999;
}

.copilot-btn {
  width: 60px;
  height: 60px;
  box-shadow: 0 4px 12px var(--gold-glow);
  background: linear-gradient(135deg, var(--gold-primary), var(--gold-secondary));
  border: none;
  font-size: 24px;
  transition: transform 0.3s;
  color: #000;
}

.copilot-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px var(--gold-glow);
}

.chat-window {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 400px;
  height: 550px;
  background: var(--bg-card);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.chat-header {
  background: var(--bg-secondary);
  padding: 12px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  color: var(--gold-primary);
  font-weight: bold;
  font-size: 15px;
}

.status-tag {
  font-size: 11px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.close-btn {
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 18px;
}

.close-btn:hover {
  color: #fff;
}

.chat-body {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background-color: var(--bg-primary);
}

.message {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  background: rgba(255, 215, 0, 0.1);
  color: var(--gold-primary);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 8px;
  flex-shrink: 0;
  border: 1px solid var(--gold-glow);
}

.message.user .content {
  background: linear-gradient(135deg, var(--gold-primary), var(--gold-secondary));
  color: #000;
  border-radius: 12px 12px 0 12px;
  font-weight: 500;
}

.message.ai .content {
  background: var(--bg-card);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: 12px 12px 12px 0;
}

.content {
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
  max-width: 75%;
  word-wrap: break-word;
}

/* 流式输出光标 */
.streaming {
  min-width: 40px;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
  color: var(--gold-primary);
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 等待加载状态样式 */
.waiting-content {
  background: rgba(64, 158, 255, 0.12) !important;
  border: 1px solid rgba(64, 158, 255, 0.25) !important;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 38px;
}

.pulse-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
  animation: pulse 1.4s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.3); }
}

.waiting-text {
  color: #a0cfff;
  font-size: 13px;
  white-space: nowrap;
}

.waiting-text::after {
  content: '';
  animation: ellipsis 1.5s steps(4, end) infinite;
}

@keyframes ellipsis {
  0% { content: ''; }
  25% { content: '.'; }
  50% { content: '..'; }
  75% { content: '...'; }
  100% { content: ''; }
}

/* Markdown 渲染样式 */
.content :deep(.md-h1) {
  font-size: 16px;
  font-weight: bold;
  color: var(--gold-primary);
  margin: 8px 0 4px;
}

.content :deep(.md-h2) {
  font-size: 15px;
  font-weight: bold;
  color: var(--gold-primary);
  margin: 6px 0 3px;
}

.content :deep(.md-h3) {
  font-size: 14px;
  font-weight: bold;
  margin: 4px 0 2px;
}

.content :deep(.md-li) {
  margin-left: 8px;
  list-style: disc inside;
}

.content :deep(strong) {
  color: var(--gold-primary);
}

.content :deep(br) {
  display: block;
  content: '';
  margin-top: 4px;
}

.nav-action {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.nav-action :deep(.el-button) {
  background: linear-gradient(135deg, var(--gold-primary), var(--gold-secondary));
  border: none;
  color: #000;
  font-size: 12px;
  padding: 4px 12px;
}

.nav-action :deep(.el-button:hover) {
  opacity: 0.9;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-top: 1px solid rgba(255,255,255,0.05);
}

.action-hint {
  font-size: 11px;
  color: #666;
}

.chat-footer {
  padding: 12px 15px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

.chat-footer :deep(.el-input__wrapper) {
  background-color: var(--bg-primary);
  box-shadow: none;
  border: 1px solid var(--border-color);
}

.chat-footer :deep(.el-input__inner) {
  color: #fff;
}

.chat-footer :deep(.el-input-group__append) {
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-left: none;
  color: var(--gold-primary);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
