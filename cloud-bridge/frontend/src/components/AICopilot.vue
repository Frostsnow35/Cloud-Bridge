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
            <div class="content" v-if="msg.role === 'ai'" v-html="renderMarkdown(msg.text, isLoading && index === messages.length - 1)"></div>
            <div class="content" v-else>{{ msg.text }}</div>
          </div>
          <div v-if="isWaiting" class="message ai waiting-message">
            <div class="avatar"><el-icon><Cpu /></el-icon></div>
            <div class="content waiting-content">
              <span class="pulse-dot"></span>
              <span class="waiting-text">{{ waitingText }}</span>
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
import { ref, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Close, Cpu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isOpen = ref(false)
const input = ref('')
const isLoading = ref(false)
const agentStatus = ref('')
const chatBody = ref<HTMLElement | null>(null)

// 会话ID，用于多轮对话记忆
const sessionId = ref('')

// 等待加载状态：发消息后、Agent 首次返回 token 之前
const isWaiting = ref(false)
let waitingTimer: ReturnType<typeof setInterval> | null = null

const waitingText = '请稍后，模型正在回复'

const startWaiting = () => {
  isWaiting.value = true
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

interface Message {
  role: 'user' | 'ai';
  text: string;
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
  agentStatus.value = ''
  startWaiting()
  scrollToBottom()

  // 检查是否有预设回答
  const presetReply = getPresetReply(userMsg)
  if (presetReply) {
    messages.value.push({ role: 'ai', text: presetReply })
    isLoading.value = false
    stopWaiting()
    scrollToBottom()
    return
  }

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
            // 普通 token 文本，累积到 messages 中的 AI 消息
            messages.value[aiMsgIndex].text += data
          }
        }
      }
      scrollToBottom()
    }

    // 流结束
    agentStatus.value = ''

  } catch (e: any) {
    console.error('Agent chat error:', e)
    messages.value[aiMsgIndex].text = '抱歉，Agent 服务暂时不可用，请稍后再试。'
    agentStatus.value = ''
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
 * 预设常见问题回答
 */
const getPresetReply = (msg: string): string | null => {
  const m = msg.trim()
  // 身份询问
  if (/^(你(是谁|叫什么|是做什么的)|你的身份|介绍(一下)?你自己?)$/i.test(m)) {
    return '你好！我是**云转桥**科技成果转化平台的智能助手，可以帮你查找科技成果、专家人才、扶持政策和资金信息，也能为你解答平台使用问题。'
  }
  // 功能询问
  if (/^(你(能做什么|有什么功能)|(有|是)什么功能|你能帮我(做什么|什么忙)|你的作用是什么)$/i.test(m) ||
      /^功能(介绍|说明)?$/i.test(m)) {
    return '我可以帮你做这些事情：\n\n1. **查找成果** — 浏览和搜索科技成果\n2. **查找专家** — 对接各领域专家人才\n3. **查找政策** — 查询产业扶持政策\n4. **查找资金** — 了解科技金融支持\n5. **智能匹配** — 描述需求，精准匹配资源\n\n你也可以直接在页面顶部的导航栏使用这些功能。'
  }
  // 操作指南
  if (/^(怎么(用|操作|使用)|如何使用|操作(指南|说明|步骤)?|使用(方法|指南|说明)?)$/i.test(m)) {
    return '使用云转桥平台很简单：\n\n1. **浏览资源** — 点击导航栏的「成果大厅」「政策库」「专家库」等查看各类资源\n2. **智能匹配** — 点击右下角的 AI 按钮或前往「智能匹配」页面，描述你的技术需求\n3. **发布需求** — 在「需求大厅」页面可以发布你的技术需求\n4. **个人中心** — 管理你的发布、收藏和消息\n\n有什么具体需要我帮忙的吗？'
  }
  // 打招呼
  if (/^(你好|您好|嗨|hi|hello|hey|早|早上好|下午好|晚上好)$/i.test(m)) {
    return '你好！我是云转桥智能助手，很高兴为你服务。有什么需要我帮助的吗？你可以问我关于科技成果、专家、政策、资金等方面的问题。'
  }
  // 发布成果
  if (/^(怎么|如何)(发布|上传|提交)(科研)?成果$/i.test(m) || 
      /^(科研)?成果(怎么|如何)(发布|上传|提交)$/i.test(m) ||
      /^(我要|我想)(发布|上传|提交)(科研)?成果$/i.test(m) ||
      /^发布(科研)?成果$/i.test(m)) {
    return '发布科技成果很简单，步骤如下：\n\n1. 点击导航栏的「成果大厅」\n2. 点击页面右上角的「发布成果」按钮\n3. 填写成果标题、描述、领域、标签等信息\n4. 可以使用AI标签建议功能自动生成标签\n5. 点击「提交审核」即可\n\n审核通过后，你的成果将在成果大厅展示。\n\n[跳转:去发布成果](/publish-achievement)'
  }
  // 发布需求
  if (/^(怎么|如何)(发布|提交)(技术)?需求$/i.test(m) ||
      /^(技术)?需求(怎么|如何)(发布|提交)$/i.test(m) ||
      /^我要(发布|提交)(技术)?需求$/i.test(m)) {
    return '发布技术需求的步骤：\n\n1. 点击导航栏的「需求大厅」\n2. 点击「发布需求」按钮\n3. 详细描述你的技术需求，包括领域、应用场景、预算等\n4. 提交后系统会自动进行智能匹配\n\n你也可以直接前往「智能匹配」页面，描述需求后系统会自动帮你匹配相关成果。\n\n[跳转:去发布需求](/publish-need)  [跳转:去智能匹配](/smart-match)'
  }
  // 智能匹配使用
  if (/^(怎么|如何)(使用|进行)(智能)?匹配$/i.test(m) ||
      /^(智能)?匹配(怎么|如何)(使用|进行)$/i.test(m) ||
      /^我要(智能)?匹配$/i.test(m)) {
    return '使用智能匹配的方法：\n\n1. 点击导航栏的「智能匹配」\n2. 在输入框中详细描述你的技术需求（如"需要一种基于深度学习的医学影像分析技术"）\n3. 系统会自动提取需求画像，进行多维度检索\n4. 查看匹配结果和知识图谱可视化\n\n描述越详细，匹配结果越精准哦！\n\n[跳转:去智能匹配](/smart-match)'
  }
  // 查找成果
  if (/^(怎么|如何)(查找|搜索|寻找)(科技)?成果$/i.test(m) ||
      /^(科技)?成果(怎么|如何)(查找|搜索|寻找)$/i.test(m)) {
    return '查找科技成果的方式：\n\n1. **成果大厅** — 点击导航栏「成果大厅」，浏览或按领域筛选\n2. **智能匹配** — 描述你的需求，系统自动匹配\n3. **搜索框** — 在页面顶部搜索框输入关键词直接搜索\n\n你可以搜索技术名称、领域、应用场景等关键词。\n\n[跳转:去成果大厅](/achievements)'
  }
  // 查找专家
  if (/^(怎么|如何)(查找|搜索|寻找)(专家|人才)$/i.test(m) ||
      /^(专家|人才)(怎么|如何)(查找|搜索|寻找)$/i.test(m)) {
    return '查找专家人才的方式：\n\n1. 点击导航栏的「资源库」→「专家库」\n2. 按领域筛选专家\n3. 查看专家的研究方向和联系方式\n4. 可以直接联系专家进行技术合作\n\n找到合适的专家后，可以通过平台进行在线沟通。\n\n[跳转:去专家库](/library/experts)'
  }
  // 查找政策
  if (/^(怎么|如何)(查找|搜索|查询)(政策|补贴)$/i.test(m) ||
      /^(政策|补贴)(怎么|如何)(查找|搜索|查询)$/i.test(m)) {
    return '查找产业政策和补贴的方式：\n\n1. 点击导航栏的「资源库」→「政策库」\n2. 按领域、地区筛选政策\n3. 查看政策详情和申请条件\n4. 系统会自动匹配与你需求相关的政策\n\n在智能匹配页面提交需求后，系统也会推荐相关政策支持。\n\n[跳转:去政策库](/library/policies)'
  }
  // 查找资金
  if (/^(怎么|如何)(查找|搜索)(资金|融资|贷款)$/i.test(m) ||
      /^(资金|融资|贷款)(怎么|如何)(查找|搜索)$/i.test(m)) {
    return '查找科技金融和资金支持的方式：\n\n1. 点击导航栏的「资源库」→「资金库」\n2. 浏览各类科技金融产品、引导基金和贷款渠道\n3. 查看申请条件和流程\n\n在智能匹配时，系统也会根据你的需求推荐合适的资金渠道。\n\n[跳转:去资金库](/library/funds)'
  }
  // 个人中心
  if (/^(我的|个人)(中心|主页|空间)$/i.test(m) ||
      /^(怎么|如何)(进入|打开)(我的|个人)(中心|主页)$/i.test(m)) {
    return '进入个人中心的方式：\n\n1. 点击页面右上角的用户头像\n2. 选择「个人中心」\n\n在个人中心你可以：\n- 查看和管理已发布的成果和需求\n- 管理收藏的资源\n- 查看消息和申请进度\n- 修改个人资料\n\n[跳转:去个人中心](/profile)'
  }
  // 收藏功能
  if (/^(怎么|如何)(收藏|关注)(成果|资源)$/i.test(m) ||
      /^(收藏|关注)(怎么|如何)做$/i.test(m)) {
    return '收藏功能使用方法：\n\n1. 在成果详情页、政策详情页等资源页面\n2. 点击页面上的「收藏」按钮\n3. 收藏的资源会保存在个人中心的「我的收藏」中\n\n这样方便你随时查看感兴趣的资源。\n\n[跳转:去成果大厅](/achievements)'
  }
  // 消息管理
  if (/^(怎么|如何)(查看|管理)(消息|通知)$/i.test(m) ||
      /^(消息|通知)(怎么|如何)(查看|管理)$/i.test(m)) {
    return '查看消息的方式：\n\n1. 点击页面右上角的消息图标\n2. 或进入个人中心的「消息」页签\n\n消息包含：\n- 匹配结果通知\n- 审核进度通知\n- 其他用户的留言和申请\n- 系统公告\n\n[跳转:去消息中心](/messages)'
  }
  // 审核进度
  if (/^(怎么|如何)(查看|查询)(审核|审批)(进度|状态)$/i.test(m) ||
      /^(审核|审批)(进度|状态)(怎么|如何)(查看|查询)$/i.test(m)) {
    return '查看审核进度的方式：\n\n1. 进入个人中心\n2. 选择「我的发布」页签\n3. 查看成果或需求的审核状态\n\n审核状态包括：待审核、审核通过、审核驳回\n如果审核驳回，可以查看驳回原因并修改后重新提交。\n\n[跳转:去个人中心](/profile)'
  }
  // 成果详情
  if (/^(怎么|如何)(查看|了解)(成果|技术)(详情|信息)$/i.test(m)) {
    return '查看成果详情的方式：\n\n1. 在成果大厅或搜索结果中\n2. 点击任意成果卡片\n3. 查看成果的详细信息，包括：\n   - 技术描述和参数\n   - 成熟度评估\n   - 应用场景\n   - 合作方式\n   - 联系方式\n\n在详情页还可以收藏或联系成果发布方。\n\n[跳转:去成果大厅](/achievements)'
  }
  return null
}

/**
 * 简易 Markdown 渲染（支持粗体、标题、列表、分段、闪烁光标、导航链接）
 */
const renderMarkdown = (text: string, showCursor = false): string => {
  if (!text && !showCursor) return ''
  let html = text
    // 转义 HTML
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // 导航链接 [跳转:名称](路径) - 渲染为可点击按钮
    .replace(/\[跳转:\s*([^\]]+)\]\(([^)]+)\)/g, '<span class="nav-link" onclick="navigateTo(\'$2\')">$1</span>')
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

  if (showCursor) {
    html += '<span class="cursor-blink">|</span>'
  }

  return html
}

/**
 * 全局导航函数，供导航链接调用
 */
(window as any).navigateTo = (path: string) => {
  router.push(path)
  isOpen.value = false
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

/* 流式输出光标 - 跟随消息末尾显示 */
.content :deep(.cursor-blink) {
  animation: blink 1s step-end infinite;
  color: var(--gold-primary);
  font-weight: bold;
  margin-left: 2px;
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

/* 导航链接样式 */
.content :deep(.nav-link) {
  display: inline-block;
  background: linear-gradient(135deg, var(--gold-primary), var(--gold-secondary));
  color: #000;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  margin: 4px 4px;
  transition: all 0.2s;
  border: none;
}

.content :deep(.nav-link:hover) {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
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
