<template>
  <div class="publish-need-page">
    <div class="form-container">
      <div class="form-header">
        <h2>发布技术需求</h2>
        <p>请详细填写您的技术难题，平台将利用 AI 匹配最合适的专家团队</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="need-form">
        <!-- Section 1: Basic Info -->
        <div class="form-section">
          <h3><el-icon><InfoFilled /></el-icon> 基础信息</h3>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="需求标题" prop="title">
                <el-input v-model="form.title" placeholder="例如：寻求高性能锂电池正极材料解决方案" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属领域" prop="field">
                <el-select v-model="form.field" placeholder="请选择领域" size="large" style="width: 100%">
                  <el-option label="新能源" value="新能源" />
                  <el-option label="新材料" value="新材料" />
                  <el-option label="生物医药" value="生物医药" />
                  <el-option label="智能制造" value="智能制造" />
                  <el-option label="电子信息" value="电子信息" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- Section 2: Technical Specs -->
        <div class="form-section">
          <h3><el-icon><Tools /></el-icon> 技术指标</h3>
          <el-form-item label="需求描述" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="6" 
              placeholder="请详细描述技术痛点、现有方案的不足以及期望达到的具体指标（如：能量密度>300Wh/kg，循环寿命>1000次）。" 
            />
          </el-form-item>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="预算范围 (万元)" prop="budget">
                <el-input-number v-model="form.budget" :min="0" :step="10" size="large" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="截止日期" prop="deadline">
                <el-date-picker v-model="form.deadline" type="date" placeholder="选择日期" size="large" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- Section 3: Contact -->
        <div class="form-section">
          <h3><el-icon><User /></el-icon> 联系方式</h3>
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="联系人" prop="contactName">
                <el-input v-model="form.contactName" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="所属机构" prop="institution">
                <el-input v-model="form.institution" size="large" />
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
import { InfoFilled, Tools, User } from '@element-plus/icons-vue'
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
  budget: 0,
  deadline: '',
  contactName: '',
  phone: '',
  institution: '',
  ownerId: 1 // Default, will be updated
})

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (userStore.userRole !== 'ENTERPRISE') {
    ElMessage.error('只有企业用户可以发布需求')
    router.push('/profile')
    return
  }

  if (userStore.user && userStore.user.id) {
    form.ownerId = userStore.user.id
  }
})

const rules = reactive({
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  field: [{ required: true, message: '请选择领域', trigger: 'change' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  institution: [{ required: true, message: '请输入所属机构', trigger: 'blur' }],
  budget: [{ required: true, message: '请输入预算', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }]
})

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const payload = {
            ...form,
            type: 'NORMAL' // Default type
        }
        await axios.post('/api/demands', payload)
        ElMessage.success('需求发布成功！AI 正在为您匹配专家...')
        router.push('/profile')
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
.publish-need-page {
  min-height: 100vh;
  padding: 40px 20px;
  background-color: var(--bg-dark);
}

.form-container {
  max-width: 900px;
  margin: 0 auto;
  background-color: var(--bg-card);
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  border: 1px solid var(--border-color);
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.form-header p {
  color: var(--text-secondary);
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
}

.form-section h3 {
  color: var(--el-color-primary);
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