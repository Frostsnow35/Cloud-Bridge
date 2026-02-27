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
          <span>AI 智能助手</span>
          <el-icon class="close-btn" @click="toggleChat"><Close /></el-icon>
        </div>
        <div class="chat-body" ref="chatBody">
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="avatar" v-if="msg.role === 'ai'">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="content">{{ msg.text }}</div>
          </div>
          <div v-if="isLoading" class="message ai">
            <div class="avatar"><el-icon><Cpu /></el-icon></div>
            <div class="content loading">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
        <div class="chat-footer">
          <el-input
            v-model="input"
            placeholder="请输入您的问题..."
            @keyup.enter="sendMessage"
          >
            <template #append>
              <el-button @click="sendMessage">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </transition>

    <!-- Feedback Dialog -->
    <el-dialog v-model="showFeedbackDialog" title="问题反馈" width="400px" append-to-body>
      <el-form :model="feedbackForm">
        <el-form-item label="反馈内容">
          <el-input 
            v-model="feedbackForm.content" 
            type="textarea" 
            :rows="4"
            placeholder="请描述您遇到的问题或建议..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showFeedbackDialog = false">取消</el-button>
          <el-button type="primary" @click="submitFeedback">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Close, Cpu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isOpen = ref(false)
const input = ref('')
const isLoading = ref(false)
const chatBody = ref<HTMLElement | null>(null)

// Feedback state
const showFeedbackDialog = ref(false)
const feedbackForm = ref({ content: '' })

interface Message {
  role: 'user' | 'ai';
  text: string;
}

const messages = ref<Message[]>([
  { role: 'ai', text: '你好！我是 Cloud Bridge 的智能助手。我可以帮您寻找技术成果、发布需求，或者解答平台使用问题。请问有什么可以帮您？' }
])

const toggleChat = () => {
  isOpen.value = !isOpen.value
}

const sendMessage = async () => {
  if (!input.value.trim()) return

  // Add User Message
  messages.value.push({ role: 'user', text: input.value })
  const userMsg = input.value
  input.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const res = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: userMsg })
    })

    let data
    const text = await res.text()
    try {
      data = JSON.parse(text)
    } catch (e) {
      // Fallback if response is not JSON
      console.warn('AI response is not JSON:', text)
      data = { 
        intent: 'CHAT', 
        reply: text || '抱歉，服务暂时不可用。',
        action: null
      }
    }
    
    // Check if the backend returns a simple string reply or a complex object
    if (typeof data === 'string') {
        data = { reply: data }
    }

    messages.value.push({ role: 'ai', text: data.reply || '抱歉，我没有理解您的意思。' })
    
    if (data.action) {
      handleAction(data.action)
    }

  } catch (e) {
    console.error('Chat error:', e)
    messages.value.push({ role: 'ai', text: '抱歉，连接服务器失败，请稍后再试。' })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const handleAction = (action: any) => {
  console.log('Executing action:', action)
  if (action.type === 'NAVIGATE') {
    if (action.payload && action.payload.path) {
      router.push(action.payload)
    }
  } else if (action.type === 'OPEN_FEEDBACK_FORM') {
    showFeedbackDialog.value = true
  }
}

const submitFeedback = async () => {
  if (!feedbackForm.value.content) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  
  // Mock submission
  // In real app: await fetch('/api/feedback', ...)
  setTimeout(() => {
    ElMessage.success('反馈已提交，感谢您的建议！')
    showFeedbackDialog.value = false
    feedbackForm.value.content = ''
  }, 500)
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
  width: 350px;
  height: 500px;
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
  color: var(--gold-primary);
  padding: 15px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
}

.close-btn {
  cursor: pointer;
  color: var(--text-secondary);
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
  line-height: 1.5;
  max-width: 75%;
  word-wrap: break-word;
}

.chat-footer {
  padding: 15px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

/* Input Override */
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

.loading span {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: var(--gold-primary);
  border-radius: 50%;
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading span:nth-child(1) { animation-delay: -0.32s; }
.loading span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
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
