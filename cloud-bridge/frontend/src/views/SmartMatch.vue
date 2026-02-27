<template>
  <div class="smart-match-page">
    <div class="match-container">
      <!-- Search Section -->
      <div :class="['search-hero', { 'is-collapsed': hasSearched }]">
        <h2 v-if="!hasSearched" class="hero-title">AI 智能匹配引擎</h2>
        <p v-if="!hasSearched" class="subtitle">输入技术需求，DeepSeek 大模型为您精准匹配全球成果</p>
        
        <div class="search-box-wrapper">
          <div class="input-container">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="hasSearched ? 2 : 4"
              placeholder="例如：寻找一种用于航空航天的高性能碳纤维复合材料，要求耐高温300度以上..."
              class="search-input"
              resize="none"
              @keyup.enter="onMatch"
            />
            <div class="input-glow"></div>
          </div>
          <el-button type="primary" class="match-btn" @click="onMatch" :loading="loading">
            <el-icon><Cpu /></el-icon> {{ loading ? '深度推演中...' : '开始匹配' }}
          </el-button>
        </div>

        <!-- Platform Hot Search Word Cloud (3D) -->
        <div v-if="!hasSearched" class="hot-search-cloud-3d animate__animated animate__fadeInUp">
            <div class="hot-title"><el-icon><TrendCharts /></el-icon> 平台热搜风向标</div>
            <div class="cloud-stage" ref="cloudStage">
                <div class="cloud-container" :style="cloudStyle">
                    <div 
                        v-for="(word, index) in hotWords" 
                        :key="index"
                        class="cloud-item"
                        :style="getWordStyle(index)"
                        @click="searchTag(word.text)"
                    >
                        <div class="word-content" :style="{ animationDelay: (index * 0.2) + 's' }">
                            <span class="word-text" :style="{ color: getWordColor(word.score) }">{{ word.text }}</span>
                            <span class="word-score">🔥{{ word.score }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
      </div>

      <!-- Results Section -->
      <transition name="fade-slide">
        <div v-if="hasSearched" class="results-section">
          <!-- Analysis Dashboard -->
          <div class="dashboard-grid">
            <!-- 1. Deduction Graph -->
            <div class="dashboard-card graph-card">
              <div class="card-header-dark">
                <el-icon><Share /></el-icon>
                <span>AI 推演路径</span>
              </div>
              <div ref="chartDom" class="chart-box"></div>
            </div>
          </div>

          <div class="section-header">
            <h3>{{ exactMatches.length > 0 ? `匹配结果 (${exactMatches.length})` : '匹配结果' }}</h3>
            <div class="header-actions">
              <el-tag effect="dark" class="keyword-tag">核心词：{{ extractedKeyword || '自动识别' }}</el-tag>
            </div>
          </div>
          
          <el-empty v-if="!loading && exactMatches.length === 0 && recommendations.length === 0" description="暂无匹配结果，请尝试优化描述" />
          
          <div v-if="!loading && exactMatches.length === 0 && recommendations.length > 0" class="recommendation-hint">
            <el-alert
              title="未找到完全匹配的成果，已为您推荐相关领域的优质项目"
              type="info"
              show-icon
              :closable="false"
              class="mb-4"
            />
          </div>

          <div class="results-grid">
            <!-- Exact Matches -->
            <div v-for="item in exactMatches" :key="'exact-' + item.id" class="result-card-wrapper">
              <div class="result-card exact-match-card" @click="goToDetail(item.id)">
                <div class="match-badge">匹配结果</div>
                <div class="card-status">
                  <span :class="['status-dot', getStatusType(item.status)]"></span>
                  {{ item.status }}
                </div>
                <h4 class="title" :title="item.title">{{ item.title }}</h4>
                <p class="description">{{ item.description }}</p>
                
                <div class="card-meta">
                  <div class="tags">
                    <span class="meta-tag field-tag">{{ item.field }}</span>
                    <span class="meta-tag maturity-tag">{{ item.maturity }}</span>
                  </div>
                  <div class="price">
                    <span class="currency">¥</span>
                    <span class="amount">{{ item.price ? item.price.toLocaleString() : '面议' }}</span>
                  </div>
                </div>
                <div class="card-hover-effect"></div>
              </div>
            </div>
          </div>
          
          <!-- Recommendations Section (Separate from Exact Matches) -->
          <div v-if="recommendations.length > 0" class="recommendations-section">
              <div class="section-header" style="margin-top: 40px;">
                <h3>{{ exactMatches.length > 0 ? '猜你想搜 / 相关推荐' : '为您推荐' }}</h3>
              </div>
              <div class="results-grid">
                <div v-for="item in recommendations" :key="'rec-' + item.id" class="result-card-wrapper">
                  <div class="result-card rec-card" @click="goToDetail(item.id)">
                    <div class="rec-badge" v-if="exactMatches.length > 0">相关推荐</div>
                    <div class="card-status">
                      <span :class="['status-dot', getStatusType(item.status)]"></span>
                      {{ item.status }}
                    </div>
                    <h4 class="title" :title="item.title">{{ item.title }}</h4>
                    <p class="description">{{ item.description }}</p>
                    
                    <div class="card-meta">
                      <div class="tags">
                        <span class="meta-tag field-tag">{{ item.field }}</span>
                        <span class="meta-tag maturity-tag">{{ item.maturity }}</span>
                      </div>
                      <div class="price">
                        <span class="currency">¥</span>
                        <span class="amount">{{ item.price ? item.price.toLocaleString() : '面议' }}</span>
                      </div>
                    </div>
                    <div class="card-hover-effect"></div>
                  </div>
                </div>
              </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
// Icons are globally registered in main.ts, but explicit import is safe
import { Cpu, Share, Connection, Search, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()

// 3D Cloud Logic
const cloudStage = ref<HTMLElement | null>(null)
// Static rotation for a nice angle
const rotationX = ref(-10) 
const rotationY = ref(20)

// Expanded Hot Words for 3D Cloud
const hotWords = ref([
    { text: '人工智能', score: 9820 }, { text: '石墨烯', score: 8540 },
    { text: '工业互联', score: 7630 }, { text: '量子加密', score: 6210 },
    { text: '生物医药', score: 5400 }, { text: '碳中和', score: 4890 },
    { text: '自动驾驶', score: 4320 }, { text: '脑机接口', score: 3980 },
    { text: '6G通信', score: 3500 }, { text: '数字孪生', score: 3200 },
    { text: '边缘计算', score: 3100 }, { text: '柔性电子', score: 2900 },
    { text: '氢能源', score: 2800 }, { text: '智慧农业', score: 2700 }
])

const cloudStyle = computed(() => ({
    transform: `rotateX(${rotationX.value}deg) rotateY(${rotationY.value}deg)`
}))

const getWordColor = (score: number) => {
    if (score > 8000) return '#F56C6C'
    if (score > 5000) return '#E6A23C'
    if (score > 3000) return '#409EFF'
    return '#67C23A'
}

const getWordStyle = (index: number) => {
    const len = hotWords.value.length
    // Distribution algorithm (Golden Spiral Sphere)
    const phi = Math.acos(-1 + (2 * index) / len)
    const theta = Math.sqrt(len * Math.PI) * phi
    
    const radius = 200 // Slightly larger radius
    
    const x = radius * Math.cos(theta) * Math.sin(phi)
    const y = radius * Math.sin(theta) * Math.sin(phi)
    const z = radius * Math.cos(phi)
    
    return {
        transform: `translate3d(${x}px, ${y}px, ${z}px)`,
        opacity: (z + radius) / (2 * radius) + 0.4 // Enhanced visibility
    }
}

// Removed interactive rotation and animation loop as requested
// Replaced with CSS animations in template

interface Achievement {
  id: number;
  title: string;
  description: string;
  field: string;
  maturity: string;
  price: number;
  status: string;
  ownerId: number;
}

interface CloudTag {
  text: string;
  size: number;
  color: string;
  opacity: number;
}

const form = reactive({
  description: ''
})
const loading = ref(false)
const hasSearched = ref(false)
const exactMatches = ref<Achievement[]>([])
const recommendations = ref<Achievement[]>([])
const extractedKeyword = ref('')
const aiGraph = ref<any>(null)
const relatedTags = ref<CloudTag[]>([])
const chartDom = ref<HTMLElement | null>(null)
let myChart: any | null = null

// Generate related tags based on backend keywords
const generateRelatedTags = (keywords: string[]) => {
  if (!keywords || keywords.length === 0) return

  const tags: CloudTag[] = []
  // First one is usually the main keyword
  if (keywords.length > 0) {
    tags.push({ text: keywords[0], size: 28, color: '#FFD700', opacity: 1 })
  }
  
  // Others
  for (let i = 1; i < keywords.length; i++) {
    tags.push({
      text: keywords[i],
      size: 12 + Math.random() * 12, // 12-24px
      color: Math.random() > 0.5 ? '#e0e0e0' : '#a0a0a0',
      opacity: 0.6 + Math.random() * 0.4
    })
  }
  relatedTags.value = tags
}

const searchTag = (text: string) => {
  form.description = text
  onMatch()
}

const goToDetail = (id: number) => {
  router.push(`/achievements/${id}`)
}

const renderChart = () => {
  if (!chartDom.value) return
  
  try {
  if (myChart) {
    myChart.dispose()
  }
  myChart = echarts.init(chartDom.value)

  const rootName = '需求'
  const keywordNodeName = extractedKeyword.value || '关键技术'
  
  const nodes: any[] = []
  const links: any[] = []

  if (aiGraph.value && aiGraph.value.nodes && aiGraph.value.nodes.length > 0) {
      // AI Dynamic Graph Mode
          const nodeMap = new Map()
          aiGraph.value.nodes.forEach((n: any) => {
              let color = '#fac858' // Default Tech
              let size = 40
              let borderColor = '#fff'
              
              if (n.type === 'Demand') { // Fixed: type name match
                  color = '#ee6666'; size = 50; borderColor = '#FFD700';
              } else if (n.type === 'Technology' || n.type === 'InferredTech') { 
                  color = '#5470c6'; size = 45; borderColor = '#409EFF';
              } else if (n.type === 'Expert') { 
                  color = '#73c0de'; size = 40; 
              } else if (n.type === 'Application') {
                  color = '#91cc75'; size = 35;
              } else if (n.type === 'Category') { // Added Category color
                  color = '#E6A23C'; size = 45; borderColor = '#F56C6C';
              }
              
              nodes.push({
                  id: n.id,
                  name: n.label,
                  type: n.type, // Important: Pass type to ECharts data
                  symbolSize: size,
                  itemStyle: { color, borderColor, borderWidth: 2 },
                  label: { show: true, position: 'bottom', color: '#fff', fontSize: 12 }
              })
              nodeMap.set(n.id, true)
          })
      
      if (aiGraph.value.relationships) {
          aiGraph.value.relationships.forEach((r: any) => {
             if (nodeMap.has(r.source) && nodeMap.has(r.target)) {
                 links.push({ source: r.source, target: r.target, value: r.type || 'related' })
             }
          });
      }
      
      // Link Top Achievements
      const sourceList = exactMatches.value.length > 0 ? exactMatches.value : recommendations.value
      if (nodes.length > 0 && sourceList.length > 0) {
          const anchorId = nodes[0].id // Link to root (Requirement)
          sourceList.slice(0, 5).forEach(res => {
              const resId = `res-${res.id}`
              nodes.push({
                  id: resId,
                  name: res.title.length > 6 ? res.title.substring(0, 6) + '..' : res.title,
                  fullTitle: res.title, // Add fullTitle for Tooltip
                  price: res.price, // Add price for Tooltip
                  type: 'Achievement', // Explicitly set type
                  symbolSize: 30,
                  itemStyle: { color: '#3ba272', borderColor: '#67C23A' },
                  label: { show: true, fontSize: 10, color: '#ccc' }
              })
              links.push({ source: anchorId, target: resId, value: '匹配成果' })
          });
      }

  } else {
      // Legacy / Fallback Graph Mode
          // 1. Requirement Node
          nodes.push({
            id: 'req',
            name: rootName,
            type: 'Demand', // Add type
            symbolSize: 40,
            itemStyle: { 
              color: '#000', 
              borderColor: '#FFD700',
              borderWidth: 2,
              shadowBlur: 10,
              shadowColor: '#FFD700'
            },
            label: { color: '#FFD700', fontSize: 12, position: 'bottom' }
          })
    
          // 2. Keyword Node
          nodes.push({
            id: 'key',
            name: keywordNodeName,
            type: 'Technology', // Add type
            symbolSize: 50,
            itemStyle: { 
              color: '#1a1a1a', 
              borderColor: '#409EFF',
              borderWidth: 3,
              shadowBlur: 15,
              shadowColor: '#409EFF'
            },
            label: { color: '#fff', fontSize: 14, fontWeight: 'bold' }
          })
          
          links.push({ source: 'req', target: 'key', value: '提取' })
    
          // 3. Top Results (from exact matches or recommendations)
          const sourceList = exactMatches.value.length > 0 ? exactMatches.value : recommendations.value
          const topResults = sourceList.slice(0, 5)
          
          topResults.forEach((res, idx) => {
            const id = `res-${res.id}`
            nodes.push({
              id: id,
              name: res.title.length > 6 ? res.title.substring(0, 6) + '..' : res.title,
              fullTitle: res.title, // Add fullTitle
              price: res.price, // Add price
              type: 'Achievement', // Add type
              symbolSize: 30,
              itemStyle: { 
                color: '#111', 
                borderColor: '#67C23A',
                borderWidth: 1 
              },
              label: { color: '#ccc', fontSize: 10 }
            })
            
            links.push({ source: 'key', target: id, value: '匹配' })
          });
  }

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0,0,0,0.8)',
      borderColor: '#333',
      textStyle: { color: '#fff' },
      formatter: (params: any) => {
        if (params.dataType === 'edge') {
           return `${params.data.source} > ${params.data.target}`;
        }
        const node = params.data;
        // Map types to user-friendly names
        const typeMap: Record<string, string> = {
            'Demand': '需求核心',
            'Category': '所属领域',
            'SubCategory': '技术路线',
            'Technology': '关键技术',
            'Application': '应用场景',
            'Achievement': '匹配成果'
        };
        const typeName = typeMap[node.type] || node.type;
        return `
            <div style="font-weight:bold; margin-bottom:4px;">${node.name}</div>
            <div style="font-size:12px; color:#aaa;">类型：${typeName}</div>
            ${node.fullTitle ? `<div style="font-size:12px; color:#aaa; margin-top:2px;">全称：${node.fullTitle}</div>` : ''}
            ${node.price ? `<div style="font-size:12px; color:#FFD700; margin-top:2px;">价格：¥${node.price}</div>` : ''}
        `;
      }
    },
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: nodes,
        links: links,
        roam: true,
        label: {
          show: true,
          position: 'right'
        },
        edgeSymbol: ['circle', 'arrow'],
        edgeSymbolSize: [4, 10],
        edgeLabel: {
          fontSize: 10,
          color: '#666'
        },
        lineStyle: {
          color: '#555',
          opacity: 0.6,
          curveness: 0.1,
          width: 2
        },
        force: {
          repulsion: 500,
          edgeLength: 120,
          gravity: 0.1
        }
      }
    ]
  };

  myChart.setOption(option as any);
  window.addEventListener('resize', handleResize);
  } catch (e) {
      console.warn('Chart render failed:', e);
  }
}

