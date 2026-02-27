<template>
  <div class="needs-page">
    <div class="section-container">
      <!-- Header Area -->
      <div class="page-header">
        <div class="header-content">
          <h2 class="page-title">需求大厅</h2>
          <p class="page-subtitle">汇聚全球创新需求，精准对接优质资源</p>
        </div>
        <div class="actions">
          <el-button type="primary" size="large" @click="navigateToPublish" class="publish-btn">
            <el-icon class="el-icon--left"><Plus /></el-icon>
            发布需求
          </el-button>
        </div>
      </div>

      <!-- Search & Filter Bar -->
      <div class="filter-section">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索需求关键词..."
            size="large"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="tags-filter">
          <span class="filter-label">领域筛选：</span>
          <div 
            :class="['filter-tag', { 'is-active': activeField === 'All' }]"
            @click="activeField = 'All'"
          >
            全部
          </div>
          <div
            v-for="field in fields"
            :key="field"
            :class="['filter-tag', { 'is-active': activeField === field }]"
            @click="activeField = field"
          >
            {{ field }}
          </div>
        </div>
      </div>

      <!-- Content Grid -->
      <div class="content-grid" v-loading="loading">
        <el-empty v-if="!loading && filteredNeeds.length === 0" description="暂无符合条件的需求" />
        
        <div class="results-grid" v-else>
          <div v-for="need in filteredNeeds" :key="need.id" class="grid-item">
            <div class="need-card" @click="viewDetails(need.id)">
              <div class="card-header">
                <span class="field-tag">{{ need.field }}</span>
                <span class="budget">¥ {{ formatBudget(need.budget) }}</span>
              </div>
              
              <h3 class="card-title" :title="need.title">{{ need.title }}</h3>
              <p class="card-desc">{{ truncateText(need.description, 60) }}</p>
              
              <div class="card-footer">
                <div class="status-indicator">
                  <span class="dot" :class="getStatusClass(need.status)"></span>
                  {{ need.status || '进行中' }}
                </div>
                <span class="detail-link">
                  查看详情 <el-icon><ArrowRight /></el-icon>
                </span>
              </div>
              <div class="card-glow"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, ArrowRight } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const searchQuery = ref('')
const activeField = ref('All')

// Mock Data for fallback
const mockNeeds = [
  { id: 1, title: '高性能石墨烯电池研发', field: '新材料', budget: 500000, status: '进行中', description: '寻求具备高导电性、高耐久性的石墨烯复合材料配方，用于下一代电动汽车电池研发。' },
  { id: 2, title: '智慧城市交通流量预测算法', field: '人工智能', budget: 300000, status: '急需', description: '基于多源数据融合的实时交通流量预测模型，要求准确率达到95%以上。' },
  { id: 3, title: '抗肿瘤靶向药物临床前研究', field: '生物医药', budget: 1200000, status: '进行中', description: '针对特定靶点的创新药筛选与药效评估，需要具备GLP认证实验室支持。' },
  { id: 4, title: '工业物联网边缘计算网关', field: '物联网', budget: 200000, status: '已对接', description: '支持多种工业协议解析的边缘计算网关硬件与嵌入式软件开发。' },
  { id: 5, title: '基于区块链的供应链金融平台', field: '金融科技', budget: 800000, status: '招募中', description: '构建透明、不可篡改的供应链金融服务平台，解决中小企业融资难问题。' },
]

const needs = ref<any[]>([])
const fields = ['新材料', '人工智能', '生物医药', '物联网', '金融科技', '智能制造', '航空航天', '新能源']

const fetchNeeds = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/demands?size=100')
    if (response.data && response.data.content) {
      needs.value = response.data.content
    } else if (response.data && Array.isArray(response.data)) {
      needs.value = response.data
    } else {
      needs.value = mockNeeds
    }
  } catch (error) {
    console.warn('API fetch failed, using mock data')
    needs.value = mockNeeds
  } finally {
    loading.value = false
  }
}

onMounted(fetchNeeds)

const filteredNeeds = computed(() => {
  return needs.value.filter(need => {
    const matchesSearch = need.title.includes(searchQuery.value) || need.description.includes(searchQuery.value)
    const matchesField = activeField.value === 'All' || need.field === activeField.value
    return matchesSearch && matchesField
  })
})

const navigateToPublish = () => {
  router.push('/needs/publish')
}

const viewDetails = (id: number) => {
  router.push(`/needs/${id}`)
}

// Helpers
const formatBudget = (val: number) => {
  return val.toLocaleString()
}

const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

const getFieldType = (field: string) => {
  const map: Record<string, string> = {
    '新材料': 'success',
    '人工智能': 'primary',
    '生物医药': 'danger',
    '物联网': 'warning',
    '金融科技': 'info',
    '智能制造': 'primary',
    '航空航天': 'warning',
    '新能源': 'success'
  }
  return map[field] || ''
}

const getStatusClass = (status: string) => {
  if (status === '急需') return 'status-urgent'
  if (status === '已对接') return 'status-done'
  return 'status-active'
}
</script>

<style scoped>
.needs-page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding: 40px 20px;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #fff 0%, #a0a0a0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

.publish-btn {
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.2);
  transition: all 0.3s ease;
  background: linear-gradient(135deg, var(--gold-primary), var(--gold-secondary));
  border: none;
  color: #000;
  font-weight: 600;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 215, 0, 0.3);
}

/* Filter Section */
.filter-section {
  background: var(--bg-card);
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 30px;
  border: 1px solid var(--border-color);
}

.search-bar {
  margin-bottom: 20px;
  max-width: 600px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.05);
  box-shadow: none;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--gold-primary);
}

.tags-filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-label {
  font-weight: 600;
  color: var(--text-secondary);
  margin-right: 8px;
}

.filter-tag {
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 20px;
  transition: all 0.2s;
  background: rgba(255,255,255,0.05);
  color: var(--text-secondary);
  font-size: 14px;
}

.filter-tag:hover {
  color: var(--gold-primary);
  background: rgba(255, 215, 0, 0.1);
}

.filter-tag.is-active {
  background: var(--gold-primary);
  color: #000;
  font-weight: 600;
}

/* Grid */
.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 64px;
  padding: 20px;
}

.need-card {
  height: 100%;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  background: var(--bg-card);
  padding: 32px;
}

.need-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.3);
  border-color: var(--el-color-primary);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 12px;
  box-shadow: inset 0 0 20px rgba(255, 215, 0, 0.05);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

.need-card:hover .card-glow {
  opacity: 1;
}

.need-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(to bottom, var(--el-color-primary), var(--gold-secondary));
  opacity: 0;
  transition: opacity 0.3s;
}

.need-card:hover::before {
  opacity: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.field-tag {
  color: var(--accent-blue);
  background: rgba(64, 158, 255, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.budget {
  font-weight: 700;
  color: var(--el-color-warning); /* Gold */
  font-size: 16px;
  font-family: 'DIN Alternate', sans-serif;
  text-shadow: 0 0 10px rgba(212, 175, 55, 0.3);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  line-height: 1.4;
  height: 50px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 20px 0;
  height: 44px;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.status-indicator {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  background-color: var(--text-secondary);
  box-shadow: 0 0 5px rgba(255,255,255,0.2);
}

.dot.status-active { background-color: var(--success-green); box-shadow: 0 0 8px rgba(103, 194, 58, 0.4); }
.dot.status-urgent { background-color: var(--danger-red); box-shadow: 0 0 8px rgba(245, 108, 108, 0.4); }
.dot.status-done { background-color: var(--text-muted); }

.detail-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--gold-primary);
  font-size: 14px;
  font-weight: 500;
}
</style>
