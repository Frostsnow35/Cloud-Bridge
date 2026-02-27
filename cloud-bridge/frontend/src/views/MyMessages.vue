<template>
  <div class="messages-page">
    <div class="container">
      <h2 class="page-title">消息中心</h2>
      
      <el-tabs v-model="activeTab" class="message-tabs">
        <el-tab-pane label="收到的消息" name="received">
          <div v-loading="loading" class="message-list">
            <el-empty v-if="receivedMessages.length === 0" description="暂无收到消息" />
            <el-card v-else v-for="msg in receivedMessages" :key="msg.id" class="message-card" :class="{ 'unread': !msg.read }">
              <template #header>
                <div class="card-header">
                  <span class="sender-info">
                    <el-icon><User /></el-icon> 来自用户 #{{ msg.senderId }}
                  </span>
                  <div class="header-right">
                    <el-tag v-if="!msg.read" type="danger" size="small" effect="dark">未读</el-tag>
                    <span class="time">{{ formatDate(msg.createdAt) }}</span>
                  </div>
                </div>
              </template>
              <div class="message-content">
                <div class="info-row" v-if="msg.relatedEntityId">
                  <span class="label">关联项目：</span>
                  <el-link type="primary" @click="goToDetail(msg.relatedEntityType, msg.relatedEntityId)">
                    查看详情 ({{ msg.relatedEntityType === 'ACHIEVEMENT' ? '成果' : '需求' }} #{{ msg.relatedEntityId }})
                  </el-link>
                </div>
                <div class="info-row" v-if="msg.cooperationType">
                  <span class="label">合作意向：</span>
                  <span>{{ msg.cooperationType }}</span>
                </div>
                <div class="info-row" v-if="msg.budget">
                  <span class="label">预算/报价：</span>
                  <span>{{ msg.budget }}</span>
                </div>
                <div class="info-row" v-if="msg.attachmentUrl">
                  <span class="label">附件：</span>
                  <a :href="getAttachmentUrl(msg.attachmentUrl)" target="_blank" class="attachment-link">
                    <el-icon><Paperclip /></el-icon> 点击下载/查看附件
                  </a>
                </div>
                <div class="content-body">
                  <p>{{ msg.content }}</p>
                </div>
                <div class="actions">
                  <el-button v-if="!msg.read" type="primary" size="small" @click="markAsRead(msg.id)">标记已读</el-button>
                  <el-button size="small" @click="reply(msg.senderId, msg.relatedEntityId, msg.relatedEntityType)">回复</el-button>
                </div>
              </div>
            </el-card>
            
            <el-pagination
              v-if="receivedTotal > 0"
              layout="prev, pager, next"
              :total="receivedTotal"
              :page-size="pageSize"
              @current-change="handleReceivedPageChange"
              class="pagination"
            />
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="发出的消息" name="sent">
          <div v-loading="loading" class="message-list">
            <el-empty v-if="sentMessages.length === 0" description="暂无发出消息" />
            <el-card v-else v-for="msg in sentMessages" :key="msg.id" class="message-card">
              <template #header>
                <div class="card-header">
                  <span class="receiver-info">
                    <el-icon><Position /></el-icon> 发送给用户 #{{ msg.receiverId }}
                  </span>
                  <span class="time">{{ formatDate(msg.createdAt) }}</span>
                </div>
              </template>
              <div class="message-content">
                <div class="info-row" v-if="msg.relatedEntityId">
                  <span class="label">关联项目：</span>
                  <el-link type="primary" @click="goToDetail(msg.relatedEntityType, msg.relatedEntityId)">
                    {{ msg.relatedEntityType === 'ACHIEVEMENT' ? '成果' : '需求' }} #{{ msg.relatedEntityId }}
                  </el-link>
                </div>
                <div class="content-body">
                  <p>{{ msg.content }}</p>
                </div>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
      <!-- Reply Dialog -->
      <el-dialog v-model="replyDialogVisible" title="回复消息" width="500px">
        <el-form :model="replyForm" ref="replyFormRef" :rules="replyRules">
          <el-form-item label="回复内容" prop="content">
            <el-input 
              v-model="replyForm.content" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入回复内容..."
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="replyDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitReply" :loading="replyLoading">发送</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { User, Position, Paperclip } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

interface Message {
  id: number;
  senderId: number;
  receiverId: number;
  content: string;
  read: boolean;
  createdAt: string;
  relatedEntityId?: number;
  relatedEntityType?: string;
  cooperationType?: string;
  budget?: string;
  attachmentUrl?: string;
}

const activeTab = ref('received')
const loading = ref(false)

const getAttachmentUrl = (url: string) => {
  if (url.startsWith('http')) return url
  return url
}
const receivedMessages = ref<Message[]>([])
const sentMessages = ref<Message[]>([])
const receivedTotal = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const fetchReceivedMessages = async (page = 1) => {
  if (!userStore.user?.id) return
  loading.value = true
  try {
    const res = await axios.get(`/api/messages/received/${userStore.user.id}`, {
      params: { page: page - 1, size: pageSize.value }
    })
    receivedMessages.value = res.data.content
    receivedTotal.value = res.data.totalElements
  } catch (error) {
    console.error('Failed to fetch received messages:', error)
    ElMessage.error('获取消息失败')
  } finally {
    loading.value = false
  }
}

const fetchSentMessages = async () => {
  if (!userStore.user?.id) return
  loading.value = true
  try {
    const res = await axios.get(`/api/messages/sent/${userStore.user.id}`)
    sentMessages.value = res.data
  } catch (error) {
    console.error('Failed to fetch sent messages:', error)
  } finally {
    loading.value = false
  }
}

const markAsRead = async (id: number) => {
  try {
    await axios.put(`/api/messages/${id}/read`)
    const msg = receivedMessages.value.find((m: any) => m.id === id)
    if (msg) msg.read = true
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToDetail = (type: string | undefined, id: number | undefined) => {
  if (!type || !id) return
  if (type === 'ACHIEVEMENT') {
    router.push(`/achievements/${id}`)
  } else if (type === 'DEMAND') {
    router.push(`/needs/${id}`)
  }
}

const replyDialogVisible = ref(false)
const replyLoading = ref(false)
const replyFormRef = ref()
const replyForm = reactive({
  content: '',
  receiverId: 0,
  relatedEntityId: 0,
  relatedEntityType: ''
})

const replyRules = {
  content: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

const reply = (senderId: number, relatedEntityId?: number, relatedEntityType?: string) => {
  replyForm.content = ''
  replyForm.receiverId = senderId
  replyForm.relatedEntityId = relatedEntityId || 0
  replyForm.relatedEntityType = relatedEntityType || ''
  replyDialogVisible.value = true
}

const submitReply = async () => {
  if (!replyFormRef.value) return
  
  await replyFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      replyLoading.value = true
      try {
        await axios.post('/api/messages', {
          senderId: userStore.user?.id,
          receiverId: replyForm.receiverId,
          content: replyForm.content,
          relatedEntityId: replyForm.relatedEntityId,
          relatedEntityType: replyForm.relatedEntityType,
          read: false
        })
        ElMessage.success('回复发送成功')
        replyDialogVisible.value = false
        // Refresh sent messages
        fetchSentMessages()
      } catch (error) {
        console.error('Failed to send reply:', error)
        ElMessage.error('发送失败，请稍后重试')
      } finally {
        replyLoading.value = false
      }
    }
  })
}

const handleReceivedPageChange = (page: number) => {
  currentPage.value = page
  fetchReceivedMessages(page)
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  fetchReceivedMessages()
  fetchSentMessages()
})
</script>

<style scoped>
.messages-page {
  min-height: 100vh;
  padding: 40px 20px;
  background-color: var(--bg-secondary);
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
  color: var(--text-primary);
}

.message-card {
  margin-bottom: 16px;
  border-left: 4px solid transparent;
  transition: all 0.3s;
}

.message-card.unread {
  border-left-color: var(--el-color-danger);
  background-color: #fff9f9;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sender-info, .receiver-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--text-primary);
}

.time {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 12px;
}

.info-row {
  margin-bottom: 8px;
  font-size: 14px;
}

.label {
  color: var(--text-secondary);
  margin-right: 8px;
}

.content-body {
  margin: 16px 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  white-space: pre-wrap;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
