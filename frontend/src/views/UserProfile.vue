<template>
  <div class="container">
    <NavBar />
    <el-card v-if="profile">
      <div class="profile-header">
        <el-avatar v-if="profile.avatar" :src="profile.avatar" :size="80" />
        <el-avatar v-else :size="80">{{ profile.nickname?.[0] }}</el-avatar>
        <div class="profile-info">
          <h2>{{ profile.nickname }}</h2>
          <div class="meta">@{{ profile.username }} · 注册于 {{ formatDate(profile.createdAt) }}</div>
        </div>
      </div>
    </el-card>

    <h3 style="margin-top: 16px">发帖记录</h3>
    <PostCard v-for="p in posts" :key="p.id" :post="p" />
    <el-pagination
      v-model:current-page="page"
      :page-size="10"
      :total="total"
      layout="prev, pager, next"
      @current-change="loadPosts"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getUserProfile, getUserPosts } from '@/api/user'
import type { UserProfile } from '@/api/user'
import type { PostVo } from '@/api/post'
import NavBar from '@/components/NavBar.vue'
import PostCard from '@/components/PostCard.vue'

const route = useRoute()
const profile = ref<UserProfile | null>(null)
const posts = ref<PostVo[]>([])
const page = ref(1)
const total = ref(0)

async function loadProfile() {
  const username = route.params.username as string
  try {
    const res: any = await getUserProfile(username)
    profile.value = res.data
  } catch {
    profile.value = null
  }
}

async function loadPosts() {
  const username = route.params.username as string
  try {
    const res: any = await getUserPosts(username, { page: page.value, size: 10 })
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    posts.value = []
    total.value = 0
  }
}

function formatDate(d: string) {
  return d ? d.replace('T', ' ').slice(0, 10) : ''
}

onMounted(() => {
  loadProfile()
  loadPosts()
})

watch(() => route.params.username, () => {
  page.value = 1
  loadProfile()
  loadPosts()
})
</script>

<style scoped>
.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 16px;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.profile-info {
  flex: 1;
}
.profile-info h2 {
  margin: 0 0 4px 0;
}
.meta {
  color: #999;
  font-size: 14px;
}
</style>
