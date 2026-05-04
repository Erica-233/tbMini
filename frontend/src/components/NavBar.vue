<template>
  <el-menu mode="horizontal" :router="true" :default-active="$route.path">
    <el-menu-item index="/">首页</el-menu-item>
    <el-menu-item index="/boards">板块</el-menu-item>
    <el-menu-item index="/submit">发帖</el-menu-item>
    <el-menu-item v-if="user.role === 'ADMIN'" index="/admin/mod-queue">管理后台</el-menu-item>
    <div style="flex: 1"></div>
    <template v-if="user.isLoggedIn">
      <span style="align-self: center; margin-right: 12px">{{ user.user?.nickname }}</span>
      <el-button link style="align-self: center; margin-right: 12px" @click="logout">退出</el-button>
    </template>
    <template v-else>
      <el-menu-item index="/login">登录</el-menu-item>
      <el-menu-item index="/register">注册</el-menu-item>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const user = useUserStore()
const router = useRouter()

function logout() {
  user.logout()
  router.push('/login')
}
</script>
