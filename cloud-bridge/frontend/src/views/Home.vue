<template>
  <div class="home">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-bg-glow"></div>
      <div class="hero-content">
        <h1 class="hero-title">连接科技与产业的智能桥梁</h1>
        <p class="hero-subtitle">基于垂域知识图谱与大模型技术，实现技术成果与企业需求的精准匹配</p>
        <div class="search-box">
          <div class="input-wrapper">
            <el-input
              v-model="searchQuery"
              placeholder="请输入技术关键词或自然语言需求，例如：'高能量密度锂电池正极材料'"
              class="hero-search-input"
              size="large"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch" class="search-btn">
              智能搜索
            </el-button>
          </div>
        </div>
        <div class="hot-tags">
          <span>热门搜索：</span>
          <span v-for="tag in hotTags" :key="tag" class="hot-tag" @click="searchTag(tag)">{{ tag }}</span>
        </div>
      </div>
    </div>

    <!-- Stats Section -->
    <div class="stats-section">
      <el-row :gutter="20" justify="center" class="stats-row">
        <el-col :span="6" v-for="stat in stats" :key="stat.label">
          <div class="stat-item">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Quick Actions -->
    <div class="section-container">
      <h2 class="section-title">核心功能</h2>
      <el-row :gutter="30">
        <el-col :span="8" v-for="action in quickActions" :key="action.title">
          <div class="action-card" @click="$router.push(action.route)">
            <div class="action-icon-wrapper" :style="{ borderColor: action.color, boxShadow: `0 0 15px ${action.color}30` }">
              <el-icon class="action-icon" :style="{ color: action.color }">
                <component :is="action.icon" />
              </el-icon>
            </div>
            <h3>{{ action.title }}</h3>
            <p>{{ action.desc }}</p>
            <div class="card-hover-light"></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Featured Achievements Carousel -->
    <div class="section-container full-width-bg">
      <div class="content-wrapper">
        <h2 class="section-title">精选成果</h2>
        <el-carousel :interval="4000" type="card" height="340px" indicator-position="outside">
          <el-carousel-item v-for="item in featuredAchievements" :key="item.id">
            <div class="carousel-card" @click="$router.push(`/achievements/${item.id}`)">
              <div class="card-glow"></div>
              <div class="carousel-content">
                <div class="card-header">
                  <h3>{{ item.title }}</h3>
                  <div class="price">¥ {{ item.price }}</div>
                </div>
                <div class="tags">
                  <span class="meta-tag field-tag">{{ item.field }}</span>
                  <span class="meta-tag maturity-tag">{{ item.maturity }}</span>
                </div>
                <p>{{ item.desc }}</p>
                <el-button text class="view-btn">查看详情 <el-icon><ArrowRight /></el-icon></el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

   <!-- Policy News Section -->
    <div class="section-container">
      <div class="content-wrapper">
        <h2 class="section-title">政策资讯</h2>
        <el-carousel :interval="5000" height="240px" indicator-position="none" arrow="always">
           <el-carousel-item v-for="i in Math.ceil(policyNews.length / 3)" :key="i">
             <div class="policy-grid-slide">
               <div v-for="policy in policyNews.slice((i-1)*3, i*3)" :key="policy.title" class="policy-card" @click="$router.push(`/libraries/policies/${policy.id || 'P001'}`)">
                  <div class="policy-icon-wrapper">
                     <img v-if="policy.icon" :src="policy.icon" class="policy-img" />
                     <el-icon v-else class="policy-icon"><Document /></el-icon>
                  </div>
                  <div class="policy-content">
                    <h3 class="policy-title">{{ policy.title }}</h3>
                    <div class="policy-meta">
                      <span class="policy-date-text">{{ policy.publishDate }}</span>
                      <el-tag size="small" effect="plain">{{ policy.department }}</el-tag>
                      <el-tag size="small" type="warning">{{ policy.policyType }}</el-tag>
                    </div>
                    <p class="policy-desc">{{ truncateText(policy.content, 60) }}</p>
                  </div>
               </div>
             </div>
           </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <!-- Latest News/Demands -->
    <div class="section-container">
      <div class="content-wrapper">
        <h2 class="section-title">最新需求</h2>
        <el-carousel :interval="6000" height="200px" direction="vertical" indicator-position="none">
          <el-carousel-item v-for="i in Math.ceil(latestDemands.length / 2)" :key="i">
            <el-row :gutter="24">
              <el-col :span="12" v-for="news in latestDemands.slice((i-1)*2, i*2)" :key="news.id">
                <div class="news-card" @click="$router.push(`/needs/${news.id}`)">
                  <div class="news-icon-wrapper">
                    <el-icon class="news-icon"><Notification /></el-icon>
                  </div>
                  <div class="news-content-wrapper">
                    <div class="news-header">
                      <span class="news-title">{{ news.title }}</span>
                      <span class="news-date">{{ news.date }}</span>
                    </div>
                    <p class="news-desc">{{ news.desc }}</p>
                    <div class="news-footer">
                      <span class="field-badge">{{ news.field }}</span>
                      <span class="arrow-icon">→</span>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Compass, Goods, Cpu, ArrowRight, Document, Notification } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const searchQuery = ref('')
