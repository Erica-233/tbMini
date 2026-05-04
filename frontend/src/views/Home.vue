<template>
  <div class="container">
    <NavBar />
    <h2>全站新帖</h2>
    <PostCard v-for="p in posts" :key="p.id" :post="p" />
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
import { listPosts } from '@/api/post'
import NavBar from '@/components/NavBar.vue'
import PostCard from '@/components/PostCard.vue'

const route = useRoute()
const posts = ref<any[]>([])
const page = ref(1)
const total = ref(0)

async function load() {
  const res: any = await listPosts({ page: page.value, size: 20 })
  posts.value = res.data?.records || []
  total.value = res.data?.total || 0
}

onMounted(load)

watch(() => route.path, (newPath) => {
  if (newPath === '/') {
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
</style>
