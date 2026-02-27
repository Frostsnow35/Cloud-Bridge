<template>
  <div class="library-page">
    <div class="section-container">
      <!-- Header Area -->
      <div class="page-header">
        <div class="header-content">
          <h2 class="page-title">{{ categoryTitle }}</h2>
          <p class="page-subtitle">{{ categorySubtitle }}</p>
        </div>
      </div>

      <!-- Search & Filter Bar -->
      <div class="filter-section">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            :placeholder="`在${categoryTitle}中搜索...`"
            size="large"
            class="search-input"
            @keyup.enter="fetchData"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="fetchData">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <!-- Content Grid -->
      <div class="content-grid" v-loading="loading">
        <el-empty v-if="!loading && items.length === 0" description="暂无相关数据" />
        
        <div class="results-grid" v-else>
          <div v-for="(item, index) in items" :key="index" class="grid-item" @click="goToDetail(item)">
            <!-- Dynamic Card based on Category -->
            
            <!-- Policy Card -->
            <div v-if="category === 'policies'" class="resource-card policy-card">
              <div class="card-header">
                <el-tag size="small" type="warning">{{ item.policyType }}</el-tag>
                <span class="date">{{ item.publishDate }}</span>
              </div>
              <h3 class="card-title">{{ item.title }}</h3>
              <div class="card-meta">
                <span><el-icon><OfficeBuilding /></el-icon> {{ item.department }}</span>
              </div>
              <p class="card-desc">{{ truncateText(item.content, 120) }}</p>
              <div class="card-footer">
                <el-tag v-for="tag in item.industry" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
              </div>
            </div>

            <!-- Expert Card -->
            <div v-else-if="category === 'experts'" class="resource-card expert-card">
              <div class="expert-profile">
                <el-avatar :size="64" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${item.name}`" @error="() => true">
                  <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                </el-avatar>
                <div class="expert-info">
                  <h3 class="expert-name">{{ item.name }}</h3>
                  <p class="expert-title">{{ item.title }} @ {{ item.affiliation }}</p>
                </div>
              </div>
              <div class="expert-fields">
                <el-tag v-for="field in item.field" :key="field" size="small" type="success">{{ field }}</el-tag>
              </div>
              <p class="expert-achievements">{{ truncateText(item.achievements, 100) }}</p>
              <div class="card-actions">
                <el-button type="primary" link @click="contactExpert(item)">联系专家</el-button>
              </div>
            </div>

            <!-- Fund Card -->
            <div v-else-if="category === 'funds'" class="resource-card fund-card">
              <div class="card-header">
                <el-tag size="small" type="danger">{{ item.fundType }}</el-tag>
                <span class="amount">{{ item.amountRange }}</span>
              </div>
              <h3 class="card-title">{{ item.name }}</h3>
              <div class="card-meta">
                <span>提供方: {{ item.provider }}</span>
              </div>
              <div class="fund-details">
                <div class="detail-item">
                  <span class="label">利率/占比:</span>
                  <span class="value">{{ item.interestRate || '面议' }}</span>
                </div>
              </div>
              <div class="card-footer">
                <el-tag v-for="tag in item.industryFocus" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
              </div>
            </div>

            <!-- Equipment Card -->
            <div v-else-if="category === 'equipments'" class="resource-card equipment-card">
              <div class="card-header">
                <el-tag size="small" :type="item.availability === 'Available' ? 'success' : 'info'">
                  {{ item.availability === 'Available' ? '可预约' : '维护中' }}
                </el-tag>
                <span class="category">{{ item.category }}</span>
              </div>
              <h3 class="card-title">{{ item.name }}</h3>
              <div class="card-meta">
                <span><el-icon><Location /></el-icon> {{ item.facilityName }}</span>
              </div>
              <p class="specs"><strong>规格:</strong> {{ item.specs }}</p>
              <div class="owner-info">所属单位: {{ item.owner }}</div>
            </div>

            <!-- Patent Card -->
            <div v-else-if="category === 'patents'" class="resource-card patent-card">
              <div class="card-header">
                <el-tag size="small" type="success">{{ item.status }}</el-tag>
                <span class="date">{{ item.publicationDate }}</span>
              </div>
              <h3 class="card-title">{{ item.title }}</h3>
              <div class="card-meta">
                <span><el-icon><Document /></el-icon> {{ item.patentNumber }}</span>
              </div>
              <p class="card-desc">{{ truncateText(item.abstractText, 100) }}</p>
              <div class="card-footer">
                <span class="assignee">专利权人: {{ item.assignee }}</span>
              </div>
            </div>

            <!-- Enterprise Card -->
            <div v-else-if="category === 'enterprises'" class="resource-card enterprise-card">
              <div class="card-header">
                <el-tag size="small" type="info">{{ item.industry }}</el-tag>
                <span class="location">{{ item.location }}</span>
              </div>
              <h3 class="card-title">{{ item.name }}</h3>
              <div class="card-meta">
                <span>规模: {{ item.scale }}</span>
              </div>
              <p class="card-desc">{{ truncateText(item.description, 100) }}</p>
              <div class="card-footer">
                 <el-button type="primary" size="small" plain @click="viewEnterprise(item)">查看主页</el-button>
              </div>
            </div>

            <!-- Public Platform Card -->
            <div v-else-if="category === 'public_platforms'" class="resource-card public-platform-card">
              <div class="card-header">
                <el-tag size="small" type="primary">{{ item.format || 'API' }}</el-tag>
                <span class="date">{{ item.updateFrequency || '不定时更新' }}</span>
              </div>
              <h3 class="card-title">{{ item.name }}</h3>
              <div class="card-meta">
                <span><el-icon><OfficeBuilding /></el-icon> {{ item.provider }}</span>
              </div>
              <p class="card-desc">{{ truncateText(item.description, 100) }}</p>
              <div class="card-footer">
                <el-tag size="small" effect="plain">{{ item.domain || '公共服务' }}</el-tag>
              </div>
            </div>

            <!-- Fallback Card -->
            <div v-else class="resource-card">
              <h3 class="card-title">{{ item.name || item.title }}</h3>
              <p class="card-desc">{{ item.description || item.content || item.abstractText }}</p>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, OfficeBuilding, Location, User, Money, Document } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const category = computed(() => route.params.category as string)
const searchQuery = ref('')
const loading = ref(false)
const items = ref<any[]>([])

const categoryMap: Record<string, { title: string, subtitle: string }> = {
  policies: { title: '政策库', subtitle: '汇聚国家及地方科技成果转化扶持政策' },
  experts: { title: '专家库', subtitle: '连接全球顶尖科研人才与行业领军人物' },
  funds: { title: '资金库', subtitle: '提供多元化的科技金融与投融资支持' },
  equipments: { title: '设备库', subtitle: '共享高端科研仪器设备，助力研发创新' },
  patents: { title: '专利库', subtitle: '海量专利数据，挖掘核心技术价值' },
  enterprises: { title: '企业库', subtitle: '汇集优质科技企业，促成校企精准对接' },
  public_platforms: { title: '公共平台', subtitle: '汇聚各类公共服务平台数据资源' }
}

const categoryTitle = computed(() => categoryMap[category.value]?.title || '资源库')
const categorySubtitle = computed(() => categoryMap[category.value]?.subtitle || '全方位资源支撑')

const fetchData = async () => {
  loading.value = true
  try {
    const response = await axios.get(`/api/libraries/${category.value}`, {
      params: { keyword: searchQuery.value }
    })
    // ES returns list of JSON strings, need to parse
    items.value = response.data.map((str: string) => JSON.parse(str))
  } catch (error) {
    console.error('Fetch library data failed:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

const contactExpert = (expert: any) => {
  ElMessage.success(`正在为您连接专家 ${expert.name}...`)
}

const goToDetail = (item: any) => {
  if (item.id) {
    router.push(`/libraries/${category.value}/${item.id}`)
  } else {
    ElMessage.warning('该资源暂无详情')
  }
}

const viewEnterprise = (ent: any) => {
  // ElMessage.info(`正在跳转至 ${ent.name} 的企业主页...`)
  if (ent.id) {
    router.push(`/libraries/${category.value}/${ent.id}`)
  } else {
    router.push(`/enterprise/${ent.id}`)
  }
}

onMounted(fetchData)

watch(() => route.params.category, () => {
  searchQuery.value = ''
  fetchData()
})
</script>

<style scoped>
.library-page {
  padding: 40px 0;
  background-color: #121212;
  min-height: 100vh;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  margin-bottom: 40px;
  text-align: center;
}

.page-title {
  font-size: 32px;
  color: #FFD700;
  margin-bottom: 12px;
  font-weight: 700;
}

.page-subtitle {
  font-size: 16px;
  color: #a0a0a0;
}

.filter-section {
  margin-bottom: 30px;
}

.search-bar {
  max-width: 600px;
  margin: 0 auto;
}

.search-input :deep(.el-input__wrapper) {
  background-color: #1e1e1e;
  box-shadow: 0 0 0 1px #333;
}

.search-input :deep(.el-input__inner) {
  color: #fff;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.resource-card {
  background: #1e1e1e;
  border: 1px solid #333;
  border-radius: 12px;
  padding: 30px;
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.resource-card:hover {
  transform: translateY(-5px);
  border-color: #FFD700;
  box-shadow: 0 12px 30px rgba(255, 215, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-title {
  font-size: 20px;
  color: #fff;
  margin: 0 0 16px 0;
  line-height: 1.5;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.card-meta {
  font-size: 13px;
  color: #888;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-desc {
  font-size: 15px;
  color: #a0a0a0;
  line-height: 1.8;
  margin-bottom: 20px;
  flex-grow: 1;
}

.card-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: auto;
}

/* Expert Card Specifics */
.expert-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.expert-name {
  font-size: 18px;
  color: #fff;
  margin: 0;
}

.expert-title {
  font-size: 13px;
  color: #888;
  margin: 4px 0 0 0;
}

.expert-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.expert-achievements {
  font-size: 14px;
  color: #a0a0a0;
  margin-bottom: 16px;
  line-height: 1.5;
}

/* Fund Card Specifics */
.amount {
  font-size: 16px;
  color: #ff4d4f;
  font-weight: 600;
}

.fund-details {
  background: #252525;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.detail-item .label { color: #888; }
.detail-item .value { color: #FFD700; }

/* Equipment Card Specifics */
.specs {
  font-size: 13px;
  color: #a0a0a0;
  margin-bottom: 8px;
}

.owner-info {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}

.date {
  font-size: 12px;
  color: #666;
}
</style>