const policyNews = ref<any[]>([])

const fetchPolicyNews = async () => {
  try {
    const res = await axios.get('/api/libraries/policies')
    // Parse JSON strings
    const policies = res.data.map((str: string) => JSON.parse(str))
    policyNews.value = policies.slice(0, 9) // Take top 9 for 3 slides
  } catch (e) {
    console.error(e)
  }
}


const getDay = (dateStr: string) => {
   return dateStr ? dateStr.split('-')[2] : '01'
}
const getMonth = (dateStr: string) => {
   return dateStr ? dateStr.split('-')[1] + '月' : '1月'
}
const truncateText = (text: string, len: number) => {
    if(!text) return ''
    return text.length > len ? text.substring(0, len) + '...' : text
}

const hotTags = ref<string[]>([])

const fetchHotTags = async () => {
  try {
    const res = await axios.get('/api/demands/hot-tags')
    if (res.data && res.data.length > 0) {
      hotTags.value = res.data
    } else {
      // Fallback
      hotTags.value = ['石墨烯', '锂电池', '基因编辑', '碳纤维', '人工智能']
    }
  } catch (e) {
    console.error(e)
    hotTags.value = ['石墨烯', '锂电池', '基因编辑', '碳纤维', '人工智能']
  }
}

const featuredAchievements = ref<any[]>([])

const fetchFeaturedAchievements = async () => {
  try {
    const res = await axios.get('/api/achievements', {
      params: { size: 4, sort: 'id,desc' }
    })
    if (res.data && res.data.content) {
      featuredAchievements.value = res.data.content.map((item: any) => ({
        id: item.id,
        title: item.title,
        field: item.field,
        maturity: item.maturity,
        price: item.price ? `${item.price}万` : '面议',
        desc: truncateText(item.description, 60),
        // Use random image if no image provided
        image: item.image || `https://picsum.photos/seed/${item.id}/400/300`
      }))
    }
  } catch (e) {
    console.error('Fetch featured achievements failed:', e)
    // Fallback to hardcoded data if API fails
    featuredAchievements.value = [
      { id: 1, title: '高能量密度锂硫电池正极材料', field: '新能源', maturity: 'TRL5', price: '50万', desc: '基于多孔碳纳米管复合硫载体，比能量密度达到450Wh/kg。' },
      { id: 2, title: '耐高温抗腐蚀石墨烯涂层', field: '新材料', maturity: 'TRL7', price: '80万', desc: '适用于海洋环境，耐温400℃，通过3000小时盐雾测试。' },
      { id: 3, title: 'AI辅助蛋白质结构预测平台', field: '生物医药', maturity: 'TRL8', price: '12万', desc: '预测精度接近AlphaFold2，推理速度提升3倍。' },
      { id: 4, title: '柔性可穿戴汗液传感器', field: '医疗器械', maturity: 'TRL6', price: '30万', desc: '实时监测葡萄糖、乳酸电解质，蓝牙连接智能手机。' }
    ]
  }
}

onMounted(() => {
  fetchPolicyNews()
  fetchHotTags()
  fetchFeaturedAchievements()
})

const stats = [
  { label: '入库技术成果', value: '12,580+' },
  { label: '企业技术需求', value: '3,420+' },
  { label: '入驻专家团队', value: '850+' },
  { label: '累计促成交易', value: '¥ 2.4亿' }
]

const quickActions = [
  { title: '发布需求', desc: '一键发布企业技术难题，全网寻解', icon: Compass, color: 'var(--accent-blue)', route: '/needs' },
  { title: '发布成果', desc: '高校/科研院所成果转化直通车', icon: Goods, color: 'var(--success-green)', route: '/achievements' },
  { title: '智能匹配', desc: 'AI驱动供需精准对接，秒级响应', icon: Cpu, color: 'var(--warning-orange)', route: '/match' }
]

const latestDemands = [
  { id: 1, title: '寻求低成本的生物可降解塑料生产工艺', date: '2026-02-17', field: '新材料', desc: '急需PLA/PBAT共混改性技术，降低生产成本至1.5万元/吨以下。' },
  { id: 2, title: '高精度工业机器人视觉引导系统', date: '2026-02-16', field: '智能制造', desc: '针对异形件抓取，定位精度需达到0.05mm，响应时间<100ms。' },
  { id: 3, title: '抗体偶联药物(ADC)定点偶联技术', date: '2026-02-15', field: '生物医药', desc: '寻求均一性好、DAR值可控的定点偶联技术方案。' },
  { id: 4, title: '新能源汽车固态电池电解质研发', date: '2026-02-14', field: '新能源', desc: '室温离子电导率>5mS/cm，与锂负极界面稳定性好。' }
]

