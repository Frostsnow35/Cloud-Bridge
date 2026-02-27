<template>
  <div class="resource-detail-page" v-loading="loading">
    <div class="content-wrapper">
      <!-- Back Button -->
      <div class="back-nav">
        <el-button link @click="$router.go(-1)">
          <el-icon><ArrowLeft /></el-icon> 返回上一页
        </el-button>
      </div>

      <div class="detail-card" v-if="item">
        <div class="card-header">
          <div class="header-main">
            <h1 class="title">{{ item.title || item.name }}</h1>
            <div class="meta-tags">
               <el-tag v-if="item.policyType" type="warning">{{ item.policyType }}</el-tag>
               <el-tag v-if="item.department" effect="plain">{{ item.department }}</el-tag>
               <el-tag v-if="item.fundType" type="danger">{{ item.fundType }}</el-tag>
               <el-tag v-if="item.category" type="info">{{ item.category }}</el-tag>
            </div>
          </div>
          <div class="header-side" v-if="item.publishDate || item.date">
             <span class="date">{{ item.publishDate || item.date }}</span>
          </div>
        </div>

        <el-divider />

        <div class="card-body">
          <!-- Common Description -->
          <div class="section">
            <h3>详情描述</h3>
            <p class="desc-text">{{ item.content || item.description || item.abstractText || '暂无详细描述' }}</p>
          </div>

          <!-- Specific Fields based on Category -->
          
          <!-- Expert Fields -->
          <div v-if="category === 'experts'" class="detail-section">
            <div class="info-row">
              <span class="label">职称:</span>
              <span class="value">{{ item.title }}</span>
            </div>
            <div class="info-row">
              <span class="label">所属单位:</span>
              <span class="value">{{ item.affiliation }}</span>
            </div>
             <div class="info-row">
              <span class="label">研究领域:</span>
              <span class="value">
                 <el-tag v-for="field in item.field" :key="field" size="small" class="mr-2">{{ field }}</el-tag>
              </span>
            </div>
             <div class="section mt-4">
               <h3>主要成就</h3>
               <p>{{ item.achievements }}</p>
             </div>
          </div>

          <!-- Fund Fields -->
          <div v-if="category === 'funds'" class="detail-section">
             <div class="info-row">
              <span class="label">资金规模:</span>
              <span class="value highlight">{{ item.amountRange }}</span>
            </div>
             <div class="info-row">
              <span class="label">提供方:</span>
              <span class="value">{{ item.provider }}</span>
            </div>
             <div class="info-row">
              <span class="label">利率/占比:</span>
              <span class="value">{{ item.interestRate }}</span>
            </div>
             <div class="info-row">
              <span class="label">关注行业:</span>
              <span class="value">
                 <el-tag v-for="ind in item.industryFocus" :key="ind" size="small" effect="dark" class="mr-2">{{ ind }}</el-tag>
              </span>
            </div>
          </div>

          <!-- Equipment Fields -->
          <div v-if="category === 'equipments'" class="detail-section">
             <div class="info-row">
              <span class="label">当前状态:</span>
              <el-tag :type="item.availability === 'Available' ? 'success' : 'info'">
                  {{ item.availability === 'Available' ? '可预约' : '维护中' }}
              </el-tag>
            </div>
             <div class="info-row">
              <span class="label">所在设施:</span>
              <span class="value">{{ item.facilityName }}</span>
            </div>
             <div class="info-row">
              <span class="label">所属单位:</span>
              <span class="value">{{ item.owner }}</span>
            </div>
             <div class="info-row">
              <span class="label">规格参数:</span>
              <span class="value">{{ item.specs }}</span>
            </div>
          </div>

          <!-- Patent Fields -->
           <div v-if="category === 'patents'" class="detail-section">
             <div class="info-row">
              <span class="label">专利号:</span>
              <span class="value">{{ item.patentNumber }}</span>
            </div>
             <div class="info-row">
              <span class="label">专利权人:</span>
              <span class="value">{{ item.assignee }}</span>
            </div>
             <div class="info-row">
              <span class="label">申请日期:</span>
              <span class="value">{{ item.filingDate }}</span>
            </div>
             <div class="info-row">
              <span class="label">公开日期:</span>
              <span class="value">{{ item.publicationDate }}</span>
            </div>
             <div class="info-row">
              <span class="label">当前状态:</span>
              <el-tag type="success">{{ item.status }}</el-tag>
            </div>
          </div>
          <!-- Enterprise Fields -->
          <div v-if="category === 'enterprises'" class="detail-section">
             <div class="info-row">
              <span class="label">行业领域:</span>
              <span class="value">{{ item.industry }}</span>
            </div>
             <div class="info-row">
              <span class="label">所在地区:</span>
              <span class="value">{{ item.location }}</span>
            </div>
             <div class="info-row">
              <span class="label">企业规模:</span>
              <span class="value">{{ item.scale }}</span>
            </div>
          </div>

          <!-- Public Platform Fields -->
          <div v-if="category === 'public_platforms'" class="detail-section">
             <div class="info-row">
              <span class="label">数据提供方:</span>
              <span class="value">{{ item.provider }}</span>
            </div>
             <div class="info-row">
              <span class="label">数据格式:</span>
              <span class="value">
                <el-tag size="small" type="primary">{{ item.format || 'API' }}</el-tag>
              </span>
            </div>
             <div class="info-row">
              <span class="label">更新频率:</span>
              <span class="value">{{ item.updateFrequency || '不定时更新' }}</span>
            </div>
             <div class="info-row">
              <span class="label">领域:</span>
              <span class="value">{{ item.domain }}</span>
            </div>
          </div>
          
        </div>

        <el-divider />

        <div class="card-footer">
           <el-button type="primary" size="large" @click="handleAction">
             {{ actionButtonText }}
           </el-button>
           <el-button size="large" @click="handleShare">分享</el-button>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="未找到相关资源" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const category = computed(() => route.params.category as string)