const handleResize = () => {
  myChart?.resize()
}

const isComponentMounted = ref(true)

onMounted(() => {
  if (route.query.q) {
    form.description = route.query.q as string
    onMatch()
  }
})

// Clean up
onUnmounted(() => {
    isComponentMounted.value = false
    if (myChart) {
        myChart.dispose()
        myChart = null
    }
    window.removeEventListener('resize', handleResize)
})

const getStatusType = (status: string) => {
  switch (status) {
    case 'PUBLISHED': return 'status-published';
    case 'PENDING_REVIEW': return 'status-pending';
    default: return 'status-default';
  }
}

const onMatch = async () => {
  if (!form.description) {
    ElMessage.warning('请输入需求描述')
    return
  }
  loading.value = true
  hasSearched.value = true
  exactMatches.value = []
  recommendations.value = []
  
  try {
    const response = await axios.post('/api/matching/match', {
      description: form.description
    })
    
    // Handle new response structure
    if (response.data && typeof response.data === 'object' && 'matches' in response.data) {
        exactMatches.value = response.data.matches || []
        recommendations.value = response.data.recommendations || []
        const keywords = response.data.relatedKeywords || []
        
        aiGraph.value = response.data.aiGraph || null
        
        if (keywords.length > 0) {
            extractedKeyword.value = keywords[0]
            generateRelatedTags(keywords)
        } else {
             // Fallback if backend returns empty keywords
             extractedKeyword.value = form.description.substring(0, 4)
             generateRelatedTags([extractedKeyword.value])
        }
    } else {
        // Fallback for old API structure (List<Achievement>)
        if (Array.isArray(response.data)) {
            exactMatches.value = response.data
            aiGraph.value = null // Reset if old structure
        }
    }
    
    nextTick(() => {
      renderChart()
    })

    if (exactMatches.value.length === 0) {
      if (recommendations.value.length > 0) {
        ElMessage.info('暂无精确匹配，已为您推荐相关成果')
      } else {
        ElMessage.warning('未找到匹配成果')
      }
    } else {
      ElMessage.success(`找到 ${exactMatches.value.length} 个高匹配成果`)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('匹配服务连接失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.mb-4 {
    margin-bottom: 16px;
}
/* Use Global Variables from theme.css */
.smart-match-page {
  min-height: calc(100vh - 60px);
  background: radial-gradient(circle at top right, var(--bg-card) 0%, var(--bg-primary) 100%);
  padding: 40px 20px;
  color: var(--text-primary);
  font-family: 'Inter', 'Helvetica Neue', sans-serif;
}

.match-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Hero Section */
.search-hero {
  text-align: center;
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1);
  margin-top: 15vh;
}

.search-hero.is-collapsed {
  margin-top: 0;
  display: flex;
  align-items: center;
  gap: 30px;
  background: rgba(20, 20, 20, 0.8);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 16px;
  border: 1px solid var(--border-color);
  margin-bottom: 30px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
}

.hero-title {
  font-size: 48px;
  background: linear-gradient(135deg, #fff 0%, #a0a0a0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 16px;
  letter-spacing: -1px;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 18px;
  margin-bottom: 48px;
  font-weight: 300;
}

/* Search Box */
.search-box-wrapper {
  position: relative;
  max-width: 800px;
  margin: 0 auto 50px;
  width: 100%;
}

.is-collapsed .search-box-wrapper {
  margin: 0;
  flex: 1;
  display: flex;
  gap: 15px;
  align-items: center;
}

.input-container {
  position: relative;
  width: 100%;
}

.search-input :deep(.el-textarea__inner) {
  background-color: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  color: #fff;
  border-radius: 12px;
  padding: 18px;
  font-size: 16px;
  transition: all 0.3s;
}

.search-input :deep(.el-textarea__inner:focus) {
  border-color: var(--gold-primary);
  background-color: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 0 2px var(--gold-glow);
}

.match-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  padding: 10px 24px;
  font-size: 15px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--gold-primary), var(--gold-secondary));
  border: none;
  color: #000;
  font-weight: 600;
  transition: transform 0.2s;
}

