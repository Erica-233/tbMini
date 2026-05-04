<template>
  <div class="page">
    <el-card class="box" style="width: 400px">
      <h2 style="text-align: center">登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
const form = reactive({ email: '', password: '' })

async function onSubmit() {
  const res: any = await login(form)
  user.setToken(res.data.token, res.data.user)
  ElMessage.success('登录成功')
  router.push('/')
}
</script>

<style scoped>
.page {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}
</style>