const id = computed(() => route.params.id as string)

const item = ref<any>(null)
const loading = ref(false)

const fetchData = async () => {
  if (!id.value) return
  loading.value = true
  try {
    const res = await axios.get(`/api/libraries/${category.value}/${id.value}`)
    // Expecting JSON string from backend based on controller
    if (typeof res.data === 'string') {
        try {
            item.value = JSON.parse(res.data)
        } catch(e) {
            // If backend returned object directly
             item.value = res.data
        }
    } else {
        item.value = res.data
    }
  } catch (e) {
    console.error('Fetch detail failed:', e)
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const actionButtonText = computed(() => {
    switch(category.value) {
        case 'experts': return '联系专家'
        case 'equipments': return '预约设备'
        case 'funds': return '申请资金'
        case 'patents': return '联系专利权人'
        case 'enterprises': return '查看企业主页'
        case 'public_platforms': return '申请数据访问'
        default: return '咨询详情'
    }
})

const handleAction = () => {
    ElMessage.success('请求已提交，请留意消息通知')
}

const handleShare = () => {
    navigator.clipboard.writeText(window.location.href)
    ElMessage.success('链接已复制到剪贴板')
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.resource-detail-page {
  min-height: 100vh;
  background-color: #121212;
  padding: 40px 0;
  color: #fff;
}

.content-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-nav {
  margin-bottom: 20px;
}

.detail-card {
  background: #1e1e1e;
  border: 1px solid #333;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.title {
  font-size: 28px;
  color: #fff;
  margin: 0 0 16px 0;
  font-weight: 700;
}

.meta-tags {
  display: flex;
  gap: 10px;
}

.date {
  color: #888;
  font-size: 14px;
}

.card-body {
  padding: 20px 0;
}

.section {
  margin-bottom: 30px;
}

.section h3 {
  font-size: 18px;
  color: #FFD700;
  margin-bottom: 12px;
  border-left: 3px solid #FFD700;
  padding-left: 10px;
}

.desc-text {
  line-height: 1.8;
  color: #ccc;
  font-size: 16px;
  white-space: pre-wrap;
}

.detail-section {
  background: #252525;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
  align-items: center;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  width: 100px;
  color: #888;
  font-size: 14px;
  flex-shrink: 0;
}

.value {
  color: #fff;
  font-size: 15px;
}

.highlight {
  color: #ff4d4f;
  font-weight: bold;
  font-size: 18px;
}

.mr-2 {
    margin-right: 8px;
}

.mt-4 {
    margin-top: 16px;
}

.card-footer {
  display: flex;
  gap: 20px;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
