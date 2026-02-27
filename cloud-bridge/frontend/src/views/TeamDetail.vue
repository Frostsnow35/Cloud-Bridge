<template>
  <div class="team-detail">
    <!-- Header Section -->
    <div class="team-header">
      <div class="section-container header-inner">
        <div class="avatar-section">
          <el-avatar :size="100" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
        </div>
        <div class="info-section">
          <h1 class="team-name">{{ team.name }}</h1>
          <p class="team-institution"><el-icon><School /></el-icon> {{ team.institution }}</p>
          <div class="team-stats">
            <div class="stat-item">
              <span class="num">{{ team.achievementCount }}</span>
              <span class="label">发布成果</span>
            </div>
            <div class="stat-item">
              <span class="num">{{ team.rating }}</span>
              <span class="label">综合评分</span>
            </div>
            <div class="stat-item">
              <span class="num">{{ team.responseRate }}%</span>
              <span class="label">响应率</span>
            </div>
          </div>
        </div>
        <div class="action-section">
          <el-button type="primary" size="large">关注团队</el-button>
          <el-button size="large">发送私信</el-button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="section-container content-wrapper">
      <el-row :gutter="30">
        <el-col :span="16">
          <div class="content-card">
            <h3 class="section-title">团队介绍</h3>
            <p class="description">{{ team.description }}</p>
          </div>

          <div class="content-card">
            <h3 class="section-title">研究方向</h3>
            <div class="research-tags">
              <el-tag v-for="tag in team.researchAreas" :key="tag" size="large" effect="plain">{{ tag }}</el-tag>
            </div>
          </div>

          <div class="content-card">
            <h3 class="section-title">已发布成果</h3>
            <div class="achievement-list">
              <div v-for="item in team.achievements" :key="item.id" class="achievement-item" @click="$router.push(`/achievements/${item.id}`)">
                <div class="item-main">
                  <h4>{{ item.title }}</h4>
                  <div class="item-meta">
                    <el-tag size="small">{{ item.field }}</el-tag>
                    <span class="date">{{ item.date }}</span>
                  </div>
                </div>
                <div class="item-price">¥ {{ item.price }}</div>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="8">
          <div class="content-card">
            <h3 class="section-title">联系方式</h3>
            <div class="contact-info">
              <p><el-icon><Location /></el-icon> {{ team.location }}</p>
              <p><el-icon><Message /></el-icon> {{ team.email }}</p>
              <p><el-icon><Phone /></el-icon> {{ team.phone }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { School, Location, Message, Phone } from '@element-plus/icons-vue'

const route = useRoute()
const team = ref<any>({})

onMounted(() => {
  // Mock Data
  team.value = {
    id: route.params.id,
    name: '张教授团队',
    institution: '某知名高校材料学院',
    description: '张教授团队长期致力于高性能复合材料的基础研究与应用开发，拥有多项国家级发明专利。团队核心成员包括教授2名、副教授3名及博士研究生10余名，在碳纤维、纳米材料等领域具有深厚的科研积累。',
    achievementCount: 12,
    rating: 4.9,
    responseRate: 98,
    researchAreas: ['新型复合材料', '纳米技术', '高分子化学', '智能制造'],
    location: '北京市海淀区学院路30号',
    email: 'contact@lab-materials.edu.cn',
    phone: '010-88888888',
    achievements: [
      { id: 1, title: '高性能碳纤维复合材料制备技术', field: '新材料', date: '2023-10-15', price: '2,000,000' },
      { id: 2, title: '纳米银导电油墨配方', field: '电子材料', date: '2023-09-20', price: '500,000' },
      { id: 3, title: '生物可降解塑料生产工艺', field: '环保科技', date: '2023-08-05', price: '800,000' }
    ]
  }
})
</script>

<style scoped>
.team-detail {
  min-height: 100vh;
  background-color: var(--bg-dark);
  padding-bottom: 60px;
}
.team-header {
  background: var(--bg-card);
  padding: 40px 0;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 30px;
}
.header-inner {
  display: flex;
  align-items: center;
  gap: 30px;
}
.info-section { flex: 1; }
.team-name { font-size: 28px; margin: 0 0 10px 0; color: var(--text-primary); }
.team-institution { color: var(--text-secondary); display: flex; align-items: center; gap: 8px; font-size: 16px; margin-bottom: 20px; }
.team-stats { display: flex; gap: 40px; }
.stat-item { display: flex; flex-direction: column; }
.stat-item .num { font-size: 24px; font-weight: bold; color: var(--text-primary); }
.stat-item .label { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }

.content-wrapper { }
.content-card {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 30px;
  border: 1px solid var(--border-color);
  margin-bottom: 20px;
}
.section-title {
  font-size: 18px;
  color: var(--text-primary);
  margin-bottom: 20px;
  border-left: 4px solid var(--el-color-primary);
  padding-left: 12px;
  line-height: 1;
}
.description { line-height: 1.8; color: var(--text-secondary); }
.research-tags .el-tag { margin-right: 12px; margin-bottom: 12px; }

.achievement-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}
.achievement-item:hover { background: rgba(255,255,255,0.02); padding-left: 10px; }
.achievement-item:last-child { border-bottom: none; }
.item-main h4 { margin: 0 0 8px 0; font-size: 16px; color: var(--text-primary); }
.item-meta { display: flex; gap: 10px; align-items: center; }
.item-meta .date { font-size: 12px; color: var(--text-secondary); }
.item-price { font-weight: bold; color: var(--el-color-warning); font-size: 16px; }

.contact-info p { display: flex; align-items: center; gap: 10px; color: var(--text-secondary); margin-bottom: 15px; }
</style>