const handleSearch = () => {
  if (searchQuery.value) {
    router.push({ path: '/match', query: { q: searchQuery.value } })
  }
}

const searchTag = (tag: string) => {
  searchQuery.value = tag
  handleSearch()
}
</script>

<style scoped>
.home {
  background-color: var(--bg-primary);
  min-height: 100vh;
}

/* Hero Section */
.hero-section {
  background: radial-gradient(circle at center, var(--bg-card) 0%, var(--bg-primary) 100%);
  color: var(--text-primary);
  padding: 100px 20px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-bg-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.05) 0%, transparent 70%);
  pointer-events: none;
}

.hero-content {
  max-width: 900px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

.hero-title {
  font-size: 56px;
  margin-bottom: 24px;
  font-weight: 800;
  letter-spacing: -1px;
  background: linear-gradient(135deg, #fff 0%, #a0a0a0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 20px;
  margin-bottom: 50px;
  color: var(--text-secondary);
  font-weight: 300;
}

/* Search Box */
.search-box {
  margin-bottom: 30px;
}

.input-wrapper {
  display: flex;
  max-width: 700px;
  margin: 0 auto;
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50px;
  border: 1px solid var(--border-color);
  padding: 4px;
  transition: all 0.3s;
}

.input-wrapper:hover, .input-wrapper:focus-within {
  border-color: var(--gold-primary);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.15);
  background: rgba(0, 0, 0, 0.4);
}

.hero-search-input {
  flex: 1;
}

.hero-search-input :deep(.el-input__wrapper) {
  background-color: transparent;
  box-shadow: none !important;
  padding-left: 15px;
}

.hero-search-input :deep(.el-input__inner) {
  color: #fff;
  font-size: 16px;
  height: 50px;
}

.search-icon {
  font-size: 20px;
  color: var(--text-secondary);
}

.search-btn {
  border-radius: 40px;
  padding: 0 35px;
  font-size: 16px;
  height: 50px;
  background: linear-gradient(135deg, var(--gold-primary), var(--gold-secondary));
  border: none;
  color: #000;
  font-weight: 600;
}

.hot-tags {
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.hot-tag {
  cursor: pointer;
  padding: 4px 12px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.hot-tag:hover {
  color: var(--gold-primary);
  background-color: rgba(255, 215, 0, 0.1);
}

/* Stats Section */
.stats-section {
  background-color: #0f0f0f;
  border-bottom: 1px solid var(--border-color);
  padding: 40px 0;
}

.stats-row {
  max-width: 1200px;
  margin: 0 auto !important;
}

.stat-item {
  text-align: center;
  border-right: 1px solid var(--border-color);
}

.stat-item:last-child {
  border-right: none;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--gold-primary);
  margin-bottom: 5px;
  font-family: 'DIN Alternate', sans-serif;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 14px;
}

/* Common Section Styles */
.section-container {
  padding: 80px 20px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  font-size: 32px;
  margin-bottom: 60px;
  color: var(--text-primary);
  font-weight: 700;
  position: relative;
  display: inline-block;
  width: 100%;
}

.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 3px;
  background: var(--gold-primary);
  margin: 15px auto 0;
  border-radius: 2px;
}

.full-width-bg {
  background-color: #0a0a0a;
  background-image: radial-gradient(circle at 20% 50%, rgba(64, 158, 255, 0.05) 0%, transparent 50%),
                    radial-gradient(circle at 80% 50%, rgba(255, 215, 0, 0.05) 0%, transparent 50%);
  border-top: 1px solid #1a1a1a;
  border-bottom: 1px solid #1a1a1a;
  position: relative;
}

.full-width-bg::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

/* Policy News */
.policy-grid-slide {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  padding: 5px; /* For shadow */
}

.policy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.policy-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.policy-card:hover {
  border-color: var(--gold-primary);
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}

.policy-icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: rgba(255, 215, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid var(--gold-glow);
}

.policy-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

.policy-icon {
  font-size: 28px;
  color: var(--gold-primary);
}

.policy-date-text {
  font-size: 12px;
  color: var(--text-muted);
  margin-right: 8px;
}

/* News Cards */
.news-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  margin-bottom: 0;
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.news-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.news-icon {
  font-size: 24px;
  color: var(--accent-blue);
}

.news-content-wrapper {
  flex: 1;
  min-width: 0;
}


.news-card:hover {
  border-color: var(--text-secondary);
  background: var(--bg-card-hover);
}

.news-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.news-title {
  font-weight: 600;
  font-size: 16px;
  color: var(--text-primary);
}

.news-date {
  color: var(--text-muted);
  font-size: 13px;
}

.news-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 20px;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.field-badge {
  font-size: 12px;
  color: var(--text-secondary);
  background: #222;
  padding: 2px 8px;
  border-radius: 4px;
}

.arrow-icon {
  color: var(--gold-primary);
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s;
}

.news-card:hover .arrow-icon {
  opacity: 1;
  transform: translateX(0);
}
</style>