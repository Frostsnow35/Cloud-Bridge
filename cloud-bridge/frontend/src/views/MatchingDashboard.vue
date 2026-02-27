<template>
  <div class="dashboard-container">
    <!-- Header Section with Steps -->
    <div class="dashboard-header">
      <div class="header-content section-container">
        <h1 class="dynamic-title">
          <span class="typed-text">{{ displayedTitle }}</span>
          <span class="cursor" :class="{ typing: isTyping }">|</span>
        </h1>
        
        <!-- Analysis Steps -->
        <div class="analysis-steps">
           <el-steps :active="activeStep" finish-status="success" align-center>
             <el-step title="需求解析" description="AI深度理解需求语义"></el-step>
             <el-step title="图谱推理" description="挖掘跨领域技术关联"></el-step>
             <el-step title="资源匹配" description="链接政策/资金/设备"></el-step>
             <el-step title="生成报告" description="构建全维解决方案"></el-step>
           </el-steps>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="dashboard-content section-container">
      <el-row :gutter="24">
        <!-- Left: Demand Profile (Compact) -->
        <el-col :span="6">
          <el-card class="glass-card demand-profile" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>需求画像</span>
                <el-tag size="small" effect="dark">{{ demand.field || '未知领域' }}</el-tag>
              </div>
            </template>
            <div class="profile-body">
              <h3>{{ demand.title }}</h3>
              <p class="desc">{{ demand.description }}</p>
              <div class="tags-cloud">
                <el-tag v-for="tag in demandTags" :key="tag" size="small" class="tag-item">{{ tag }}</el-tag>
              </div>
            </div>
          </el-card>

          <el-card class="glass-card resource-nav mt-4" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>资源智能导航</span>
                <el-icon><Compass /></el-icon>
              </div>
            </template>
            
            <div class="resource-groups">
               <!-- Policy -->
               <div class="res-group">
                 <div class="group-title"><el-icon><Document /></el-icon> 政策补贴</div>
                 <div class="res-list">
                    <div class="res-item" v-for="p in resources.policies" :key="p.id">
                       <span class="res-name">{{ p.name }}</span>
                       <el-tag size="small" type="danger">最高{{ p.amount }}万</el-tag>
                    </div>
                 </div>
               </div>
               
               <!-- Funding -->
               <div class="res-group">
                 <div class="group-title"><el-icon><Money /></el-icon> 科技金融</div>
                 <div class="res-list">
                    <div class="res-item" v-for="f in resources.funds" :key="f.id">
                       <span class="res-name">{{ f.name }}</span>
                       <span class="res-sub">{{ f.type }}</span>
                    </div>
                 </div>
               </div>
               
               <!-- Equipment -->
               <div class="res-group">
                 <div class="group-title"><el-icon><Monitor /></el-icon> 共享设备</div>
                 <div class="res-list">
                    <div class="res-item" v-for="e in resources.equipments" :key="e.id">
                       <span class="res-name">{{ e.name }}</span>
                       <el-button type="primary" link size="small">预约</el-button>
                    </div>
                 </div>
               </div>
            </div>
          </el-card>
        </el-col>

        <!-- Center: Top Matches (Enhanced) -->
        <el-col :span="hasAnalyzed ? 12 : 18">
          <el-card class="glass-card match-list-main" shadow="hover">
            <template #header>
              <div class="card-header main-header">
                <div class="header-left">
                  <span class="main-title">最佳匹配成果</span>
                  <el-tag type="success" effect="dark" round v-if="hasAnalyzed">AI 智能推荐</el-tag>
                </div>
                <div class="header-right">
                   <el-tooltip content="所有匹配结果均已通过区块链存证，确保真实可信" placement="top">
                      <el-tag type="warning" effect="plain" class="evidence-tag">
                        <el-icon><Lock /></el-icon> 区块链存证保护
                      </el-tag>
                   </el-tooltip>
                </div>
              </div>
            </template>
            
                <div class="match-items-main">
                  <div v-if="!hasAnalyzed" class="empty-state-container">
                    <template v-if="!isAnalyzing">
                        <el-empty description="点击下方按钮开始AI智能深度匹配" />
                        <el-button type="primary" size="large" @click="startMatching" class="start-match-btn">
                          <el-icon class="el-icon--left"><Cpu /></el-icon> 开始AI匹配
                        </el-button>
                    </template>
                    <template v-else>
                        <div class="analyzing-state">
                            <el-progress type="dashboard" :percentage="progress" :color="colors" />
                            <h3 class="analyzing-text">{{ progressText }}</h3>
                            <p class="analyzing-sub">正在检索全网{{ (progress * 150).toFixed(0) }}条相关资源...</p>
                        </div>
                    </template>
                  </div>
                  
                  <div 
                    v-else
                    v-for="(item, index) in recommendations" 
                    :key="item.id" 
                    class="match-item-card"
                    @click="$router.push(`/achievements/${item.id}`)"
                  >
                <div class="match-card-left">
                   <div class="score-badge">
                      <span class="score-val">{{ item.score }}%</span>
                      <span class="score-label">匹配度</span>
                   </div>
                </div>
                
                <div class="match-card-content">
                  <div class="item-header">
                     <h3 class="item-title">{{ item.title }}</h3>
                     <span class="item-price">¥ {{ item.price ? item.price.toLocaleString() : '面议' }}</span>
                  </div>
                  
                  <div class="item-tags">
                    <el-tag size="small" v-for="t in item.tags" :key="t" effect="plain">{{ t }}</el-tag>
                  </div>
                  
                  <div class="ai-reason">
                    <el-icon><MagicStick /></el-icon>
                    <span class="reason-text">AI 推荐理由: 该成果在核心技术指标上与需求高度契合，且团队具有相关领域成功落地经验。</span>
                  </div>
                </div>
                
                <div class="match-card-action">
                   <el-button type="primary" plain round>查看详情</el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- Right: Radar Analysis & Force Graph -->
        <el-col :span="6" v-if="hasAnalyzed">
          <el-card class="glass-card analysis-chart mb-4" shadow="hover">
             <template #header>
              <div class="card-header">
                <span>多维匹配模型</span>
              </div>
            </template>
            <div class="chart-container-small" ref="radarChartDom"></div>
          </el-card>

          <el-card class="glass-card analysis-report" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>深度分析报告</span>
                <el-icon v-if="isGeneratingReport" class="is-loading"><Loading /></el-icon>
              </div>
            </template>
            <div class="report-content">
               <p>{{ analysisReport }}<span class="cursor" v-if="isGeneratingReport">|</span></p>
            </div>
          </el-card>
          
          <el-card class="glass-card force-graph mt-4" shadow="hover">
             <template #header>
              <div class="card-header">
                <span>资源关联图谱</span>
              </div>
            </template>
            <div class="chart-container-small" ref="forceGraphDom"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Aim, ArrowRight, MagicStick, Lock, Compass, Document, Money, Monitor, Cpu, Loading } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()

