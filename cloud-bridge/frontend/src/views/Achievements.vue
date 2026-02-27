<template>
  <div class="achievements-page">
    <div class="section-container">
      <!-- Header Area -->
      <div class="page-header">
        <div class="header-content">
          <h2 class="page-title">成果大厅</h2>
          <p class="page-subtitle">顶尖科研成果展示，加速科技价值转化</p>
        </div>
        <div class="actions">
           <!-- Publish Button -->
           <el-button type="primary" size="large" class="publish-btn" @click="$router.push('/achievements/publish')">发布成果</el-button>
        </div>
      </div>

      <!-- Search & Filter Bar -->
      <div class="filter-section">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索成果技术、应用场景..."
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
            @click="toggleField('All')"
          >
            全部
          </div>
          <div
            v-for="field in fields"
            :key="field"
            :class="['filter-tag', { 'is-active': activeField === field }]"
            @click="toggleField(field)"
          >
            {{ field }}
          </div>
        </div>
      </div>

      <!-- Content Grid -->
      <div class="content-grid" v-loading="loading">
        <el-empty v-if="!loading && filteredAchievements.length === 0" description="暂无符合条件的成果" />
        
        <div class="results-grid" v-else>
          <div v-for="item in filteredAchievements" :key="item.id" class="grid-item">
            <div class="achievement-card" @click="viewDetails(item.id)">
              <div class="card-header">
                <span class="field-tag">{{ item.field }}</span>
                <span class="price" v-if="item.price">¥ {{ formatPrice(item.price) }}</span>
                <span class="price negotiable" v-else>面议</span>
              </div>
              
              <h3 class="card-title" :title="item.title">{{ item.title }}</h3>
              <p class="card-desc">{{ truncateText(item.description, 60) }}</p>
              
              <div class="card-footer">
                <div class="institution">
                  <el-icon><School /></el-icon> {{ truncateText(item.institution || '知名高校实验室', 12) }}
                </div>
                <div class="maturity-badge">
                   {{ item.maturity || '成熟应用' }}
                </div>
              </div>
              <div class="card-glow"></div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 36, 48]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, ArrowRight, Trophy, School } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const searchQuery = ref('')
const activeField = ref('All')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const achievements = ref<any[]>([])
const fields = ['新材料', '人工智能', '生物医药', '物联网', '金融科技', '智能制造', '航空航天', '新能源']

const fetchAchievements = async () => {
  loading.value = true
  try {
    const params: any = {
        keyword: searchQuery.value,
        field: activeField.value === 'All' ? '' : activeField.value,
        page: currentPage.value - 1,
        size: pageSize.value
    }
    const response = await axios.get('/api/achievements', { params })
    
    const data = response.data
    if (data && data.content) {
       achievements.value = data.content
       total.value = data.totalElements
    } else if (Array.isArray(data)) {
       achievements.value = data
       total.value = data.length
    } else {
       achievements.value = []
       total.value = 0
    }
  } catch (error) {
    console.error('API fetch failed:', error)
    achievements.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const toggleField = (field: string) => {
  if (activeField.value === field && field !== 'All') {
    activeField.value = 'All'
  } else {
    activeField.value = field
  }
  currentPage.value = 1
  fetchAchievements()
}

// Watch search query with debounce
let timeout: any = null
watch(searchQuery, (newVal) => {
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(() => {
        currentPage.value = 1
        fetchAchievements()
    }, 500)
})

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchAchievements()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchAchievements()
}

onMounted(() => {
    if (route.query.keyword) {
        searchQuery.value = route.query.keyword as string
    }
    fetchAchievements()
})

const filteredAchievements = computed(() => {
  return achievements.value
})

const viewDetails = (id: number) => {
  router.push(`/achievements/${id}`)
}

// Helpers
const formatPrice = (val: number) => {
  if (val >= 10000) {
    return (val / 10000).toFixed(0) + '万'
  }
  return val.toLocaleString()
}

const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}
</script>

<style scoped>
.achievements-page {
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

.achievement-card {
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

.achievement-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.3);
  border-color: var(--el-color-primary);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 50% 0%, rgba(255,255,255,0.05), transparent 70%);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

.achievement-card:hover .card-glow {
  opacity: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.field-tag {
  font-size: 12px;
  color: var(--gold-primary);
  border: 1px solid var(--gold-primary);
  padding: 2px 8px;
  border-radius: 4px;
}

.price {
  color: var(--el-color-danger);
  font-weight: 600;
}

.price.negotiable {
  color: var(--text-secondary);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  line-height: 1.4;
  height: 50px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 24px;
  flex-grow: 1;
  height: 66px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
  font-size: 13px;
  color: var(--text-secondary);
}

.institution {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-container {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}
</style>