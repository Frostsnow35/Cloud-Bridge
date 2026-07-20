<template>
  <div class="admin-dashboard">
    <div class="container">
      <div class="dashboard-header">
        <h2>管理后台</h2>
        <el-tag type="danger" effect="dark">管理员</el-tag>
      </div>

      <div class="stats-cards">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>总用户数</span>
                </div>
              </template>
              <div class="card-value">{{ stats.totalUsers }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>总需求数</span>
                </div>
              </template>
              <div class="card-value">{{ stats.totalDemands }}</div>
              <div class="card-footer">待审核: {{ stats.pendingDemands }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>总成果数</span>
                </div>
              </template>
              <div class="card-value">{{ stats.totalAchievements }}</div>
              <div class="card-footer">待审核: {{ stats.pendingAchievements }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>待办事项</span>
                </div>
              </template>
              <div class="card-value action-needed">{{ stats.pendingDemands + stats.pendingAchievements }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <el-tabs v-model="activeTab" class="dashboard-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="待审核需求" name="demands">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" :icon="Refresh" circle @click="fetchDemands" :loading="loading.demands" />
            </div>
            
            <el-table :data="pendingDemands" v-loading="loading.demands" style="width: 100%" empty-text="暂无待审核需求">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="需求标题" min-width="200" show-overflow-tooltip />
              <el-table-column prop="ownerId" label="发布者ID" width="100" />
              <el-table-column prop="field" label="领域" width="120" />
              <el-table-column prop="createdAt" label="提交时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="success" @click="auditDemand(scope.row.id, 'PUBLISHED')">通过</el-button>
                  <el-button size="small" type="danger" @click="auditDemand(scope.row.id, 'REJECTED')">驳回</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="待审核成果" name="achievements">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" :icon="Refresh" circle @click="fetchAchievements" :loading="loading.achievements" />
            </div>

            <el-table :data="pendingAchievements" v-loading="loading.achievements" style="width: 100%" empty-text="暂无待审核成果">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="成果名称" min-width="200" show-overflow-tooltip />
              <el-table-column prop="ownerId" label="发布者ID" width="100" />
              <el-table-column prop="field" label="领域" width="120" />
              <el-table-column prop="createdAt" label="提交时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="success" @click="auditAchievement(scope.row.id, 'PUBLISHED')">通过</el-button>
                  <el-button size="small" type="danger" @click="auditAchievement(scope.row.id, 'REJECTED')">驳回</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="资源库管理" name="libraries">
          <div class="tab-content">
            <div class="toolbar">
              <el-select v-model="libraryCategory" placeholder="选择资源类别" class="category-select" @change="fetchLibraryData">
                <el-option v-for="cat in libraryCategories" :key="cat.key" :label="cat.label" :value="cat.key" />
              </el-select>
              <el-button type="primary" :icon="Plus" @click="openEditModal()">新增</el-button>
              <el-upload
                class="upload-btn"
                :action="`/api/admin/libraries/${libraryCategory}/import`"
                :headers="{ 'Authorization': 'Bearer ' + userStore.token }"
                accept=".csv"
                :show-file-list="false"
                :on-success="handleImportSuccess"
                :on-error="handleImportError"
                :before-upload="beforeImport"
                :disabled="importing || !libraryCategory"
              >
                <el-button type="success" :icon="Upload" :loading="importing">
                  {{ importing ? '导入中...' : '批量导入CSV' }}
                </el-button>
              </el-upload>
              <el-button type="warning" :icon="Refresh" @click="fetchLibraryData" :loading="loading.library">刷新</el-button>
              <el-button type="danger" :icon="Delete" @click="clearLibrary" v-if="libraryCategory">清空数据</el-button>
            </div>

            <el-table :data="libraryData" v-loading="loading.library" style="width: 100%" empty-text="暂无数据">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column :prop="libraryTitleField" :label="libraryCategoryLabel" min-width="200" show-overflow-tooltip />
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
              <el-table-column prop="field" label="领域" width="120" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="openEditModal(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteLibraryItem(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              v-if="libraryTotal > 0"
              class="pagination"
              :current-page="libraryPage"
              :page-size="20"
              :total="libraryTotal"
              @current-change="handleLibraryPageChange"
              layout="total, prev, pager, next"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="editModalVisible" :title="isEditing ? '编辑资源' : '新增资源'" width="600px">
      <el-form :model="formData" label-width="100px">
        <template v-if="libraryCategory === 'policies'">
          <el-form-item label="政策标题">
            <el-input v-model="formData.title" />
          </el-form-item>
          <el-form-item label="发布部门">
            <el-input v-model="formData.department" />
          </el-form-item>
          <el-form-item label="发布日期">
            <el-date-picker v-model="formData.publishDate" type="date" />
          </el-form-item>
          <el-form-item label="政策类型">
            <el-input v-model="formData.policyType" />
          </el-form-item>
          <el-form-item label="政策内容">
            <el-input v-model="formData.content" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="适用行业">
            <el-input v-model="formData.industry" placeholder="多个行业用逗号分隔" />
          </el-form-item>
        </template>
        <template v-else-if="libraryCategory === 'experts'">
          <el-form-item label="专家姓名">
            <el-input v-model="formData.name" />
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="formData.title" />
          </el-form-item>
          <el-form-item label="所属机构">
            <el-input v-model="formData.affiliation" />
          </el-form-item>
          <el-form-item label="研究领域">
            <el-input v-model="formData.field" placeholder="多个领域用逗号分隔" />
          </el-form-item>
          <el-form-item label="主要成果">
            <el-input v-model="formData.achievements" type="textarea" :rows="4" />
          </el-form-item>
        </template>
        <template v-else-if="libraryCategory === 'funds'">
          <el-form-item label="资金名称">
            <el-input v-model="formData.name" />
          </el-form-item>
          <el-form-item label="资金类型">
            <el-input v-model="formData.fundType" />
          </el-form-item>
          <el-form-item label="金额范围">
            <el-input v-model="formData.amountRange" />
          </el-form-item>
          <el-form-item label="提供方">
            <el-input v-model="formData.provider" />
          </el-form-item>
          <el-form-item label="利率/占比">
            <el-input v-model="formData.interestRate" />
          </el-form-item>
          <el-form-item label="行业方向">
            <el-input v-model="formData.industryFocus" placeholder="多个方向用逗号分隔" />
          </el-form-item>
        </template>
        <template v-else-if="libraryCategory === 'equipments'">
          <el-form-item label="设备名称">
            <el-input v-model="formData.name" />
          </el-form-item>
          <el-form-item label="设备类别">
            <el-input v-model="formData.category" />
          </el-form-item>
          <el-form-item label="可用状态">
            <el-select v-model="formData.availability">
              <el-option label="可预约" value="Available" />
              <el-option label="维护中" value="Maintenance" />
            </el-select>
          </el-form-item>
          <el-form-item label="所在机构">
            <el-input v-model="formData.facilityName" />
          </el-form-item>
          <el-form-item label="规格参数">
            <el-input v-model="formData.specs" />
          </el-form-item>
          <el-form-item label="所属单位">
            <el-input v-model="formData.owner" />
          </el-form-item>
        </template>
        <template v-else-if="libraryCategory === 'patents'">
          <el-form-item label="专利名称">
            <el-input v-model="formData.title" />
          </el-form-item>
          <el-form-item label="专利状态">
            <el-input v-model="formData.status" />
          </el-form-item>
          <el-form-item label="公开日期">
            <el-date-picker v-model="formData.publicationDate" type="date" />
          </el-form-item>
          <el-form-item label="专利号">
            <el-input v-model="formData.patentNumber" />
          </el-form-item>
          <el-form-item label="摘要">
            <el-input v-model="formData.abstractText" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item label="专利权人">
            <el-input v-model="formData.assignee" />
          </el-form-item>
        </template>
        <template v-else-if="libraryCategory === 'enterprises'">
          <el-form-item label="企业名称">
            <el-input v-model="formData.name" />
          </el-form-item>
          <el-form-item label="所属行业">
            <el-input v-model="formData.industry" />
          </el-form-item>
          <el-form-item label="所在地区">
            <el-input v-model="formData.location" />
          </el-form-item>
          <el-form-item label="企业规模">
            <el-input v-model="formData.scale" />
          </el-form-item>
          <el-form-item label="企业简介">
            <el-input v-model="formData.description" type="textarea" :rows="4" />
          </el-form-item>
        </template>
        <template v-else-if="libraryCategory === 'public_platforms'">
          <el-form-item label="平台名称">
            <el-input v-model="formData.name" />
          </el-form-item>
          <el-form-item label="提供方">
            <el-input v-model="formData.provider" />
          </el-form-item>
          <el-form-item label="更新频率">
            <el-input v-model="formData.updateFrequency" />
          </el-form-item>
          <el-form-item label="数据格式">
            <el-input v-model="formData.format" />
          </el-form-item>
          <el-form-item label="领域">
            <el-input v-model="formData.domain" />
          </el-form-item>
          <el-form-item label="数据项">
            <el-input v-model="formData.dataItems" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="formData.description" type="textarea" :rows="4" />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="名称">
            <el-input v-model="formData.name" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="formData.title" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="formData.description" type="textarea" :rows="4" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="editModalVisible = false">取消</el-button>
        <el-button type="primary" @click="saveLibraryItem" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Upload, Delete } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('demands')
const pendingDemands = ref([])
const pendingAchievements = ref([])

const stats = reactive({
  totalDemands: 0,
  totalAchievements: 0,
  totalUsers: 0,
  pendingDemands: 0,
  pendingAchievements: 0
})

const loading = reactive({
  demands: false,
  achievements: false,
  library: false
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const fetchStats = async () => {
  try {
    const res = await axios.get('/api/admin/stats')
    Object.assign(stats, res.data)
  } catch (error) {
    console.error('Failed to fetch stats:', error)
  }
}

const fetchDemands = async () => {
  loading.demands = true
  try {
    const res = await axios.get('/api/demands/pending')
    pendingDemands.value = res.data
  } catch (error) {
    console.error('Failed to fetch pending demands:', error)
    ElMessage.error('获取待审核需求失败')
  } finally {
    loading.demands = false
  }
}

const fetchAchievements = async () => {
  loading.achievements = true
  try {
    const res = await axios.get('/api/achievements/pending')
    pendingAchievements.value = res.data
  } catch (error) {
    console.error('Failed to fetch pending achievements:', error)
    ElMessage.error('获取待审核成果失败')
  } finally {
    loading.achievements = false
  }
}

const handleTabClick = () => {
  if (activeTab.value === 'demands') {
    fetchDemands()
  } else if (activeTab.value === 'achievements') {
    fetchAchievements()
  } else if (activeTab.value === 'libraries') {
    fetchLibraryCategories()
  }
}

const auditDemand = async (id: number, status: string) => {
  const actionText = status === 'PUBLISHED' ? '通过' : '驳回'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该需求吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: status === 'PUBLISHED' ? 'success' : 'warning'
    })
    
    await axios.put(`/api/demands/${id}/audit`, null, {
      params: { status }
    })
    
    ElMessage.success(`需求已${actionText}`)
    fetchDemands()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

const auditAchievement = async (id: number, status: string) => {
  const actionText = status === 'PUBLISHED' ? '通过' : '驳回'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}该成果吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: status === 'PUBLISHED' ? 'success' : 'warning'
    })
    
    await axios.put(`/api/achievements/${id}/audit`, null, {
      params: { status }
    })
    
    ElMessage.success(`成果已${actionText}`)
    fetchAchievements()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

const libraryCategories = ref<any[]>([])
const libraryCategory = ref('')
const libraryData = ref<any[]>([])
const libraryTotal = ref(0)
const libraryPage = ref(1)
const editModalVisible = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const importing = ref(false)

const formData = reactive<any>({})

const libraryTitleField = computed(() => {
  const titleFields: Record<string, string> = {
    policies: 'title',
    experts: 'name',
    funds: 'name',
    equipments: 'name',
    patents: 'title',
    enterprises: 'name',
    public_platforms: 'name'
  }
  return titleFields[libraryCategory.value] || 'name'
})

const libraryCategoryLabel = computed(() => {
  const labels: Record<string, string> = {
    policies: '政策标题',
    experts: '专家姓名',
    funds: '资金名称',
    equipments: '设备名称',
    patents: '专利名称',
    enterprises: '企业名称',
    public_platforms: '平台名称'
  }
  return labels[libraryCategory.value] || '名称'
})

const fetchLibraryCategories = async () => {
  try {
    const res = await axios.get('/api/admin/libraries/categories')
    libraryCategories.value = res.data
  } catch (error) {
    console.error('Failed to fetch categories:', error)
    ElMessage.error('获取资源类别失败，请刷新页面重试')
  }
}

const fetchLibraryData = async () => {
  if (!libraryCategory.value) return
  
  loading.library = true
  try {
    const res = await axios.get(`/api/admin/libraries/${libraryCategory.value}`, {
      params: { page: libraryPage.value, size: 20 }
    })
    libraryData.value = res.data.data || []
    libraryTotal.value = res.data.total || 0
  } catch (error) {
    console.error('Failed to fetch library data:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.library = false
  }
}

const handleLibraryPageChange = (page: number) => {
  libraryPage.value = page
  fetchLibraryData()
}

const openEditModal = (item?: any) => {
  if (!libraryCategory.value) {
    ElMessage.warning('请先选择资源类别')
    return
  }
  
  isEditing.value = !!item
  if (item) {
    Object.assign(formData, item)
  } else {
    Object.keys(formData).forEach(key => delete formData[key])
  }
  editModalVisible.value = true
}

const saveLibraryItem = async () => {
  if (!libraryCategory.value) {
    ElMessage.warning('请选择资源类别')
    return
  }
  
  // 必填字段验证
  const requiredFields: Record<string, string> = {
    policies: 'title',
    experts: 'name',
    funds: 'name',
    equipments: 'name',
    patents: 'title',
    enterprises: 'name',
    public_platforms: 'name'
  }
  const requiredField = requiredFields[libraryCategory.value]
  if (!formData[requiredField] || String(formData[requiredField]).trim() === '') {
    ElMessage.warning(`请填写${libraryCategoryLabel.value}`)
    return
  }
  
  // 格式化日期字段
  const dateFields = ['publishDate', 'publicationDate']
  const submitData = { ...formData }
  dateFields.forEach(field => {
    if (submitData[field]) {
      if (submitData[field] instanceof Date) {
        submitData[field] = submitData[field].toISOString().split('T')[0]
      }
    }
  })
  
  saving.value = true
  try {
    if (isEditing.value) {
      await axios.put(`/api/admin/libraries/${libraryCategory.value}/${formData.id}`, submitData)
      ElMessage.success('更新成功')
    } else {
      await axios.post(`/api/admin/libraries/${libraryCategory.value}`, submitData)
      ElMessage.success('添加成功')
    }
    editModalVisible.value = false
    fetchLibraryData()
  } catch (error) {
    console.error('Save failed:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const deleteLibraryItem = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.delete(`/api/admin/libraries/${libraryCategory.value}/${id}`)
    ElMessage.success('删除成功')
    fetchLibraryData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

const clearLibrary = async () => {
  try {
    await ElMessageBox.confirm(`确定要清空${libraryCategoryLabel.value}的所有数据吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await axios.post(`/api/admin/libraries/${libraryCategory.value}/clear`)
    ElMessage.success('清空成功')
    fetchLibraryData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('清空失败')
    }
  }
}

const beforeImport = () => {
  if (!libraryCategory.value) {
    ElMessage.warning('请先选择资源类别')
    return false
  }
  importing.value = true
  return true
}

const handleImportSuccess = (response: any) => {
  importing.value = false
  ElMessage.success(response.message || '导入成功')
  fetchLibraryData()
}

const handleImportError = () => {
  importing.value = false
  ElMessage.error('导入失败，请检查文件格式')
}

onMounted(() => {
  if (!userStore.isLoggedIn || userStore.userRole !== 'ADMIN') {
    ElMessage.error('无权访问')
    router.push('/')
    return
  }
  
  fetchStats()
  fetchDemands()
  fetchLibraryCategories()
})
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding: 40px 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  padding: 24px;
  border-radius: 8px;
}

.dashboard-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.dashboard-header h2 {
  margin: 0;
  color: var(--text-primary);
}

.stats-cards {
  margin-bottom: 30px;
}

.card-header {
  font-weight: bold;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}

.card-value.action-needed {
  color: #F56C6C;
}

.card-footer {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.dashboard-tabs {
  background: var(--bg-card);
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.category-select {
  width: 180px;
}

.upload-btn {
  display: inline-block;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-table) {
  background-color: transparent;
  color: var(--text-primary);
  --el-table-header-bg-color: var(--bg-secondary);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
}

:deep(.el-dialog) {
  --el-dialog-bg-color: var(--bg-card);
  --el-dialog-title-color: var(--text-primary);
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--border-color);
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: var(--text-primary);
}
</style>
