<template>
  <div class="container">
    <NavBar />
    <h2>发布帖子</h2>
    <el-form :model="form" label-width="80px">
      <el-form-item label="板块">
        <el-select v-model="form.boardSlug" placeholder="选择板块">
          <el-option v-for="b in boards" :key="b.id" :label="b.name" :value="b.slug" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入标题" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="form.bodyMd" type="textarea" :rows="6" placeholder="请输入正文" />
      </el-form-item>
      <el-form-item label="图片">
        <ImageUploader v-model="images" :max="9" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">发布</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost } from '@/api/post'
import { listBoards } from '@/api/board'
import type { Board } from '@/api/board'
import NavBar from '@/components/NavBar.vue'
import ImageUploader from '@/components/ImageUploader.vue'

const route = useRoute()
const router = useRouter()
const boards = ref<Board[]>([])
const images = ref<File[]>([])

const form = reactive({
  boardSlug: (route.query.slug as string) || '',
  title: '',
  bodyMd: ''
})

async function onSubmit() {
  if (!form.boardSlug) {
    ElMessage.warning('请选择板块')
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }

  const fd = new FormData()
  fd.append('data', new Blob([JSON.stringify(form)], { type: 'application/json' }))
  images.value.forEach((file) => {
    fd.append('images', file)
  })

  const res: any = await createPost(fd)
  ElMessage.success('发布成功')
  router.push('/p/' + res.data.id)
}

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
