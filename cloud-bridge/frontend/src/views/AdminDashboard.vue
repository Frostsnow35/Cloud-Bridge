<template>
  <div class="admin-dashboard">
    <div class="container">
      <div class="dashboard-header">
        <h2>管理后台</h2>
        <el-tag type="danger" effect="dark">管理员</el-tag>
      </div>

      <div class="stats-cards">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>总用户数</span>
                </div>
              </template>
              <div class="card-value">{{ stats.totalUsers }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>总需求数</span>
                </div>
              </template>
              <div class="card-value">{{ stats.totalDemands }}</div>
              <div class="card-footer">待审核: {{ stats.pendingDemands }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>总成果数</span>
                </div>
              </template>
              <div class="card-value">{{ stats.totalAchievements }}</div>
              <div class="card-footer">待审核: {{ stats.pendingAchievements }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>待办事项</span>
                </div>
              </template>
              <div class="card-value action-needed">{{ stats.pendingDemands + stats.pendingAchievements }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <el-tabs v-model="activeTab" class="dashboard-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="待审核需求" name="demands">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" :icon="Refresh" circle @click="fetchDemands" :loading="loading.demands" />
            </div>
            
            <el-table :data="pendingDemands" v-loading="loading.demands" style="width: 100%" empty-text="暂无待审核需求">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="需求标题" min-width="200" show-overflow-tooltip />
              <el-table-column prop="ownerId" label="发布者ID" width="100" />
              <el-table-column prop="field" label="领域" width="120" />
              <el-table-column prop="createdAt" label="提交时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="success" @click="auditDemand(scope.row.id, 'PUBLISHED')">通过</el-button>
                  <el-button size="small" type="danger" @click="auditDemand(scope.row.id, 'REJECTED')">驳回</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="待审核成果" name="achievements">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" :icon="Refresh" circle @click="fetchAchievements" :loading="loading.achievements" />
            </div>

            <el-table :data="pendingAchievements" v-loading="loading.achievements" style="width: 100%" empty-text="暂无待审核成果">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="成果名称" min-width="200" show-overflow-tooltip />
              <el-table-column prop="ownerId" label="发布者ID" width="100" />
              <el-table-column prop="field" label="领域" width="120" />
              <el-table-column prop="createdAt" label="提交时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="success" @click="auditAchievement(scope.row.id, 'PUBLISHED')">通过</el-button>
                  <el-button size="small" type="danger" @click="auditAchievement(scope.row.id, 'REJECTED')">驳回</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="存证管理" name="evidence">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" :icon="Refresh" circle @click="fetchEvidence" :loading="loading.evidence" />
            </div>

            <el-table :data="evidenceList" v-loading="loading.evidence" style="width: 100%" empty-text="暂无存证记录">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="hash" label="数据哈希" min-width="200" show-overflow-tooltip />
              <el-table-column prop="txHash" label="交易哈希" min-width="200" show-overflow-tooltip />
              <el-table-column prop="evidenceType" label="类型" width="120">
                <template #default="scope">
                  <el-tag>{{ scope.row.evidenceType || 'DEFAULT' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="ownerId" label="用户ID" width="100" />
              <el-table-column prop="createdAt" label="存证时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" link @click="viewOnChain(scope.row.txHash)">
                    查看链上详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('demands')
const pendingDemands = ref([])
const pendingAchievements = ref([])
const evidenceList = ref([])

const stats = reactive({
  totalDemands: 0,
  totalAchievements: 0,
  totalUsers: 0,
  pendingDemands: 0,
  pendingAchievements: 0
})

const loading = reactive({
  demands: false,
  achievements: false,
  evidence: false
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const fetchStats = async () => {
  try {
    const res = await axios.get('/api/admin/stats')
    Object.assign(stats, res.data)
  } catch (error) {
    console.error('Failed to fetch stats:', error)
    // Silently fail or show warning
  }
}

const fetchDemands = async () => {
  loading.demands = true
  try {
    const res = await axios.get('/api/demands/pending')
    pendingDemands.value = res.data
  } catch (error) {
    console.error('Failed to fetch pending demands:', error)
    ElMessage.error('获取待审核需求失败')
  } finally {
    loading.demands = false
  }
}

const fetchAchievements = async () => {
  loading.achievements = true
  try {
    const res = await axios.get('/api/achievements/pending')
    pendingAchievements.value = res.data
  } catch (error) {
    console.error('Failed to fetch pending achievements:', error)
    ElMessage.error('获取待审核成果失败')
  } finally {
    loading.achievements = false
  }
}

const fetchEvidence = async () => {
  loading.evidence = true
  try {
    const res = await axios.get('/api/evidence/list')
    evidenceList.value = res.data
  } catch (error) {
    console.error('Failed to fetch evidence list:', error)
    ElMessage.error('获取存证记录失败')
  } finally {
    loading.evidence = false
  }
}

const viewOnChain = (txHash: string) => {
  // Open external blockchain browser
  // Replace with actual BCOS browser URL if available, e.g. https://fisco-bcos-browser/tx/
  window.open(`https://fisco-bcos-browser.example.com/tx/${txHash}`, '_blank')
}

const handleTabClick = () => {
  if (activeTab.value === 'demands') {
    fetchDemands()
  } else if (activeTab.value === 'achievements') {
    fetchAchievements()
  } else if (activeTab.value === 'evidence') {
    fetchEvidence()
  }
}

onMounted(() => {
  fetchStats()
  fetchDemands()
})

const auditDemand = async (id: number, status: string) => {
  const actionText = status === 'PUBLISHED' ? '通过' : '驳回'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该需求吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: status === 'PUBLISHED' ? 'success' : 'warning'
    })
    
    await axios.put(`/api/demands/${id}/audit`, null, {
      params: { status }
    })
    
    ElMessage.success(`需求已${actionText}`)
    fetchDemands()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

const auditAchievement = async (id: number, status: string) => {
  const actionText = status === 'PUBLISHED' ? '通过' : '驳回'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该成果吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: status === 'PUBLISHED' ? 'success' : 'warning'
    })
    
    await axios.put(`/api/achievements/${id}/audit`, null, {
      params: { status }
    })
    
    ElMessage.success(`成果已${actionText}`)
    fetchAchievements()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn || userStore.userRole !== 'ADMIN') {
    ElMessage.error('无权访问')
    router.push('/')
    return
  }
  
  fetchDemands()
})
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding: 40px 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  padding: 24px;
  border-radius: 8px;
}

.dashboard-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.dashboard-header h2 {
  margin: 0;
  color: var(--text-primary);
}

.stats-cards {
  margin-bottom: 30px;
}

.card-header {
  font-weight: bold;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}

.card-value.action-needed {
  color: #F56C6C;
}

.card-footer {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.dashboard-tabs {
  background: var(--bg-card);
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  background-color: transparent;
  color: var(--text-primary);
  --el-table-header-bg-color: var(--bg-secondary);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
}
</style>