.match-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 0 15px var(--gold-glow);
}

.is-collapsed .match-btn {
  position: static;
  height: 54px;
}

/* Dashboard Grid */
.dashboard-grid {
  display: block; /* Changed from grid to block */
  margin-bottom: 40px;
}

.dashboard-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  overflow: hidden;
  height: 500px; /* Increased height for better graph view */
  display: flex;
  flex-direction: column;
}

.card-header-dark {
  padding: 16px 20px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #d0d0d0;
}

.chart-box {
  flex: 1;
  width: 100%;
}

/* 3D Hot Search Cloud Styles */
.hot-search-cloud-3d {
    max-width: 800px;
    margin: 0 auto;
    text-align: center;
}

.cloud-stage {
    height: 400px;
    width: 100%;
    perspective: 800px;
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
}

.cloud-container {
    width: 0;
    height: 0;
    position: absolute;
    transform-style: preserve-3d;
    transition: transform 0.1s linear;
}

.cloud-item {
    position: absolute;
    left: 0;
    top: 0;
    transform-style: preserve-3d;
    cursor: pointer;
    white-space: nowrap;
    user-select: none;
}


.word-content {
    display: inline-block;
    animation: float 6s ease-in-out infinite, pulse 4s ease-in-out infinite;
    transform-style: preserve-3d;
}

