<template>
  <div class="publish-achievement-page">
    <div class="form-container">
      <div class="form-header">
        <h2>发布科研成果</h2>
        <p>展示您的顶尖科研成果，连接产业需求，加速科技价值转化</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="achievement-form">
        <!-- Section 1: Basic Info -->
        <div class="form-section">
          <h3><el-icon><InfoFilled /></el-icon> 基础信息</h3>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="成果名称" prop="title">
                <el-input v-model="form.title" placeholder="例如：高灵敏度柔性电子皮肤传感技术" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属领域" prop="field">
                <el-select v-model="form.field" placeholder="请选择领域" size="large" style="width: 100%">
                  <el-option label="新材料" value="新材料" />
                  <el-option label="人工智能" value="人工智能" />
                  <el-option label="新能源" value="新能源" />
                  <el-option label="智能制造" value="智能制造" />
                  <el-option label="生物医药" value="生物医药" />
                  <el-option label="电子信息" value="电子信息" />
                  <el-option label="环保科技" value="环保科技" />
                  <el-option label="大数据" value="大数据" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- Section 2: Technical Specs -->
        <div class="form-section">
          <h3><el-icon><Tools /></el-icon> 技术详情</h3>
          <el-form-item label="成果简介" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="6" 
              placeholder="请详细描述成果的技术原理、核心优势、应用场景及当前研发状态。" 
            />
          </el-form-item>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="成熟度阶段" prop="maturity">
                <el-select v-model="form.maturity" placeholder="请选择成熟度" size="large" style="width: 100%">
                  <el-option label="实验室研发" value="实验室研发" />
                  <el-option label="中试阶段" value="中试阶段" />
                  <el-option label="产业化阶段" value="产业化阶段" />
                  <el-option label="成熟应用" value="成熟应用" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="转让价格 (元)" prop="price">
                <el-input-number v-model="form.price" :min="0" :step="10000" size="large" style="width: 100%" placeholder="0为面议" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="专利信息" prop="patentInfo">
            <el-input 
              v-model="form.patentInfo" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入专利号或相关知识产权描述" 
            />
          </el-form-item>

          <el-form-item label="应用案例" prop="applicationCases">
            <el-input 
              v-model="form.applicationCases" 
              type="textarea" 
              :rows="4" 
              placeholder="请描述该成果的实际应用案例或测试验证情况" 
            />
          </el-form-item>

          <el-form-item label="资源链接" prop="resourceLinks">
            <el-input 
              v-model="form.resourceLinks" 
              type="textarea" 
              :rows="2" 
              placeholder="相关论文、视频演示或详细文档链接（选填）" 
            />
          </el-form-item>
        </div>

        <!-- Section 3: Owner Info -->
        <div class="form-section">
          <h3><el-icon><School /></el-icon> 所属单位</h3>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="所属机构/高校" prop="institution">
                <el-input v-model="form.institution" placeholder="例如：某知名高校材料实验室" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系人姓名" prop="contactName">
                <el-input v-model="form.contactName" placeholder="例如：张教授" size="large" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" placeholder="例如：13800000000" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系人ID (临时)" prop="ownerId">
                 <el-input-number v-model="form.ownerId" :min="1" size="large" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- Actions -->
        <div class="form-actions">
          <el-button size="large" @click="$router.back()">取消</el-button>
          <el-button type="primary" size="large" @click="submitForm" :loading="loading">确认发布</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled, Tools, School } from '@element-plus/icons-vue'
import axios from 'axios'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  title: '',
  field: '',
  description: '',
  maturity: '',
  price: 0,
  patentInfo: '',
  applicationCases: '',
  resourceLinks: '',
  institution: '',
  contactName: '',
  phone: '',
  ownerId: 1, // Default for now
  status: 'PUBLISHED'
})

const rules = reactive({
  title: [{ required: true, message: '请输入成果名称', trigger: 'blur' }],
  field: [{ required: true, message: '请选择所属领域', trigger: 'change' }],
  description: [{ required: true, message: '请输入成果简介', trigger: 'blur' }],
  maturity: [{ required: true, message: '请选择成熟度', trigger: 'change' }],
  patentInfo: [{ required: true, message: '请输入专利信息', trigger: 'blur' }],
  applicationCases: [{ required: true, message: '请输入应用案例', trigger: 'blur' }],
  institution: [{ required: true, message: '请输入所属机构', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
})

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (userStore.userRole !== 'EXPERT') {
    ElMessage.error('只有专家用户可以发布成果')
    router.push('/profile')
    return
  }

  if (userStore.user && userStore.user.id) {
    form.ownerId = userStore.user.id
  }
})

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const response = await axios.post('/api/achievements', form)
        if (response.data) {
             ElMessage.success('成果发布成功！')
             router.push('/profile')
        }
      } catch (error) {
          console.error(error)
          ElMessage.error('发布失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.publish-achievement-page {
  min-height: 100vh;
  padding: 40px 20px;
  background-color: var(--bg-dark); /* Ensure this var exists or use fixed color */
  background-color: #141414;
}

.form-container {
  max-width: 900px;
  margin: 0 auto;
  background-color: #1f1f1f;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  border: 1px solid #333;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  color: #fff;
  margin-bottom: 10px;
}

.form-header p {
  color: #999;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #333;
}

.form-section h3 {
  color: #409EFF;
  font-size: 18px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
}
</style>