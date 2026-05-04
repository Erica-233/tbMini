<template>
  <div class="container">
    <NavBar />
    <h2>板块列表</h2>
    <el-row :gutter="16">
      <el-col :span="8" v-for="b in boards" :key="b.id">
        <el-card @click="$router.push('/b/' + b.slug)" style="cursor: pointer; margin-bottom: 12px">
          <div style="font-weight: bold">{{ b.name }}</div>
          <div style="color: #666; font-size: 14px">{{ b.description }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listBoards } from '@/api/board'
import type { Board } from '@/api/board'
import NavBar from '@/components/NavBar.vue'

const boards = ref<Board[]>([])

onMounted(async () => {
  const res: any = await listBoards()
  boards.value = res.data || []
})
</script>

<style scoped>
.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 16px;
}
</style>