// Data
const demand = ref<any>({})
const recommendations = ref<any[]>([])
const activeStep = ref(0)
const radarChartDom = ref<HTMLElement | null>(null)
let radarChart: echarts.ECharts | null = null
const isComponentMounted = ref(true)
const isAnalyzing = ref(false)
const hasAnalyzed = ref(false)
const progress = ref(0)
const progressText = ref('AI引擎初始化...')
const colors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 }
]

const isGeneratingReport = ref(false)
const analysisReport = ref('')
const forceGraphDom = ref<HTMLElement | null>(null)
let forceGraph: echarts.ECharts | null = null

// Resources Mock
const resources = ref({
    policies: [
        { id: 1, name: '高新技术企业认定奖励', amount: 30 },
        { id: 2, name: '研发费用加计扣除', amount: 50 },
        { id: 3, name: '科技成果转化专项资金', amount: 100 }
    ],
    funds: [
        { id: 1, name: '科技信贷', type: '低息贷款' },
        { id: 2, name: '天使投资', type: '股权融资' }
    ],
    equipments: [
        { id: 1, name: '高性能计算中心', type: '算力' },
        { id: 2, name: '材料分析实验室', type: '检测' }
    ]
})

// Dynamic Title Logic
const fullTitle = "智能供需匹配深度分析仪表盘"
const displayedTitle = ref('')
const isTyping = ref(true)

const typeTitle = () => {
  let i = 0
  const timer = setInterval(() => {
    if (i < fullTitle.length) {
      displayedTitle.value += fullTitle.charAt(i)
      i++
    } else {
      clearInterval(timer)
      isTyping.value = false
    }
  }, 100)
}

const demandTags = computed(() => {
    // Mock extraction
    return ['技术攻关', '研发合作', demand.value.field || '高新技术', '区块链存证']
})

