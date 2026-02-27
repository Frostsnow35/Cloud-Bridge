<template>
  <div class="contact-expert-page">
    <div class="container">
      <div class="page-header">
        <el-button @click="$router.back()" link>
          <el-icon><ArrowLeft /></el-icon> 返回详情
        </el-button>
        <h2>{{ pageTitle }}</h2>
      </div>

      <div class="contact-card">
        <div class="expert-info" v-if="targetEntity">
          <h3>{{ subTitle }}</h3>
          <div class="project-summary">
            <span class="label">{{ entityLabel }}</span>
            <span class="value">{{ targetEntity.title }}</span>
          </div>
          <div class="project-summary">
            <span class="label">所属机构：</span>
            <span class="value">{{ targetEntity.institution || '暂无机构信息' }}</span>
          </div>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="contact-form">
          <el-form-item label="合作意向类型" prop="cooperationType">
            <el-select v-model="form.cooperationType" placeholder="请选择您的合作意向" size="large" style="width: 100%">
              <el-option label="技术转让" value="技术转让" />
              <el-option label="联合研发" value="联合研发" />
              <el-option label="技术许可" value="技术许可" />
              <el-option label="专家咨询" value="专家咨询" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>

          <el-form-item label="预估预算范围 (万元)" prop="budget">
            <el-input v-model="form.budget" placeholder="请输入预估预算，例如：50-100" size="large">
              <template #append>万元</template>
            </el-input>
          </el-form-item>

          <el-form-item label="需求详细描述" prop="content">
            <el-input 
              v-model="form.content" 
              type="textarea" 
              :rows="6" 
              placeholder="请详细描述您的合作需求、应用场景及期望达成的目标。" 
            />
          </el-form-item>

          <el-form-item label="附件上传 (需求文档/公司介绍)" prop="attachmentUrl">
             <el-upload
                class="upload-demo"
                drag
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                :limit="1"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  拖拽文件到此处或 <em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 PDF/Word 文档，大小不超过 10MB
                  </div>
                </template>
              </el-upload>
          </el-form-item>

          <div class="form-actions">
            <el-button size="large" @click="$router.back()">取消</el-button>
            <el-button type="primary" size="large" @click="submitForm" :loading="loading">发送合作申请</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, UploadFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const targetEntity = ref<any>(null)

const isDemand = computed(() => route.path.includes('/needs'))
const pageTitle = computed(() => isDemand.value ? '联系发布方' : '联系专家')
const subTitle = computed(() => isDemand.value ? '您正在联系需求发布方' : '您正在联系项目负责人')
const entityLabel = computed(() => isDemand.value ? '关联需求：' : '意向成果：')

const form = reactive({
  cooperationType: '',
  budget: '',
  content: '',
  attachmentUrl: '', // Simulated
  senderId: 0,
  receiverId: 0,
  relatedEntityId: 0,
  relatedEntityType: 'ACHIEVEMENT'
})

const rules = reactive({
  cooperationType: [{ required: true, message: '请选择合作意向类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入需求详细描述', trigger: 'blur' }]
})

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  const id = route.params.id
  if (id) {
    try {
      let res;
      if (isDemand.value) {
        res = await axios.get(`/api/demands/${id}`)
        form.relatedEntityType = 'DEMAND'
      } else {
        res = await axios.get(`/api/achievements/${id}`)
        form.relatedEntityType = 'ACHIEVEMENT'
      }
      targetEntity.value = res.data
      
      // Initialize form IDs
      if (userStore.user) {
        form.senderId = userStore.user.id
      } else {
        ElMessage.warning('用户信息异常，请重新登录')
        router.push('/login')
        return
      }
      form.receiverId = targetEntity.value.ownerId
      form.relatedEntityId = targetEntity.value.id
    } catch (error) {
      console.error('Failed to load entity:', error)
      ElMessage.error('加载信息失败')
    }
  }
})

const handleFileChange = async (file: any) => {
    if (!file.raw) return;
    
    const formData = new FormData();
    formData.append("file", file.raw);
    
    try {
        ElMessage.info("正在上传...")
        const response = await axios.post("/api/upload", formData, {
            headers: {
                "Content-Type": "multipart/form-data"
            }
        });
        
        form.attachmentUrl = response.data.fileDownloadUri;
        ElMessage.success("文件上传成功！");
    } catch (error) {
        console.error("Upload failed:", error);
        ElMessage.error("上传失败，请重试");
    }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        await axios.post('/api/messages', form)
        ElMessage.success('合作申请已发送！专家将尽快与您联系。')
        if (isDemand.value) {
          router.push(`/needs/${form.relatedEntityId}`)
        } else {
          router.push(`/achievements/${form.relatedEntityId}`)
        }
      } catch (error) {
        console.error('Failed to send message:', error)
        ElMessage.error('发送失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.contact-expert-page {
  min-height: 100vh;
  padding: 40px 20px;
  background-color: var(--bg-primary);
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: var(--text-primary);
}

.contact-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 40px;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.expert-info {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-color);
}

.expert-info h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: var(--text-primary);
}

.project-summary {
  margin-bottom: 8px;
  font-size: 14px;
}

.project-summary .label {
  color: var(--text-secondary);
}

.project-summary .value {
  color: var(--text-primary);
  font-weight: 500;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 40px;
}
</style>
