<template>
  <div class="container">
    <NavBar />
    <div v-if="board" class="board-header">
      <h2>{{ board.name }}</h2>
      <p style="color:#666">{{ board.description }}</p>
    </div>
    <el-button type="primary" @click="$router.push('/submit?slug=' + slug)">发帖</el-button>
    <div style="margin-top:12px">
      <PostCard v-for="p in posts" :key="p.id" :post="p" />
    </div>
    <el-pagination
      v-model:current-page="page"
      :page-size="20"
      :total="total"
      layout="prev, pager, next"
      @current-change="load"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getBoardBySlug, listBoardPosts } from '@/api/board'
import type { BoardDetailResponse } from '@/api/board'
import NavBar from '@/components/NavBar.vue'
import PostCard from '@/components/PostCard.vue'

const route = useRoute()
const slug = route.params.slug as string
const board = ref<BoardDetailResponse | null>(null)
const posts = ref<any[]>([])
const page = ref(1)
const total = ref(0)

async function load() {
  const bres: any = await getBoardBySlug(slug)
  board.value = bres.data
  const pres: any = await listBoardPosts(slug, { page: page.value, pageSize: 20 })
  posts.value = pres.data?.records || []
  total.value = pres.data?.total || 0
}

onMounted(load)

watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/b/')) {
    page.value = 1
    load()
  }
})
</script>

<style scoped>
.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 16px;
}
.board-header {
  margin-bottom: 12px;
}
</style>
