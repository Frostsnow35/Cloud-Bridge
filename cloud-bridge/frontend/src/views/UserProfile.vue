<template>
  <div class="user-profile">
    <div class="container">
      <!-- User Info Card -->
      <el-card class="profile-header-card">
        <div class="profile-header-content">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="userStore.user?.avatar">
              <el-icon :size="50"><UserFilled /></el-icon>
            </el-avatar>
            <div class="user-meta">
              <h2 class="username">{{ userStore.user?.username || '未登录用户' }}</h2>
              <el-tag type="success" effect="dark" round>{{ userStore.user?.role || '普通用户' }}</el-tag>
            </div>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <span class="label">用户ID</span>
              <span class="value">{{ userStore.user?.id || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ userStore.user?.email || '未绑定' }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机号</span>
              <span class="value">{{ userStore.user?.phone || '未绑定' }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间</span>
              <span class="value">{{ userStore.user?.createdAt ? new Date(userStore.user.createdAt).toLocaleDateString() : '-' }}</span>
            </div>
          </div>

          <div class="header-actions">
            <el-button type="primary" plain @click="handleEditProfile">编辑资料</el-button>
            <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-card>

      <!-- Main Content Tabs -->
      <div class="main-tabs-wrapper">
        <el-tabs v-model="activeTab" class="profile-tabs" type="border-card" @tab-click="handleTabClick">
          
          <!-- My Achievements (Expert) -->
          <el-tab-pane label="我的成果" name="achievements" v-if="userStore.user?.role === 'EXPERT' || userStore.user?.role === 'RESEARCHER'">
            <div class="tab-content">
              <div class="tab-header">
                <h3>已发布成果 ({{ myAchievements.length }})</h3>
                <el-button type="primary" @click="$router.push('/achievements/publish')">发布新成果</el-button>
              </div>
              
              <div v-if="loading.achievements" class="loading-state">
                <el-skeleton :rows="3" animated />
              </div>
              
              <div v-else-if="myAchievements.length === 0" class="simple-empty">
                暂无发布成果
              </div>

              <div v-else class="resource-grid">
                <el-card v-for="item in myAchievements" :key="item.id" class="resource-card" shadow="hover">
                  <div class="card-header">
                    <h4 class="card-title" :title="item.title">{{ item.title }}</h4>
                    <el-tag :type="getStatusType(item.status)" size="small" effect="plain">{{ getStatusLabel(item.status) }}</el-tag>
                  </div>
                  
                  <div class="card-body">
                    <div class="info-row">
                      <span class="label">领域</span>
                      <span class="value">{{ item.field }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">价格</span>
                      <span class="price-value">{{ item.price ? '¥ ' + item.price.toLocaleString() : '面议' }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">发布时间</span>
                      <span class="value">{{ new Date(item.createdAt || Date.now()).toLocaleDateString() }}</span>
                    </div>
                  </div>

                  <div class="card-footer">
                    <el-button link type="primary" @click="$router.push(`/achievements/${item.id}`)">查看</el-button>
                    <el-divider direction="vertical" />
                    <el-button link type="primary" @click="handleEdit(item, 'achievement')">编辑</el-button>
                    <el-divider direction="vertical" />
                    <el-button link type="danger" @click="handleDelete(item.id, 'achievement')">删除</el-button>
                  </div>
                </el-card>
              </div>
            </div>
          </el-tab-pane>

          <!-- My Demands (Enterprise) -->
          <el-tab-pane label="我的需求" name="demands" v-if="userStore.user?.role === 'ENTERPRISE'">
             <div class="tab-content">
              <div class="tab-header">
                <h3>已发布需求 ({{ myDemands.length }})</h3>
                <el-button type="primary" @click="$router.push('/needs/publish')">发布新需求</el-button>
              </div>
              
              <div v-if="loading.demands" class="loading-state">
                <el-skeleton :rows="3" animated />
              </div>

              <div v-else-if="myDemands.length === 0" class="simple-empty">
                暂无发布需求
              </div>

              <div v-else class="resource-grid">
                <el-card v-for="item in myDemands" :key="item.id" class="resource-card" shadow="hover">
                  <div class="card-header">
                    <h4 class="card-title" :title="item.title">{{ item.title }}</h4>
                    <el-tag :type="getStatusType(item.status)" size="small" effect="plain">{{ getStatusLabel(item.status) }}</el-tag>
                  </div>
                  
                  <div class="card-body">
                    <div class="info-row">
                      <span class="label">领域</span>
                      <span class="value">{{ item.field }}</span>
                    </div>
                    <div class="info-row">
                      <span class="label">预算</span>
                      <span class="price-value">{{ item.budget ? '¥ ' + item.budget.toLocaleString() : '面议' }}</span>
                    </div>
                     <div class="info-row">
                      <span class="label">截止日期</span>
                      <span class="value">{{ item.deadline }}</span>
                    </div>
                  </div>

                  <div class="card-footer">
                    <el-button link type="primary" @click="$router.push(`/needs/${item.id}`)">查看</el-button>
                    <el-divider direction="vertical" />
                    <el-button link type="primary" @click="handleEdit(item, 'demand')">编辑</el-button>
                    <el-divider direction="vertical" />
                    <el-button link type="danger" @click="handleDelete(item.id, 'demand')">删除</el-button>
                  </div>
                </el-card>
              </div>
            </div>
          </el-tab-pane>

          <!-- Message Center -->
          <el-tab-pane label="消息中心" name="messages">
            <div class="tab-content">
               <el-tabs v-model="messageTab" type="card">
                  <el-tab-pane label="收件箱" name="received">
                     <div v-loading="loading.messages">
                        <el-empty v-if="receivedMessages.length === 0" description="暂无收到消息" />
                        <div v-else class="message-list">
                           <div v-for="msg in receivedMessages" :key="msg.id" class="message-item" :class="{ unread: !msg.read }">
                              <div class="msg-header">
                                 <span class="msg-sender">来自用户 {{ msg.senderId }}</span>
                                 <span class="msg-time">{{ new Date(msg.createdAt).toLocaleString() }}</span>
                              </div>
                              <div class="msg-content">
                                 <p><strong>合作类型：</strong>{{ msg.cooperationType || '未指定' }}</p>
                                 <p>{{ msg.content }}</p>
                              </div>
                              <div class="msg-actions">
                                 <el-button size="small" @click="handleReply(msg)">回复</el-button>
                                 <el-tag v-if="!msg.read" type="danger" size="small" effect="dark">未读</el-tag>
                              </div>
                           </div>
                        </div>
                     </div>
                  </el-tab-pane>
                  <el-tab-pane label="发件箱" name="sent">
                     <div v-loading="loading.messages">
                        <el-empty v-if="sentMessages.length === 0" description="暂无发送消息" />
                        <div v-else class="message-list">
                           <div v-for="msg in sentMessages" :key="msg.id" class="message-item">
                              <div class="msg-header">
                                 <span class="msg-receiver">发给用户 {{ msg.receiverId }}</span>
                                 <span class="msg-time">{{ new Date(msg.createdAt).toLocaleString() }}</span>
                              </div>
                              <div class="msg-content">
                                 <p><strong>合作类型：</strong>{{ msg.cooperationType || '未指定' }}</p>
                                 <p>{{ msg.content }}</p>
                              </div>
                           </div>
                        </div>
                     </div>
                  </el-tab-pane>
               </el-tabs>
            </div>
          </el-tab-pane>

          <!-- Favorites -->
          <el-tab-pane label="我的收藏" name="favorites">
             <div class="tab-content">
                <el-empty description="暂无收藏内容 (功能开发中)" />
             </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editType === 'achievement' ? '编辑成果' : '编辑需求'"
      width="50%"
      destroy-on-close
    >
      <el-form :model="editForm" label-width="100px" ref="editFormRef" :rules="editRules">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item :label="editType === 'achievement' ? '价格' : '预算'" :prop="editType === 'achievement' ? 'price' : 'budget'">
          <el-input-number v-if="editType === 'achievement'" v-model="editForm.price" :min="0" :step="1000" />
          <el-input-number v-else v-model="editForm.budget" :min="0" :step="1000" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="editForm.contactName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const activeTab = ref('achievements')
const messageTab = ref('received')

const loading = reactive({
  achievements: false,
  demands: false,
  messages: false
})

const myAchievements = ref<any[]>([])
const myDemands = ref<any[]>([])
const receivedMessages = ref<any[]>([])
const sentMessages = ref<any[]>([])

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'PUBLISHED': 'success',
    'DRAFT': 'info',
    'PENDING': 'warning',
    'CLOSED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
   const map: Record<string, string> = {
    'PUBLISHED': '已发布',
    'DRAFT': '草稿',
    'PENDING': '审核中',
    'CLOSED': '已关闭'
  }
  return map[status] || status
}

const fetchData = async () => {
  if (!userStore.user?.id) return

  // Achievements
  if (activeTab.value === 'achievements' && myAchievements.value.length === 0) {
     loading.achievements = true
     try {
       const res = await axios.get('/api/achievements/my', { params: { userId: userStore.user.id } })
       myAchievements.value = res.data
     } catch (e) {
       console.error(e)
     } finally {
       loading.achievements = false
     }
  }

  // Demands
  if (activeTab.value === 'demands' && myDemands.value.length === 0) {
     loading.demands = true
     try {
       const res = await axios.get('/api/demands/my', { params: { userId: userStore.user.id } })
       myDemands.value = res.data
     } catch (e) {
       console.error(e)
     } finally {
       loading.demands = false
     }
  }

  // Messages
  if (activeTab.value === 'messages') {
     loading.messages = true
     try {
        if (messageTab.value === 'received') {
           const res = await axios.get(`/api/messages/received/${userStore.user.id}`)
           receivedMessages.value = res.data.content || res.data
        } else {
           const res = await axios.get(`/api/messages/sent/${userStore.user.id}`)
           sentMessages.value = res.data
        }
     } catch (e) {
        console.error(e)
     } finally {
        loading.messages = false
     }
  }
}

const handleTabClick = () => {
  fetchData()
}

watch(messageTab, () => {
   if (activeTab.value === 'messages') {
      fetchData()
   }
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}

const handleEditProfile = () => {
  ElMessage.info('编辑资料功能开发中')
}

// Edit Dialog Logic
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editType = ref('')
const editFormRef = ref()
const editForm = reactive({
  id: 0,
  title: '',
  description: '',
  price: 0,
  budget: 0,
  contactName: '',
  phone: '',
  // Preserve other fields
  field: '',
  maturity: '',
  patentInfo: '',
  applicationCases: '',
  resourceLinks: '',
  institution: '',
  deadline: '',
  ownerId: 0,
  status: ''
})

const editRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  budget: [{ required: true, message: '请输入预算', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const handleEdit = (item: any, type: string) => {
  editType.value = type
  Object.assign(editForm, item)
  // Ensure numeric fields are numbers
  if (type === 'achievement') {
     editForm.price = Number(item.price) || 0
  } else {
     editForm.budget = Number(item.budget) || 0
  }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      editLoading.value = true
      try {
        const url = editType.value === 'achievement' 
          ? `/api/achievements/${editForm.id}`
          : `/api/demands/${editForm.id}`
        
        await axios.put(url, editForm)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        fetchData() // Refresh list
      } catch (e) {
        console.error(e)
        ElMessage.error('更新失败')
      } finally {
        editLoading.value = false
      }
    }
  })
}

const handleDelete = (id: number, type: string) => {
  ElMessageBox.confirm('确定要删除该项吗? 此操作不可恢复', '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
     try {
        const url = type === 'achievement' 
          ? `/api/achievements/${id}`
          : `/api/demands/${id}`
        
        await axios.delete(url)
        ElMessage.success('删除成功')
        
        // Remove from local list to avoid full refresh flicker
        if (type === 'achievement') {
           myAchievements.value = myAchievements.value.filter(i => i.id !== id)
        } else {
           myDemands.value = myDemands.value.filter(i => i.id !== id)
        }
     } catch (e) {
        console.error(e)
        ElMessage.error('删除失败')
     }
  }).catch(() => {})
}

const handleReply = (msg: any) => {
   // Navigate to achievement detail to contact again, or show reply dialog
   // For now, simpler to just direct to detail page if related entity exists
   if (msg.relatedEntityId && msg.relatedEntityType === 'ACHIEVEMENT') {
      router.push(`/achievements/${msg.relatedEntityId}`)
   } else {
      ElMessage.info('无法关联到具体项目，请直接联系发送者')
   }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  
  // Set default tab based on role
  if (userStore.user?.role === 'ENTERPRISE') {
     activeTab.value = 'demands'
  } else if (userStore.user?.role === 'EXPERT' || userStore.user?.role === 'RESEARCHER') {
     activeTab.value = 'achievements'
  }
  
  fetchData()
})
</script>

<style scoped>
.user-profile {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding: 40px 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  margin-bottom: 24px;
}

.profile-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 24px;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.username {
  margin: 0;
  font-size: 24px;
  color: var(--text-primary);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  flex: 1;
  margin: 0 40px;
  padding: 0 40px;
  border-left: 1px solid var(--border-color);
  border-right: 1px solid var(--border-color);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.label {
  font-size: 12px;
  color: var(--text-secondary);
}

.value {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.main-tabs-wrapper {
  background: var(--bg-card);
  border-radius: 4px;
}

.tab-content {
  padding: 20px;
  min-height: 300px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tab-header h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
}

/* Message List Styles */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  background: var(--bg-secondary);
  transition: all 0.3s;
}

.message-item:hover {
  border-color: var(--primary-color);
  transform: translateY(-2px);
}

.message-item.unread {
  border-left: 4px solid var(--danger-color);
  background: rgba(245, 108, 108, 0.05);
}

.msg-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.msg-content p {
  margin: 4px 0;
  color: var(--text-primary);
}

.msg-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  align-items: center;
}

/* Grid Layout Styles */
.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.resource-card {
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.resource-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: var(--primary-color);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 12px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.price-value {
  color: var(--primary-color);
  font-weight: 600;
  font-size: 15px;
}

.card-footer {
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.simple-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: var(--text-secondary);
  font-size: 14px;
  border: 1px dashed var(--border-color);
  border-radius: 4px;
}

.loading-state {
  padding: 20px;
}
</style>