@keyframes float {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-15px); }
}

@keyframes pulse {
    0%, 100% { filter: brightness(1); text-shadow: 0 0 5px rgba(255,255,255,0.1); }
    50% { filter: brightness(1.3); text-shadow: 0 0 15px rgba(255,255,255,0.4); }
}

.cloud-item:hover .word-text {
    text-shadow: 0 0 15px currentColor;
    transform: scale(1.2);
}

.word-text {
    font-size: 1.1rem;
    font-weight: bold;
    transition: all 0.3s;
    display: inline-block;
}

.word-score {
    font-size: 0.7rem;
    color: #888;
    margin-left: 4px;
    opacity: 0.6;
}

.hot-title {
    color: rgba(255, 255, 255, 0.9);
    font-size: 1.1rem;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-weight: bold;
}

/* Word Cloud */
.word-cloud-container {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-wrap: wrap;
  align-content: center;
  justify-content: center;
  gap: 12px;
  overflow: hidden;
  position: relative;
}

.cloud-tag {
  cursor: pointer;
  transition: all 0.3s;
  padding: 4px 10px;
  border-radius: 20px;
  background: rgba(255,255,255,0.05);
  white-space: nowrap;
}

.cloud-tag:hover {
  transform: scale(1.2);
  background: var(--gold-dim);
  color: #fff !important;
  z-index: 10;
  border: 1px solid var(--gold-primary);
}