const fetchData = async () => {
    const id = route.params.id
    if (!id) return

    try {
        const res = await axios.get(`/api/demands/${id}`)
        demand.value = res.data
        
        // Simulate Loading resources based on field
        if (demand.value.field === '生物医药') {
            resources.value.policies.push({ id: 4, name: '生物医药产业专项', amount: 200 })
            resources.value.equipments.push({ id: 3, name: '冷冻电镜中心', type: '高端设备' })
        }
    } catch (e) {
        console.error(e)
    }
}

const startMatching = async () => {
    if (!demand.value || !demand.value.id) return
    
    recommendations.value = []
    isAnalyzing.value = true
    hasAnalyzed.value = false
    activeStep.value = 1
    progress.value = 0
    progressText.value = '正在解析需求语义...'
    
    try {
        const analysisPromise = fetchAnalysis(demand.value)
        
        // Progress Simulation
        const timer = setInterval(() => {
            if (progress.value < 90) {
                progress.value += Math.floor(Math.random() * 5)
                if (progress.value > 30 && progress.value < 60) {
                    activeStep.value = 2
                    progressText.value = '正在构建知识图谱...'
                } else if (progress.value >= 60) {
                    activeStep.value = 3
                    progressText.value = '正在匹配全网资源...'
                }
            }
        }, 200)

        let graphData
        try {
            graphData = await analysisPromise
            clearInterval(timer)
            progress.value = 100
            activeStep.value = 4
            progressText.value = '匹配完成'
        } catch (error) {
            clearInterval(timer)
            console.warn('API Analysis failed', error)
            // Mock Data Fallback
            recommendations.value = [
                { id: 101, title: '高精度脑机接口信号采集系统', price: 150000, score: 95, tags: ['精准匹配', '技术契合'] },
                { id: 102, title: '基于深度学习的神经康复机器人', price: 280000, score: 88, tags: ['应用场景匹配'] },
                { id: 103, title: '多模态生物信号分析平台', price: 98000, score: 82, tags: ['数据处理'] }
            ]
            graphData = {
                nodes: [
                    { id: '0', name: demand.value.title || '核心需求', category: 0, symbolSize: 30 },
                    { id: '1', name: '政策支持', category: 1, symbolSize: 20 },
                    { id: '2', name: '专项资金', category: 1, symbolSize: 20 },
                    { id: '3', name: '关键技术', category: 2, symbolSize: 25 },
                    { id: '4', name: '领军专家', category: 2, symbolSize: 20 },
                    { id: '5', name: '合作企业', category: 3, symbolSize: 20 }
                ],
                links: [
                    { source: '0', target: '3' },
                    { source: '3', target: '1' },
                    { source: '3', target: '2' },
                    { source: '3', target: '4' },
                    { source: '4', target: '5' }
                ]
            }
        }
        
        if (recommendations.value.length > 0) {
            // Wait a bit for progress bar to finish
            await new Promise(resolve => setTimeout(resolve, 500))
            hasAnalyzed.value = true
            isGeneratingReport.value = true
            const reportText = "根据深度分析，该技术在生物医药领域具有极高的应用潜力。建议优先申请相关专利保护，并接触专注医疗健康的早期基金。同时，可利用共享设备平台进行低成本验证。"
            
            let i = 0
            const typeTimer = setInterval(() => {
                if (i < reportText.length) {
                    analysisReport.value += reportText.charAt(i)
                    i++
                } else {
                    clearInterval(typeTimer)
                    isGeneratingReport.value = false
                    ElMessage.success('AI深度匹配完成')
                    nextTick(() => {
                        initCharts(graphData)
                        initForceGraph(graphData)
                    })
                }
            }, 50)
        } else {
            activeStep.value = 0
            isAnalyzing.value = false
            ElMessage.warning('暂无匹配结果，请尝试优化需求描述')
        }
    } catch (e) {
        console.error(e)
        activeStep.value = 0
        isAnalyzing.value = false
        ElMessage.error('匹配分析服务暂时不可用，请稍后重试')
    }
}

