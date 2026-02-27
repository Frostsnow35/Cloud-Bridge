<template>
  <div class="evidence-page">
    <div class="header-row">
      <h2>区块链存证服务</h2>
      <el-tag type="warning" effect="dark" class="network-tag">模拟网络 (Mock Network)</el-tag>
    </div>
    
    <el-tabs type="border-card">
      <el-tab-pane label="存证上链">
        <el-form :model="storeForm" label-width="100px">
          <el-form-item label="存证类型">
            <el-select v-model="storeForm.type" placeholder="请选择存证类型">
              <el-option label="技术交底" value="DISCLOSURE" />
              <el-option label="合同签署" value="CONTRACT" />
              <el-option label="项目交付" value="DELIVERY" />
              <el-option label="其它" value="DEFAULT" />
            </el-select>
          </el-form-item>
          <el-form-item label="存证内容">
            <el-input 
              v-model="storeForm.content" 
              type="textarea" 
              rows="5" 
              placeholder="请输入需要存证的文本内容，例如：技术交底书摘要、合同关键条款..."
            />
          </el-form-item>
          <el-form-item label="元数据">
            <el-input v-model="storeForm.metadata" placeholder="可选 JSON 元数据" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="onStore" :loading="storing">立即存证</el-button>
          </el-form-item>
        </el-form>
        
        <div v-if="txHash" class="result-box success">
          <el-alert
            title="存证成功！"
            type="success"
            :description="'交易哈希: ' + txHash"
            show-icon
          />
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="存证核验">
        <el-form :model="verifyForm" label-width="100px">
          <el-form-item label="文件哈希">
            <el-input v-model="verifyForm.hash" placeholder="请输入 SHA256 哈希值" />
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="onVerify" :loading="verifying">开始核验</el-button>
          </el-form-item>
        </el-form>

        <div v-if="verifyResult" class="result-box">
          <el-descriptions title="核验结果" border>
            <el-descriptions-item label="状态">
              <el-tag type="success">已上链</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="拥有者">{{ verifyResult.owner }}</el-descriptions-item>
            <el-descriptions-item label="签署人ID">{{ verifyResult.signerId || 'SYSTEM' }}</el-descriptions-item>
            <el-descriptions-item label="存证类型">{{ verifyResult.evidenceType || 'DEFAULT' }}</el-descriptions-item>
            <el-descriptions-item label="时间戳">{{ new Date(verifyResult.timestamp * 1000).toLocaleString() }}</el-descriptions-item>
            <el-descriptions-item label="元数据">{{ verifyResult.metadata }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

// Simple SHA256 simulation for MVP
const sha256 = async (message: string) => {
  const msgBuffer = new TextEncoder().encode(message);
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  return hashHex;
}

const storeForm = reactive({
  content: '',
  metadata: '',
  type: 'DEFAULT'
})

const storing = ref(false)
const txHash = ref('')

const onStore = async () => {
  if (!storeForm.content) {
    ElMessage.warning('请输入存证内容')
    return
  }
  storing.value = true
  try {
    const hash = await sha256(storeForm.content)
    const response = await axios.post('/api/evidence/store', {
      hash: hash,
      metadata: storeForm.metadata || '{}',
      type: storeForm.type
    })
    txHash.value = response.data.txHash
    ElMessage.success('存证成功')
  } catch (error: any) {
    ElMessage.error('存证失败: ' + (error.response?.data || error.message))
  } finally {
    storing.value = false
  }
}

const verifyForm = reactive({
  hash: ''
})
const verifying = ref(false)
const verifyResult = ref<any>(null)

const onVerify = async () => {
  if (!verifyForm.hash) {
    ElMessage.warning('请输入哈希值')
    return
  }
  verifying.value = true
  verifyResult.value = null
  try {
    const response = await axios.get(`/api/evidence/verify/${verifyForm.hash}`)
    verifyResult.value = response.data
    ElMessage.success('核验通过')
  } catch (error) {
    ElMessage.error('核验失败：未找到该存证记录')
  } finally {
    verifying.value = false
  }
}
</script>

<style scoped>
.evidence-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.result-box {
  margin-top: 20px;
}
</style>