/* Results Grid */
.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 48px;
  padding: 10px;
}

.result-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.result-card:hover {
  border-color: var(--gold-primary);
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
}

.card-status {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #888;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-published { background: var(--success-green); box-shadow: 0 0 5px var(--success-green); }
.status-pending { background: var(--warning-orange); }
.status-default { background: #909399; }

.title {
  color: #fff;
  font-size: 18px;
  margin: 0 0 12px 0;
  line-height: 1.4;
  padding-right: 60px; /* space for status */
}

.description {
  color: var(--text-muted);
  font-size: 14px;
  line-height: 1.6;
  flex: 1;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-top: 1px solid #222;
  padding-top: 16px;
}

.tags {
  display: flex;
  gap: 8px;
}

.meta-tag {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  background: #222;
  color: #aaa;
}

.field-tag { color: var(--accent-blue); background: rgba(64, 158, 255, 0.1); }
.maturity-tag { color: var(--warning-orange); background: rgba(230, 162, 60, 0.1); }

.price {
  color: var(--gold-primary);
  font-weight: bold;
}

.currency { font-size: 14px; margin-right: 2px; }
.amount { font-size: 20px; font-family: 'DIN', sans-serif; }

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  color: #fff;
  font-size: 20px;
  border-left: 4px solid var(--gold-primary);
  padding-left: 12px;
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.5s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
/* Dark Mode Adaptations for Element Plus Components */
:deep(.el-empty__description p) {
  color: var(--text-secondary);
}

:deep(.el-loading-mask) {
  background-color: rgba(0, 0, 0, 0.8);
}

.match-badge, .rec-badge {
  position: absolute;
  top: -10px;
  left: -10px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: bold;
  border-radius: 4px;
  z-index: 5;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

.match-badge {
  background: var(--gold-primary);
  color: #000;
}

.rec-badge {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
}

.exact-match-card {
  border: 1px solid var(--gold-glow);
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.1);
}
</style>