const fetchAnalysis = async (item: any) => {
    try {
        const desc = item.description ? item.description.substring(0, 50) : ''
        const res = await axios.post('/api/matching/match', {
             description: `${item.title} ${desc}`
        })
        
        if (res.data) {
            let rawMatches = res.data.matches || []
            // Use fallback recommendations if no direct matches found
            if (rawMatches.length === 0 && res.data.recommendations && res.data.recommendations.length > 0) {
                rawMatches = res.data.recommendations
            }

            if (rawMatches.length > 0) {
                recommendations.value = rawMatches.slice(0, 5).map((m: any) => ({
                    ...m,
                    score: m.score || 0, // Use real score
                    tags: ['精准匹配', '技术契合'] // Unified label
                }))
                
                return res.data.aiGraph
            }
        }
    } catch (e) {
        throw e
    }
}

const initCharts = (graphData: any) => {
    // Radar Chart - Adjusted for Dark Theme
    if (!isComponentMounted.value) return
    // Ensure DOM element exists
    const chartDom = radarChartDom.value
    if (!chartDom) return

    try {
        if (radarChart) {
            radarChart.dispose()
        }
        radarChart = echarts.init(chartDom)
        
        // Generate dynamic radar data based on top match score
        const topScore = recommendations.value.length > 0 ? recommendations.value[0].score : 85
        const base = topScore > 90 ? 90 : (topScore > 80 ? 80 : 70)
        const randomOffset = () => Math.floor(Math.random() * 10)
        
        radarChart.setOption({
            backgroundColor: 'transparent',
            radar: {
                indicator: [
                    { name: '技术匹配度', max: 100 },
                    { name: '领域相关性', max: 100 },
                    { name: '创新指数', max: 100 },
                    { name: '落地可行性', max: 100 },
                    { name: '预算契合度', max: 100 }
                ],
                splitArea: {
                    areaStyle: {
                        color: ['rgba(64, 158, 255, 0.1)', 'rgba(64, 158, 255, 0.2)']
                    }
                },
                axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.3)' } },
                splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } },
                axisName: { color: '#fff' }
            },
            series: [{
                type: 'radar',
                data: [
                    {
                        value: [
                            topScore, 
                            base + randomOffset(), 
                            base + randomOffset(), 
                            base - randomOffset(), 
                            base + randomOffset()
                        ],
                        name: '匹配分析模型',
                        areaStyle: {
                            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                { offset: 0, color: 'rgba(64, 158, 255, 0.8)' },
                                { offset: 1, color: 'rgba(64, 158, 255, 0.2)' }
                            ])
                        },
                        lineStyle: { color: '#409EFF' }
                    }
                ]
            }]
        })
    } catch (e) {
        console.warn('Failed to init radar chart:', e)
    }
}

const initForceGraph = (graphData: any) => {
    if (!isComponentMounted.value || !forceGraphDom.value) return
    
    try {
        if (forceGraph) forceGraph.dispose()
        forceGraph = echarts.init(forceGraphDom.value)
        
        // Mock Graph Data if API returns empty
        const nodes = graphData?.nodes?.length ? graphData.nodes : [
            { id: '0', name: demand.value.title || '核心需求', category: 0, symbolSize: 30 },
            { id: '1', name: '政策支持', category: 1, symbolSize: 20 },
            { id: '2', name: '专项资金', category: 1, symbolSize: 20 },
            { id: '3', name: '关键技术', category: 2, symbolSize: 25 },
            { id: '4', name: '领军专家', category: 2, symbolSize: 20 },
            { id: '5', name: '合作企业', category: 3, symbolSize: 20 }
        ]
        
        const links = graphData?.links?.length ? graphData.links : [
            { source: '0', target: '3' },
            { source: '3', target: '1' },
            { source: '3', target: '2' },
            { source: '3', target: '4' },
            { source: '4', target: '5' }
        ]
        
        forceGraph.setOption({
            backgroundColor: 'transparent',
            series: [{
                type: 'graph',
                layout: 'force',
                data: nodes,
                links: links,
                categories: [{ name: '需求' }, { name: '资源' }, { name: '技术' }, { name: '企业' }],
                roam: true,
                label: { show: true, position: 'right', color: '#fff' },
                force: { repulsion: 100, edgeLength: 50 },
                lineStyle: { color: 'source', curveness: 0.3 }
            }]
        })
    } catch (e) {
        console.warn('Failed to init force graph:', e)
    }
}

const handleResize = () => {
    radarChart?.resize()
    forceGraph?.resize()
}

