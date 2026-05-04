<template>
  <div class="container">
    <NavBar />
    <el-card v-if="post">
      <div class="post-header">
        <el-avatar v-if="post.userAvatar" :src="post.userAvatar" :size="40" />
        <el-avatar v-else :size="40">{{ post.userNickname?.[0] }}</el-avatar>
        <div class="post-meta">
          <div class="nickname">{{ post.userNickname }}</div>
          <div class="time">{{ formatDate(post.createdAt) }} · {{ post.boardName }}</div>
        </div>
      </div>
      <h2 style="margin: 12px 0">{{ post.title }}</h2>
      <div class="post-body">{{ post.bodyMd }}</div>
      <div v-if="post.images?.length" class="post-images">
        <el-image
          v-for="(img, idx) in post.images"
          :key="idx"
          :src="img"
          fit="cover"
          style="width: 160px; height: 160px; border-radius: 8px; margin-right: 8px"
        />
      </div>
      <div class="post-actions">
        <el-button v-if="!post.liked" @click="onLike">👍 点赞 {{ post.likeCount }}</el-button>
        <el-button v-else type="primary" @click="onUnlike">👍 已点赞 {{ post.likeCount }}</el-button>
        <el-button @click="reportVisible = true">举报</el-button>
        <el-button v-if="canDelete" type="danger" @click="onDelete">删除帖子</el-button>
      </div>
    </el-card>

    <el-card v-else style="text-align: center; padding: 40px">
      <p>帖子不存在或已被删除</p>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </el-card>

    <template v-if="post">
    <h3 style="margin-top: 16px">评论</h3>
    <el-card style="margin-bottom: 12px">
      <el-input v-model="commentContent" type="textarea" :rows="2" placeholder="写评论..." />
      <el-button type="primary" style="margin-top: 8px" @click="submitComment">发表评论</el-button>
    </el-card>

    <CommentList
      :comments="comments"
      :post-id="postId"
      @reply="submitReply"
      @report="openReport"
    />

    </template>

    <el-dialog v-model="reportVisible" title="举报" width="400px">
      <el-input v-model="reportReason" type="textarea" :rows="3" placeholder="举报原因" />
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPost, likePost, unlikePost, deletePost } from '@/api/post'
import { listComments, createComment } from '@/api/comment'
import { report } from '@/api/report'
import type { PostDetailResponse } from '@/api/post'
import type { CommentVo } from '@/api/comment'
import NavBar from '@/components/NavBar.vue'
import CommentList from '@/components/CommentList.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const postId = Number(route.params.id)
const post = ref<PostDetailResponse | null>(null)
const comments = ref<CommentVo[]>([])
const commentContent = ref('')
const reportVisible = ref(false)
const reportReason = ref('')
const reportTarget = ref<{ type: string; id: number } | null>(null)

const canDelete = computed(() => {
  if (!userStore.isLoggedIn || !post.value) return false
  return post.value.userId === userStore.user?.id || userStore.role === 'ADMIN'
})

async function load() {
  try {
    const pres: any = await getPost(postId)
    post.value = pres.data
    const cres: any = await listComments(postId)
    comments.value = cres.data || []
  } catch {
    post.value = null
    comments.value = []
  }
}

async function onLike() {
  await likePost(postId)
  if (post.value) {
    post.value.liked = true
    post.value.likeCount++
  }
  ElMessage.success('点赞成功')
}

async function onUnlike() {
  await unlikePost(postId)
  if (post.value) {
    post.value.liked = false
    post.value.likeCount--
  }
  ElMessage.success('取消点赞')
}

async function submitComment() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  await createComment({ postId, bodyMd: commentContent.value })
  commentContent.value = ''
  ElMessage.success('评论成功')
  load()
}

async function submitReply(parentId: number, content: string) {
  await createComment({ postId, parentId, bodyMd: content })
  ElMessage.success('回复成功')
  load()
}

function openReport(type: string, id: number) {
  reportTarget.value = { type, id }
  reportReason.value = ''
  reportVisible.value = true
}

async function submitReport() {
  if (!reportTarget.value) return
  await report({ targetType: reportTarget.value.type, targetId: reportTarget.value.id, reason: reportReason.value })
  reportVisible.value = false
  ElMessage.success('举报成功')
}

async function onDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后不可恢复。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deletePost(postId)
    ElMessage.success('帖子已删除')
    router.push('/')
  } catch {
    ElMessage.error('删除失败，请重试')
  }
}

function formatDate(d: string) {
  return d ? d.replace('T', ' ').slice(0, 16) : ''
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 16px;
}
.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.post-meta {
  flex: 1;
}
.nickname {
  font-weight: 600;
}
.time {
  color: #999;
  font-size: 12px;
}
.post-body {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #333;
}
.post-images {
  margin-top: 12px;
}
.post-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
</style>
