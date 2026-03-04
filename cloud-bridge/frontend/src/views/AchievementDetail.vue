<template>
  <div class="achievement-detail-page">
    <div class="container" v-if="loading">
      <el-skeleton :rows="10" animated />
    </div>
    <div class="container" v-else-if="achievement">
      <el-row :gutter="24">
        <!-- Left: Main Content -->
        <el-col :span="17">
          <div class="main-content">
            <!-- Banner -->
            <div class="banner card-style" :style="bannerStyle">
              <div class="banner-overlay"></div>
              <div class="banner-content">
                <div class="tag-group">
                  <el-tag type="warning" effect="dark" class="maturity-tag">{{ achievement.maturity }}</el-tag>
                  <el-tag v-for="tag in tags" :key="tag" type="info" effect="light" class="tech-tag">{{ tag }}</el-tag>
                  <el-tag :type="statusType" effect="plain" v-if="tags.length === 0">{{ statusText }}</el-tag>
                </div>
                <h1>{{ achievement.title }}</h1>
                <div class="meta-info">
                  <span><el-icon><Clock /></el-icon> {{ new Date(achievement.createdAt).toLocaleDateString() }}</span>
                  <span><el-icon><View /></el-icon> 1,234 次浏览</span>
                </div>
              </div>
            </div>
            
            <div class="banner-footer card-style-footer">
              <div class="price-section">
                <span class="currency">¥</span>
                <span class="amount">{{ achievement.price ? achievement.price.toLocaleString() : '面议' }}</span>
              </div>
              <div class="action-buttons">
                <!-- Actions for Owner -->
                <template v-if="isOwner">
                  <el-button type="primary" size="large" @click="handleEdit">编辑成果</el-button>
                  <el-button type="danger" size="large" plain @click="handleDelete">删除成果</el-button>
                </template>

                <!-- Actions for Admin -->
                <template v-else-if="isAdmin">
                   <template v-if="achievement.status === 'PENDING_REVIEW'">
                      <el-button type="success" size="large" @click="handleApprove">通过审核</el-button>
                      <el-button type="danger" size="large" @click="handleReject">驳回申请</el-button>
                   </template>
                   <el-tag v-else size="large" :type="statusType">{{ statusText }}</el-tag>
                </template>

                <!-- Actions for Visitor -->
                <template v-else>
                  <el-button type="primary" size="large" class="gradient-btn contact-btn" @click="handleContact">
                    <el-icon><Message /></el-icon> 联系负责人
                  </el-button>
                  <el-button size="large" class="fav-btn" plain @click="handleFavorite">
                    <el-icon><Star /></el-icon> 收藏
                  </el-button>
                </template>
              </div>
            </div>

            <!-- Description -->
            <div class="detail-section card-style">
              <h3 class="section-title">成果简介</h3>
              <div class="rich-text">
                <p>{{ achievement.description }}</p>
              </div>
            </div>

            <!-- Application Cases -->
            <div class="detail-section card-style">
              <h3 class="section-title">应用案例</h3>
              <div class="rich-text">
                <p v-if="achievement.applicationCases" style="white-space: pre-wrap;">{{ achievement.applicationCases }}</p>
                <el-empty v-else description="暂无案例展示" :image-size="60" />
              </div>
            </div>

            <!-- Intellectual Property -->
            <div class="detail-section card-style">
              <h3 class="section-title">知识产权信息</h3>
              <div class="ip-info" v-if="achievement.patentInfo">
                 <div class="ip-row">
                    <el-icon><Document /></el-icon>
                    <span class="label">专利信息：</span>
                    <span class="value">{{ achievement.patentInfo }}</span>
                 </div>
              </div>
              <el-empty v-else description="暂无知识产权信息" :image-size="60" />
            </div>

            <!-- Resource Download -->
            <div class="detail-section card-style" v-if="resources.length > 0">
              <h3 class="section-title">资源下载</h3>
              <div class="resource-list">
                <div class="resource-item" v-for="(res, index) in resources" :key="index">
                   <div class="res-icon"><el-icon><Files /></el-icon></div>
                   <div class="res-info">
                      <div class="res-name">{{ res.name }}</div>
                      <div class="res-size">{{ res.size }}</div>
                   </div>
                   <el-button type="primary" link @click="handleDownload(res)">
                      <el-icon><Download /></el-icon> 下载
                   </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- Right: Sidebar -->
        <el-col :span="7">
          <div class="sidebar">
            <!-- Owner Card -->
            <div class="sidebar-card owner-card">
              <div class="card-header">
                <span class="header-title">发布团队</span>
              </div>
              <div class="owner-profile" @click="handleTeamClick(achievement.ownerId)">
                <UserAvatar :username="`User${achievement.ownerId}`" :size="70" class="owner-avatar" />
                <div class="owner-info">
                  <h4>发布者 {{ achievement.ownerId }}</h4>
                  <p>{{ achievement.institution || '暂无机构信息' }}</p>
                  <el-tag size="small" type="info" effect="plain" class="auth-tag">已认证</el-tag>
                </div>
              </div>
              <div class="stats-grid">
                <div class="stat-item">
                  <span class="num">12</span>
                  <span class="label">发布成果</span>
                </div>
                <div class="stat-item">
                  <span class="num text-gold">4.9</span>
                  <span class="label">信用评分</span>
                </div>
                <div class="stat-item">
                  <span class="num">5</span>
                  <span class="label">成交记录</span>
                </div>
              </div>
            </div>

            <!-- Contact Card -->
            <div class="sidebar-card contact-card">
              <div class="card-header">
                <span class="header-title">联系方式</span>
              </div>
              <div class="contact-content">
                <div v-if="userStore.isLoggedIn" class="contact-details">
                  <div class="detail-row">
                    <span class="label">联系人：</span>
                    <span class="value">{{ achievement.contactName || '未填写' }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">电话：</span>
                    <span class="value">{{ achievement.phone || '未填写' }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">邮箱：</span>
                    <span class="value link-text">点击查看</span>
                  </div>
                </div>
                <div v-else class="login-mask">
                  <el-icon class="lock-icon" :size="32"><Lock /></el-icon>
                  <p>登录后查看完整联系方式</p>
                  <el-button type="primary" link @click="$router.push('/login')">立即登录</el-button>
                </div>
              </div>
            </div>

            <!-- Recommendations -->
            <div class="sidebar-card recommend-card">
              <div class="card-header">
                <span class="header-title">相关推荐</span>
              </div>
              <div class="recommend-list">
                <el-empty v-if="recommendations.length === 0" description="暂无推荐" :image-size="60" />
                <div class="recommend-item" v-for="item in recommendations" :key="item.id" @click="$router.push(`/achievements/${item.id}`)">
                  <div class="rec-info">
                    <div class="rec-title">{{ item.title }}</div>
                    <div class="rec-tags">
                       <el-tag size="small" type="info">{{ item.field }}</el-tag>
                    </div>
                  </div>
                  <div class="rec-price">¥ {{ item.price ? item.price.toLocaleString() : '面议' }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Contact Dialog removed -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Picture, Share, Files, Reading, Tickets, Star, Link, CircleCheck, ArrowRight, Message, Lock, Clock, View, Document, Trophy, Download } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import UserAvatar from '../components/UserAvatar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)

const achievement = ref<any>(null)
const recommendations = ref<any[]>([])

const bannerStyle = computed(() => {
  if (achievement.value?.image) {
    return { 
      backgroundImage: `url(${achievement.value.image})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  // Fallback gradient
  const id = achievement.value?.id || 0
  const hue = (id * 137) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue}, 60%, 20%) 0%, hsl(${(hue + 40) % 360}, 60%, 15%) 100%)`
  }
})

const isOwner = computed(() => {
  return userStore.isLoggedIn && userStore.user && achievement.value && userStore.user.id === achievement.value.ownerId
})

const isAdmin = computed(() => {
  return userStore.isLoggedIn && userStore.userRole === 'ADMIN'
})

const statusType = computed(() => {
  if (!achievement.value) return 'info'
  switch (achievement.value.status) {
    case 'PUBLISHED': return 'success'
    case 'PENDING_REVIEW': return 'warning'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
})

const statusText = computed(() => {
  if (!achievement.value) return ''
  switch (achievement.value.status) {
    case 'PUBLISHED': return '已发布'
    case 'PENDING_REVIEW': return '待审核'
    case 'REJECTED': return '已驳回'
    default: return achievement.value.status
  }
})

const handleContact = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后联系专家')
    router.push('/login')
    return
  }
  router.push(`/achievements/${achievement.value.id}/contact`)
}

const handleDownload = (resource: any) => {
    if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录后下载资源')
        router.push('/login')
        return
    }
    // Simulate download
    ElMessage.success(`正在下载：${resource.name}`)
    // window.open(resource.url, '_blank')
}

const resources = computed(() => {
    if (!achievement.value || !achievement.value.resourceLinks) {
        return []
    }
    try {
        // Try parsing as JSON first
        if (achievement.value.resourceLinks.trim().startsWith('[')) {
            return JSON.parse(achievement.value.resourceLinks)
        }
        // Fallback for simple comma separated links (if any)
        return achievement.value.resourceLinks.split(',').map((link: string, index: number) => ({
            name: `资源文件 ${index + 1}`,
            url: link.trim(),
            size: '未知大小'
        }))
    } catch (e) {
        console.error("Failed to parse resource links", e)
        return []
    }
})

const tags = computed(() => {
    if (!achievement.value || !achievement.value.tags) return []
    return achievement.value.tags.split(',').filter((t: string) => t && t.trim().length > 0)
})

const fetchDetail = async (id: string) => {
  loading.value = true
  try {
    const res = await axios.get(`/api/achievements/${id}`)
    if (res.data) {
        achievement.value = res.data
        fetchRecommendations(achievement.value.field)
    }
  } catch (error) {
    console.error('Error fetching details:', error)
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const fetchRecommendations = async (field: string) => {
    try {
        const res = await axios.get('/api/achievements', {
            params: { field, size: 4 }
        })
        if (res.data && res.data.content) {
            recommendations.value = res.data.content.filter((item: any) => item.id !== achievement.value.id)
        }
    } catch (error) {
        console.error('Error fetching recommendations:', error)
    }
}

const handleTeamClick = (ownerId: number) => {
    // Navigate to user profile or team page
    ElMessage.info(`查看团队 ${ownerId} 详情`)
}

const handleServiceClick = (service: any) => {
    ElMessage.success(`已选择服务：${service.name}`)
}

const handleFavorite = () => {
    ElMessage.success('已添加到收藏')
}

const handleEdit = () => {
    ElMessage.info('请前往个人中心管理您的成果')
    router.push('/profile')
}

const handleDelete = () => {
    ElMessageBox.confirm('确定要删除该成果吗？此操作不可恢复', '警告', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        try {
            await axios.delete(`/api/achievements/${achievement.value.id}`)
            ElMessage.success('删除成功')
            router.push('/achievements')
        } catch (error) {
            console.error(error)
            ElMessage.error('删除失败')
        }
    }).catch(() => {})
}

const handleApprove = () => {
    ElMessageBox.confirm('确定要通过审核吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
    }).then(async () => {
        try {
            await axios.put(`/api/achievements/${achievement.value.id}/audit`, null, {
                params: { status: 'PUBLISHED' }
            })
            ElMessage.success('审核通过')
            fetchDetail(achievement.value.id)
        } catch (error) {
            console.error(error)
            ElMessage.error('操作失败')
        }
    }).catch(() => {})
}

const handleReject = () => {
    ElMessageBox.prompt('请输入驳回理由', '驳回申请', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
    }).then(async (result: any) => {
        const { value } = result
        try {
            await axios.put(`/api/achievements/${achievement.value.id}/audit`, null, {
                params: { status: 'REJECTED' }
            })
            ElMessage.warning('已驳回申请')
            fetchDetail(achievement.value.id)
        } catch (error) {
            console.error(error)
            ElMessage.error('操作失败')
        }
    }).catch(() => {})
}

watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchDetail(newId as string)
  }
})

onMounted(() => {
  const id = route.params.id as string
  if (id) {
    fetchDetail(id)
  }
})
</script>

<style scoped>
.achievement-detail-page {
  padding: 40px 20px;
  background-color: var(--bg-primary);
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-style {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

/* Banner Styles */
.banner {
  padding: 0;
  overflow: hidden;
  position: relative;
  height: 360px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
  margin-bottom: 0;
}

.banner-footer {
  border-top-left-radius: 0;
  border-top-right-radius: 0;
  margin-bottom: 24px;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.1), rgba(0,0,0,0.8));
  z-index: 1;
}

.banner-content {
  position: relative;
  z-index: 2;
  padding: 30px;
  color: #fff;
}

.tag-group {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}

.banner-content h1 {
  font-size: 32px;
  margin: 0 0 12px 0;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.meta-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  opacity: 0.9;
}

.meta-info span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.banner-footer {
  padding: 20px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-card);
}

.price-section {
  color: #f56c6c;
  font-weight: 700;
}

.currency {
  font-size: 24px;
  margin-right: 4px;
}

.amount {
  font-size: 36px;
  line-height: 1;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.contact-btn {
  background: linear-gradient(135deg, var(--el-color-primary) 0%, #409eff 100%);
  border: none;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.fav-btn {
  width: 100px;
}

/* Section Styles */
.section-title {
  font-size: 20px;
  color: var(--text-primary);
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 4px solid var(--el-color-primary);
  font-weight: 600;
}

.rich-text {
  color: var(--text-regular);
  line-height: 1.8;
  font-size: 16px;
}

.highlight-text {
  color: var(--el-color-primary);
  font-weight: 500;
}

/* Resource List */
.resource-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.res-icon {
  margin-right: 12px;
  color: var(--el-color-primary);
  font-size: 24px;
}

.res-info {
  flex-grow: 1;
}

.res-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.res-size {
  font-size: 12px;
  color: var(--text-secondary);
}

/* IP Info */
.ip-info {
  background-color: var(--bg-secondary);
  padding: 16px;
  border-radius: 8px;
}

.ip-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-regular);
}

.ip-row .label {
  font-weight: 600;
  color: var(--text-primary);
}

/* Sidebar */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-card {
  background: var(--bg-card);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.card-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  font-weight: 600;
  color: var(--text-primary);
}

.owner-profile {
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
}

.owner-avatar {
  margin-bottom: 12px;
  border: 2px solid var(--border-color);
}

.owner-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
}

.owner-info p {
  color: var(--text-secondary);
  font-size: 13px;
  margin: 0 0 8px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  padding: 16px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-item .num {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-item .num.text-gold {
  color: #e6a23c;
}

.stat-item .label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* Recommend List */
.recommend-list {
  padding: 10px;
}

.recommend-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background 0.2s;
}

.recommend-item:last-child {
  border-bottom: none;
}

.recommend-item:hover {
  background: var(--bg-secondary);
}

.rec-title {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rec-price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
}

/* Contact Dialog */
.contact-header {
  margin-bottom: 20px;
  padding: 15px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.highlight-title {
  color: var(--el-color-primary);
  font-weight: 600;
  margin-top: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Contact Card Styles */
.contact-content {
  padding: 20px;
}

.contact-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.detail-row .label {
  color: var(--text-secondary);
}

.detail-row .value {
  color: var(--text-primary);
  font-weight: 500;
}

.link-text {
  color: var(--el-color-primary);
  cursor: pointer;
}

.login-mask {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-secondary);
  padding: 10px 0;
}

.lock-icon {
  margin-bottom: 10px;
  color: var(--text-secondary);
  opacity: 0.6;
}

.login-mask p {
  margin: 5px 0 10px;
  font-size: 13px;
}
</style>