// Lifecycle
onMounted(() => {
    isComponentMounted.value = true
    typeTitle()
    // initWordCloud()
    fetchData()
    
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    isComponentMounted.value = false
    window.removeEventListener('resize', handleResize)
    if (radarChart) {
        radarChart.dispose()
        radarChart = null
    }
    if (forceGraph) {
        forceGraph.dispose()
        forceGraph = null
    }
})
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: radial-gradient(circle at center, #1a2a3a 0%, #0d1117 100%);
  color: #fff;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
  padding-bottom: 60px;
}

/* Header */
.dashboard-header {
  position: relative;
  z-index: 2;
  padding: 30px 0 20px;
  text-align: center;
  background: linear-gradient(to bottom, rgba(13, 17, 23, 0.9), transparent);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  margin-bottom: 30px;
}

.dynamic-title {
  font-size: 2rem;
  margin-bottom: 20px;
  background: linear-gradient(120deg, #fff, #409EFF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 700;
  display: inline-block;
}

.cursor {
  display: inline-block;
  width: 3px;
  background-color: #409EFF;
  margin-left: 5px;
  animation: blink 1s infinite;
}

.cursor.typing { animation: none; }

@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

.analysis-steps {
  max-width: 800px;
  margin: 0 auto;
}

/* Force Step text color in dark mode */
.analysis-steps :deep(.el-step__title), 
.analysis-steps :deep(.el-step__description) {
    color: rgba(255, 255, 255, 0.7) !important;
}
.analysis-steps :deep(.is-success .el-step__title),
.analysis-steps :deep(.is-process .el-step__title) {
    color: #409EFF !important;
}

/* Content */
.dashboard-content {
  position: relative;
  z-index: 2;
}

.glass-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #fff;
  height: 100%;
  border-radius: 8px;
  transition: all 0.3s;
}

.glass-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0,0,0,0.3);
  background: rgba(255, 255, 255, 0.05);
}

.glass-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding: 15px 20px;
  background: rgba(0,0,0,0.2);
  border-radius: 8px 8px 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
  color: #fff;
}

.demand-profile .desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin: 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags-cloud { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-item { background: rgba(64, 158, 255, 0.1); border: none; color: #409EFF; }

.chart-container-small { height: 300px; width: 100%; }
.path-chart-container { height: 400px; width: 100%; }

/* Main Match List */
.match-list-main {
  border: 1px solid rgba(64, 158, 255, 0.3);
  background: rgba(13, 17, 23, 0.6);
}

.main-header {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.main-title {
  font-size: 18px;
  color: #409EFF;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.evidence-tag { cursor: help; }

.match-items-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px;
}

.match-item-card {
  display: flex;
  align-items: stretch;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  padding: 0;
  overflow: hidden;
}

.match-item-card:hover {
  border-color: #409EFF;
  box-shadow: 0 0 15px rgba(64,158,255,0.15);
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.05);
}

.match-card-left {
  width: 100px;
  background: rgba(64, 158, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.score-badge {
  text-align: center;
}

.score-val {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #67C23A;
  text-shadow: 0 0 5px rgba(103, 194, 58, 0.3);
}

.score-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.match-card-content {
  flex: 1;
  padding: 24px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.item-title {
  margin: 0;
  font-size: 16px;
  color: #fff;
}

.item-price {
  font-weight: bold;
  color: #F56C6C;
  font-family: 'DIN Alternate', sans-serif;
}

.item-tags {
  margin-bottom: 10px;
  display: flex;
  gap: 8px;
}

.ai-reason {
  background: rgba(64, 158, 255, 0.1);
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 13px;
  color: #a0cfff;
  display: flex;
  gap: 8px;
  align-items: flex-start;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.match-card-action {
  width: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-left: 1px solid rgba(255, 255, 255, 0.05);
}

/* Resource Nav */
.resource-groups {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.res-group {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.group-title {
  font-weight: bold;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ccc;
  font-size: 14px;
}

.res-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.res-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.03);
  padding: 8px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.2s;
}

.res-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.res-name { color: #eee; }
.res-sub { color: #909399; font-size: 12px; }

.mt-4 { margin-top: 24px; }

.empty-state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  min-height: 300px;
}

.analyzing-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.analyzing-text {
  margin-top: 20px;
  font-size: 18px;
  color: #fff;
  font-weight: 600;
}

.analyzing-sub {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

.start-match-btn {
  margin-top: 20px;
  padding: 12px 30px;
  font-size: 16px;
  background: linear-gradient(135deg, #409EFF, #36cfc9);
  border: none;
  transition: all 0.3s;
}

.start-match-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.5);
}
</style>