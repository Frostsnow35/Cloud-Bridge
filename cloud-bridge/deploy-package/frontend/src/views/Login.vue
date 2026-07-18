<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>登录 Cloud Bridge</h2>
      <el-form :model="form" label-width="80px" @submit.prevent="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit" :loading="loading" class="w-100">登录</el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    const redirect = router.currentRoute.value.query.redirect as string || '/'
    router.push(redirect)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '登录失败：用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
  background: radial-gradient(circle at center, var(--bg-secondary) 0%, var(--bg-primary) 100%);
}
.login-card {
  width: 400px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  padding: 20px;
}
h2 {
  text-align: center;
  margin-bottom: 24px;
  color: var(--text-primary);
}
.w-100 {
  width: 100%;
}
.register-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: var(--text-secondary);
}
.register-link a {
  color: var(--el-color-primary);
  text-decoration: none;
}
</style>
