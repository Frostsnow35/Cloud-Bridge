<template>
  <div class="demand-detail" v-loading="loading">
    <!-- Breadcrumb -->
    <div class="section-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/needs' }">需求广场</el-breadcrumb-item>
        <el-breadcrumb-item>{{ demand.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- Header Section -->
    <div class="detail-header">
      <div class="section-container header-inner">
        <div class="header-main">
          <h1 class="title">{{ demand.title }}</h1>
          <div class="tags">
            <el-tag size="large" type="danger" effect="dark">需求</el-tag>
            <el-tag size="large" type="primary" effect="plain">{{ demand.field }}</el-tag>
            <el-tag size="large" :type="statusType" effect="plain">{{ statusText }}</el-tag>
          </div>
        </div>
        <div class="header-action">
          <div class="price">预算: ¥ {{ demand.budget ? demand.budget.toLocaleString() : '面议' }}</div>
          
          <!-- Actions for Owner -->
          <template v-if="isOwner">
            <template v-if="demand.status === 'MATCHING'">
                <el-button type="success" size="large" class="w-100 mb-2" @click="handleComplete">项目验收</el-button>
            </template>
            <el-button type="primary" size="large" class="w-100 mb-2" @click="handleEdit">编辑需求</el-button>
            <el-button type="danger" size="large" plain class="w-100" @click="handleDelete">删除需求</el-button>
          </template>

          <!-- Actions for Admin -->
          <template v-else-if="isAdmin">
            <template v-if="demand.status === 'PENDING_REVIEW'">
              <el-button type="success" size="large" class="w-100 mb-2" @click="handleApprove">通过审核</el-button>
              <el-button type="danger" size="large" class="w-100" @click="handleReject">驳回申请</el-button>
            </template>
            <el-tag v-else size="large" class="w-100 text-center" :type="statusType">{{ statusText }}</el-tag>
          </template>

          <!-- Actions for Visitor -->
          <template v-else>
            <el-button v-if="demand.type === 'REWARD'" type="warning" size="large" class="w-100 mb-2" @click="handleBid">我要揭榜</el-button>
            <el-button type="primary" size="large" class="contact-btn" @click="handleContact">立即响应</el-button>
            <el-button size="large" plain @click="handleFavorite" class="w-100 mb-2">收藏</el-button>
            <el-button type="info" size="large" class="w-100" @click="handleAnalysis">生成解决方案</el-button>
          </template>
        </div>
      </div>
    </div>

    <!-- Content Layout -->
    <div class="section-container content-wrapper">
      <el-row :gutter="30">
        <!-- Left: Main Content -->
        <el-col :span="17">
          <div class="content-left">
            <div class="detail-section">
              <h3 class="section-title">需求描述</h3>
              <div class="rich-text">
                <p>{{ demand.description }}</p>
              </div>
            </div>

            <div class="detail-section">
              <h3 class="section-title">详细要求</h3>
              <div class="rich-text">
                <div class="info-grid">
                    <div class="info-item">
                        <span class="label">截止日期：</span>
                        <span class="value">{{ demand.deadline }}</span>
                    </div>
                    <div class="info-item">
                        <span class="label">预算范围：</span>
                        <span class="value">¥ {{ demand.budget ? demand.budget.toLocaleString() : '面议' }}</span>
                    </div>
                     <div class="info-item" v-if="demand.contactName">
                        <span class="label">联系人：</span>
                        <span class="value">{{ demand.contactName }}</span>
                    </div>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h3 class="section-title">合作模式</h3>
              <div class="rich-text">
                <el-tag size="large" effect="plain">{{ demand.type === 'REWARD' ? '揭榜挂帅' : '普通需求' }}</el-tag>
                <p class="mt-2 text-secondary">本需求倾向于{{ demand.type === 'REWARD' ? '揭榜挂帅' : '普通合作' }}模式，欢迎有实力的团队接洽。</p>
              </div>
            </div>

            <!-- Bids List for Owner and Expert -->
            <div v-if="(isOwner || isExpert) && demand.type === 'REWARD'" class="detail-section">
              <h3 class="section-title">
                揭榜记录
                <el-badge :value="bids.length" type="primary" class="ml-2" />
              </h3>
              <el-table :data="bids" style="width: 100%" v-loading="loadingBids">
                <el-table-column prop="bidderId" label="揭榜者ID" width="100" />
                <el-table-column prop="proposal" label="方案摘要" show-overflow-tooltip />
                <el-table-column prop="quote" label="报价" width="120">
                  <template #default="scope">¥ {{ scope.row.quote?.toLocaleString() || '未填写' }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getBidStatusType(scope.row.status)">{{ getBidStatusText(scope.row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="220">
                  <template #default="scope">
                    <template v-if="isOwner && scope.row.status === 'PENDING'">
                      <el-button size="small" type="success" @click="handleUpdateBidStatus(scope.row, 'ACCEPTED')">采纳</el-button>
                      <el-button size="small" type="danger" @click="handleUpdateBidStatus(scope.row, 'REJECTED')">拒绝</el-button>
                    </template>
                    <el-button v-if="isExpert" size="small" type="primary" @click="openReviewDialog(scope.row)">评审</el-button>
                    <el-button v-if="isOwner" size="small" type="warning" plain @click="openReviewListDialog(scope.row)">查看评审</el-button>
                    <el-button v-if="scope.row.txHash" size="small" type="info" link @click="viewOnChain(scope.row.txHash)">链上核验</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-col>

        <!-- Right: Owner Info & Sidebar -->
        <el-col :span="7">
          <div class="sidebar">
            <el-card class="owner-card" shadow="hover">
              <div class="owner-profile">
                <el-avatar :size="64" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                <div class="owner-info">
                  <h4>{{ demand.institution || '发布企业' }}</h4>
                  <p>企业认证 / 信用极好</p>
                </div>
              </div>
              <el-divider />
              <div class="stats">
                <div class="stat">
                  <span class="num">8</span>
                  <span class="label">发布需求</span>
                </div>
                <div class="stat">
                  <span class="num text-gold">5.0</span>
                  <span class="label">信用评分</span>
                </div>
                <div class="stat">
                  <span class="num">3</span>
                  <span class="label">成功对接</span>
                </div>
              </div>
            </el-card>

            <el-card class="recommend-card" shadow="hover">
              <template #header>
                <span class="card-title">相关成果推荐</span>
              </template>
              <div class="recommend-list">
                <el-empty v-if="recommendations.length === 0" description="暂无推荐" :image-size="60" />
                <div class="recommend-item" v-for="item in recommendations" :key="item.id" @click="$router.push(`/achievements/${item.id}`)">
                  <div class="rec-title">{{ item.title }}</div>
                  <div class="rec-meta">¥ {{ item.price ? item.price.toLocaleString() : '面议' }}</div>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Bid Dialog -->
    <el-dialog v-model="bidDialog.visible" title="我要揭榜" width="500px">
      <el-form :model="bidDialog.form" label-width="80px">
        <el-form-item label="投标方案" required>
          <el-input v-model="bidDialog.form.proposal" type="textarea" rows="4" placeholder="请简述您的技术方案和优势..." />
        </el-form-item>
        <el-form-item label="我的报价">
          <el-input-number v-model="bidDialog.form.quote" :min="0" :step="1000" style="width: 100%" />
        </el-form-item>
        <div class="blockchain-notice">
          <el-icon><InfoFilled /></el-icon>
          提交后，您的揭榜记录将自动同步至区块链，作为具备法律效力的存证凭证。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="bidDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="bidDialog.submitting" @click="submitBid">提交揭榜</el-button>
      </template>
    </el-dialog>

    <!-- Review Submission Dialog (Expert) -->
    <el-dialog v-model="reviewDialog.visible" title="专家评审" width="500px">
      <el-form :model="reviewDialog.form" label-width="80px">
        <el-form-item label="评分" required>
          <el-rate v-model="reviewDialog.form.score" :max="100" show-score text-color="#ff9900" />
          <span class="ml-2 text-secondary">(满分100分，当前: {{ reviewDialog.form.score }})</span>
        </el-form-item>
        <el-form-item label="评审意见" required>
          <el-input v-model="reviewDialog.form.comment" type="textarea" rows="4" placeholder="请输入专业的评审意见..." />
        </el-form-item>
        <div class="blockchain-notice">
          <el-icon><InfoFilled /></el-icon>
          评审结果将上链存证，不可篡改。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="reviewDialog.submitting" @click="submitReview">提交评审</el-button>
      </template>
    </el-dialog>

    <!-- Review List Dialog (Owner) -->
    <el-dialog v-model="reviewListDialog.visible" title="评审记录" width="600px">
      <el-table :data="reviewListDialog.list" style="width: 100%" v-loading="reviewListDialog.loading">
        <el-table-column prop="reviewerId" label="专家ID" width="100" />
        <el-table-column prop="score" label="评分" width="80">
          <template #default="scope">
            <span :class="{'text-danger': scope.row.score < 60, 'text-success': scope.row.score >= 80}">{{ scope.row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评审意见" show-overflow-tooltip />
        <el-table-column label="存证" width="100">
          <template #default="scope">
             <el-button v-if="scope.row.txHash" size="small" type="info" link @click="viewOnChain(scope.row.txHash)">核验</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Picture, InfoFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loadingBids = ref(false)

const demand = ref<any>({})
const recommendations = ref<any[]>([])
const bids = ref<any[]>([])
const contract = ref<any>(null)

const bidDialog = reactive({
  visible: false,
  submitting: false,
  form: {
    proposal: '',
    quote: 0
  }
})

const reviewDialog = reactive({
  visible: false,
  submitting: false,
  currentBidId: null,
  form: {
    score: 80,
    comment: ''
  }
})

const reviewListDialog = reactive({
  visible: false,
  loading: false,
  list: []
})

const contractDialog = reactive({
  visible: false,
  signing: false,
  content: '',
  status: '',
  txHash: '',
  ownerSigned: false,
  bidderSigned: false
})

const isOwner = computed(() => {
  return userStore.isLoggedIn && userStore.user && demand.value && userStore.user.id === demand.value.ownerId
})

const isAdmin = computed(() => {
  return userStore.isLoggedIn && userStore.userRole === 'ADMIN'
})

const isExpert = computed(() => {
  return userStore.isLoggedIn && userStore.userRole === 'EXPERT'
})

const statusType = computed(() => {
  switch (demand.value.status) {
    case 'PUBLISHED': return 'success'
    case 'PENDING_REVIEW': return 'warning'
    case 'REJECTED': return 'danger'
    case 'MATCHING': return 'primary'
    case 'COMPLETED': return 'info'
    default: return 'info'
  }
})

const statusText = computed(() => {
  switch (demand.value.status) {
    case 'PUBLISHED': return '已发布'
    case 'PENDING_REVIEW': return '待审核'
    case 'REJECTED': return '已驳回'
    case 'MATCHING': return '对接中'
    case 'COMPLETED': return '已完成'
    default: return demand.value.status
  }
})

const fetchDetail = async (id: string) => {
  loading.value = true
  try {
    const res = await axios.get(`/api/demands/${id}`)
    if (res.data) {
        demand.value = res.data
        // Fetch analysis only if we have valid data
        fetchAnalysis(demand.value)
        
        // If owner or expert, fetch bids
        if ((isOwner.value || isExpert.value) && demand.value.type === 'REWARD') {
          fetchBids(id)
        }
    } else {
        throw new Error('No data returned')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取需求详情失败')
  } finally {
    loading.value = false
  }
}

const fetchBids = async (id: string) => {
  loadingBids.value = true
  try {
    const res = await axios.get(`/api/demands/${id}/bids`)
    bids.value = res.data || []
  } catch (e) {
    console.error('Failed to fetch bids', e)
  } finally {
    loadingBids.value = false
  }
}

const handleBid = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后揭榜')
    router.push('/login')
    return
  }
  bidDialog.visible = true
}

const submitBid = async () => {
  if (!bidDialog.form.proposal) {
    ElMessage.warning('请输入投标方案')
    return
  }
  
  bidDialog.submitting = true
  try {
    await axios.post(`/api/demands/${demand.value.id}/bid`, bidDialog.form)
    ElMessage.success('揭榜成功！记录已存证至区块链')
    bidDialog.visible = false
    // Clear form
    bidDialog.form.proposal = ''
    bidDialog.form.quote = 0
  } catch (e) {
    console.error('Bid submission failed', e)
    ElMessage.error('揭榜失败，请稍后重试')
  } finally {
    bidDialog.submitting = false
  }
}

const handleUpdateBidStatus = async (bid: any, status: string) => {
  try {
    await axios.put(`/api/demands/bids/${bid.id}/status`, null, {
      params: { status }
    })
    ElMessage.success(status === 'ACCEPTED' ? '已采纳揭榜' : '已拒绝揭榜')
    fetchBids(demand.value.id)
    fetchDetail(demand.value.id)
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const getBidStatusType = (status: string) => {
  switch (status) {
    case 'PENDING': return 'info'
    case 'ACCEPTED': return 'success'
    case 'REJECTED': return 'danger'
    case 'NEGOTIATING': return 'warning'
    case 'COMPLETED': return 'primary'
    default: return 'info'
  }
}

const getBidStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待处理'
    case 'ACCEPTED': return '已采纳'
    case 'REJECTED': return '已拒绝'
    case 'NEGOTIATING': return '洽谈中'
    case 'COMPLETED': return '已完成'
    default: return status
  }
}

const viewOnChain = (txHash: string) => {
  window.open(`https://fisco-bcos-browser.example.com/tx/${txHash}`, '_blank')
}

const openReviewDialog = (bid: any) => {
  reviewDialog.currentBidId = bid.id
  reviewDialog.form.score = 80
  reviewDialog.form.comment = ''
  reviewDialog.visible = true
}

const submitReview = async () => {
  if (!reviewDialog.form.comment) {
    ElMessage.warning('请输入评审意见')
    return
  }
  
  reviewDialog.submitting = true
  try {
    await axios.post(`/api/reviews/bid/${reviewDialog.currentBidId}`, reviewDialog.form)
    ElMessage.success('评审提交成功！已存证上链')
    reviewDialog.visible = false
  } catch (e) {
    console.error('Review submission failed', e)
    ElMessage.error('评审提交失败')
  } finally {
    reviewDialog.submitting = false
  }
}

const openReviewListDialog = async (bid: any) => {
  reviewListDialog.visible = true
  reviewListDialog.loading = true
  reviewListDialog.list = []
  
  try {
    const res = await axios.get(`/api/reviews/bid/${bid.id}`)
    reviewListDialog.list = res.data || []
  } catch (e) {
    console.error('Fetch reviews failed', e)
    ElMessage.error('获取评审记录失败')
  } finally {
    reviewListDialog.loading = false
  }
}

const fetchAnalysis = async (item: any) => {
  if (!item || !item.title) return
  
  try {
    const desc = item.description ? item.description.substring(0, 50) : ''
    // Use matching API to find achievements that match this demand
    const res = await axios.post('/api/matching/match', {
      description: item.title + ' ' + desc,
      field: item.field,
      budget: item.budget,
      type: 'achievement' // We are looking for achievements
    })
    
    if (res.data) {
      recommendations.value = res.data.matches?.slice(0, 5) || []
    }
  } catch (e) {
    console.error('Analysis failed', e)
  }
}

onMounted(() => {
  if (route.params.id) {
    fetchDetail(route.params.id as string)
  }
})

const handleAnalysis = () => {
    ElMessage.success('正在为您生成AI解决方案...')
    if (demand.value && demand.value.id) {
        // Navigate to the new Dashboard
        router.push(`/dashboard/${demand.value.id}`)
    }
}

const handleContact = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (demand.value && demand.value.id) {
    router.push(`/needs/${demand.value.id}/contact`)
  }
}

const handleFavorite = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  ElMessage.success('已收藏该需求')
}

const handleEdit = () => {
  // Navigate to profile or show message
  ElMessage.info('请前往个人中心管理您的需求')
  router.push('/profile')
}

const handleDelete = () => {
  ElMessageBox.confirm('确定要删除该需求吗？此操作不可恢复', '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`/api/demands/${demand.value.id}`)
      ElMessage.success('删除成功')
      router.push('/needs')
    } catch (error) {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleComplete = () => {
  ElMessageBox.confirm('确认项目已交付并验收合格？此操作将完结该需求并存证上链。', '项目验收', {
    confirmButtonText: '确认验收',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await axios.put(`/api/demands/${demand.value.id}/complete`)
      ElMessage.success('项目验收成功！已存证上链')
      fetchDetail(demand.value.id)
    } catch (error) {
      console.error(error)
      ElMessage.error('操作失败')
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
      await axios.put(`/api/demands/${demand.value.id}/audit`, null, {
        params: { status: 'PUBLISHED' }
      })
      ElMessage.success('审核通过')
      fetchDetail(demand.value.id)
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
      // In a real app, we would send the reason. Here just update status.
      await axios.put(`/api/demands/${demand.value.id}/audit`, null, {
        params: { status: 'REJECTED' }
      })
      ElMessage.warning('已驳回申请')
      fetchDetail(demand.value.id)
    } catch (error) {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

watch(() => route.params.id, (newId) => {
  if (newId) fetchDetail(newId as string)
})
</script>

<style scoped>
.demand-detail {
  min-height: 100vh;
  background-color: var(--bg-dark);
  padding-bottom: 60px;
}
.breadcrumb { padding: 20px 0; }
:deep(.el-breadcrumb__inner) { color: var(--text-secondary) !important; }
:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) { color: var(--text-primary) !important; }

.detail-header {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  padding: 40px 0;
  margin-bottom: 30px;
}
.header-inner { display: flex; justify-content: space-between; align-items: flex-start; }
.title { font-size: 32px; margin-bottom: 20px; color: var(--text-primary); font-weight: 700; }
.tags .el-tag { margin-right: 12px; }
.header-action { text-align: right; min-width: 200px; }
.price { font-size: 36px; color: var(--el-color-danger); font-weight: bold; margin-bottom: 20px; font-family: 'DIN Alternate', sans-serif; }
.contact-btn { width: 100%; margin-bottom: 10px; margin-left: 0; }
.w-100 { width: 100%; }
.mt-2 { margin-top: 10px; }

.content-wrapper { margin-top: 30px; }
.content-left { background-color: var(--bg-card); border-radius: 8px; padding: 40px; border: 1px solid var(--border-color); }
.detail-section { margin-bottom: 50px; }
.section-title { font-size: 20px; color: var(--text-primary); margin-bottom: 20px; padding-left: 12px; border-left: 4px solid var(--el-color-primary); line-height: 1; display: flex; align-items: center; gap: 8px; }
.rich-text { font-size: 16px; line-height: 1.8; color: var(--text-secondary); }
.rich-text ul { padding-left: 20px; }
.rich-text li { margin-bottom: 8px; }

.sidebar { position: sticky; top: 20px; }
.owner-card, .recommend-card { margin-bottom: 20px; background-color: var(--bg-card); border: 1px solid var(--border-color); }
.owner-profile { display: flex; align-items: center; gap: 16px; padding: 10px 0; }
.owner-info h4 { margin: 0 0 4px; font-size: 16px; color: var(--text-primary); }
.owner-info p { margin: 0; color: var(--text-secondary); font-size: 13px; }
.stats { display: flex; justify-content: space-around; text-align: center; }
.stat .num { display: block; font-size: 20px; font-weight: bold; color: var(--text-primary); }
.stat .label { font-size: 12px; color: var(--text-secondary); }
.card-title { font-weight: bold; }
.recommend-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-color); cursor: pointer; transition: background 0.2s; }
.recommend-item:hover { background: rgba(255,255,255,0.05); }
.recommend-item:last-child { border-bottom: none; }
.rec-title { font-size: 14px; color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 150px; }
.rec-meta { color: var(--el-color-warning); font-weight: bold; }

.info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-top: 10px;
}
.info-item {
    display: flex;
    flex-direction: column;
}
.info-item .label {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 4px;
}
.info-item .value {
    font-size: 16px;
    color: var(--text-primary);
    font-weight: 500;
}

.blockchain-notice {
  margin-top: 15px;
  padding: 10px;
  background: rgba(103, 194, 58, 0.1);
  border-radius: 4px;
  color: var(--el-color-success);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.ml-2 { margin-left: 8px; }
</style>
