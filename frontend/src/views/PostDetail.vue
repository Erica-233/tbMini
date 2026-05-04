<template>
  <div class="page">
    <NavBar />
    <div class="container">
      <div class="main-layout">
        <div class="content">
          <el-card v-if="post" class="post-card">
            <div class="post-header">
              <div class="author">
                <el-avatar v-if="post.userAvatar" :src="post.userAvatar" :size="44" />
                <el-avatar v-else :size="44">{{ post.userNickname?.[0] }}</el-avatar>
                <div class="author-info">
                  <div class="author-name" @click="goUser">{{ post.userNickname }}</div>
                  <div class="post-meta">
                    <span>{{ formatDate(post.createdAt) }}</span>
                    <span class="separator">·</span>
                    <el-tag size="small" effect="plain">{{ post.boardName }}</el-tag>
                  </div>
                </div>
              </div>
              <el-dropdown v-if="canDelete" @command="handleCommand">
                <button class="more-btn">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="5" r="1"/>
                    <circle cx="12" cy="12" r="1"/>
                    <circle cx="12" cy="19" r="1"/>
                  </svg>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="delete" style="color: #f43f5e;">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 16px; height: 16px; margin-right: 8px;">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                      </svg>
                      删除帖子
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <h1 class="post-title">{{ post.title }}</h1>
            <div class="post-body">{{ post.bodyMd }}</div>

            <div v-if="post.images?.length" class="post-images">
              <div class="image-grid" :class="`grid-${Math.min(post.images.length, 3)}`">
                <div
                  v-for="(img, idx) in post.images"
                  :key="idx"
                  class="image-wrapper"
                  @click="previewImage(img)"
                >
                  <img :src="img" :alt="post.title" loading="lazy" />
                </div>
              </div>
            </div>

            <div class="post-actions">
              <button
                class="action-btn"
                :class="{ active: post.liked }"
                @click="post.liked ? onUnlike() : onLike()"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="{ liked: post.liked }">
                  <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
                </svg>
                <span>{{ post.likeCount || 0 }}</span>
              </button>
              <button class="action-btn" @click="scrollToComments">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
                </svg>
                <span>{{ post.commentCount || 0 }}</span>
              </button>
              <button class="action-btn" @click="reportVisible = true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
                  <line x1="4" y1="22" x2="4" y2="15"/>
                </svg>
                <span>举报</span>
              </button>
            </div>
          </el-card>

          <el-card v-else class="empty-card">
            <div class="empty-state">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="10"/>
                <line x1="8" y1="12" x2="16" y2="12"/>
              </svg>
              <p>帖子不存在或已被删除</p>
              <el-button type="primary" @click="router.push('/')">返回首页</el-button>
            </div>
          </el-card>

          <template v-if="post">
            <div id="comments" class="comments-section">
              <h3 class="section-title">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
                </svg>
                评论 ({{ post.commentCount || 0 }})
              </h3>

              <el-card class="comment-input-card">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="3"
                  placeholder="写下你的评论..."
                  class="comment-input"
                />
                <div class="comment-submit">
                  <el-button type="primary" @click="submitComment">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 16px; height: 16px; margin-right: 6px; vertical-align: middle;">
                      <line x1="22" y1="2" x2="11" y2="13"/>
                      <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                    </svg>
                    发表评论
                  </el-button>
                </div>
              </el-card>

              <CommentList
                :comments="comments"
                :post-id="postId"
                @reply="submitReply"
                @report="openReport"
              />
            </div>
          </template>
        </div>

        <aside class="sidebar">
          <div class="sidebar-card">
            <h3 class="sidebar-title">关于作者</h3>
            <div v-if="post" class="author-profile">
              <el-avatar v-if="post.userAvatar" :src="post.userAvatar" :size="56" />
              <el-avatar v-else :size="56">{{ post.userNickname?.[0] }}</el-avatar>
              <div class="author-name-lg">{{ post.userNickname }}</div>
              <el-button type="primary" plain size="small">关注</el-button>
            </div>
          </div>

          <div class="sidebar-card">
            <h3 class="sidebar-title">相关板块</h3>
            <div v-if="post" class="related-board" @click="$router.push('/b/' + post.boardSlug)">
              <div class="board-icon">{{ post.boardName?.[0] }}</div>
              <div class="board-info">
                <span class="board-name">{{ post.boardName }}</span>
                <span class="board-desc">查看更多内容</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>

    <el-dialog v-model="reportVisible" title="举报内容" width="420px" class="report-dialog">
      <div class="report-content">
        <p class="report-desc">请选择举报原因：</p>
        <el-radio-group v-model="reportReasonCode" class="report-options">
          <el-radio label="SPAM">垃圾信息</el-radio>
          <el-radio label="HARASSMENT">骚扰或攻击</el-radio>
          <el-radio label="ILLEGAL">违法违规</el-radio>
          <el-radio label="OTHER">其他原因</el-radio>
        </el-radio-group>
        <el-input
          v-model="reportReason"
          type="textarea"
          :rows="3"
          placeholder="请详细描述举报原因（可选）"
          class="report-input"
        />
      </div>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport">提交举报</el-button>
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
const reportReasonCode = ref('SPAM')
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
  reportReasonCode.value = 'SPAM'
  reportVisible.value = true
}

async function submitReport() {
  if (!reportTarget.value) return
  await report({
    targetType: reportTarget.value.type,
    targetId: reportTarget.value.id,
    reason: reportReason.value,
    reasonCode: reportReasonCode.value
  })
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

function handleCommand(command: string) {
  if (command === 'delete') {
    onDelete()
  }
}

function scrollToComments() {
  document.getElementById('comments')?.scrollIntoView({ behavior: 'smooth' })
}

function previewImage(url: string) {
  window.open(url, '_blank')
}

function formatDate(d: string) {
  return d ? d.replace('T', ' ').slice(0, 16) : ''
}

function goUser() {
  if (post.value?.username) {
    router.push(`/u/${post.value.username}`)
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8f9fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.main-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.content {
  min-width: 0;
}

.post-card {
  margin-bottom: 16px;
}

.post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  cursor: pointer;
}

.author-name:hover {
  color: #4f46e5;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #98a2b3;
}

.separator {
  color: #d0d5dd;
}

.more-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  color: #98a2b3;
  transition: all 0.2s;
}

.more-btn:hover {
  background: #f2f4f7;
  color: #667085;
}

.more-btn svg {
  width: 20px;
  height: 20px;
}

.post-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.post-body {
  font-size: 15px;
  line-height: 1.8;
  color: #344054;
  white-space: pre-wrap;
}

.post-images {
  margin-top: 20px;
}

.image-grid {
  display: grid;
  gap: 12px;
}

.image-grid.grid-1 {
  grid-template-columns: 1fr;
  max-width: 500px;
}

.image-grid.grid-2 {
  grid-template-columns: repeat(2, 1fr);
  max-width: 500px;
}

.image-grid.grid-3 {
  grid-template-columns: repeat(3, 1fr);
  max-width: 500px;
}

.image-wrapper {
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: #f2f4f7;
  cursor: pointer;
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-wrapper:hover img {
  transform: scale(1.05);
}

.post-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f2f4f7;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 1px solid #e4e7ec;
  border-radius: 10px;
  background: #fff;
  color: #667085;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f9fafb;
  border-color: #d0d5dd;
}

.action-btn.active {
  background: #fef2f2;
  border-color: #fecaca;
  color: #f43f5e;
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

.action-btn svg.liked {
  fill: #f43f5e;
  stroke: #f43f5e;
}

.empty-card {
  padding: 40px;
}

.empty-state {
  text-align: center;
  color: #667085;
}

.empty-state svg {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

.comments-section {
  margin-top: 8px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title svg {
  width: 22px;
  height: 22px;
  color: #4f46e5;
}

.comment-input-card {
  margin-bottom: 16px;
}

.comment-input :deep(.el-textarea__inner) {
  border-radius: 10px;
}

.comment-submit {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
}

.author-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.author-name-lg {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.related-board {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.related-board:hover {
  background: #f9fafb;
}

.board-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 18px;
}

.board-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.board-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.board-desc {
  font-size: 12px;
  color: #667085;
}

.report-content {
  padding: 8px 0;
}

.report-desc {
  font-size: 14px;
  color: #667085;
  margin-bottom: 16px;
}

.report-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.report-input {
  margin-top: 8px;
}

@media (max-width: 900px) {
  .main-layout {
    grid-template-columns: 1fr;
  }
  .sidebar {
    display: none;
  }
}
</style>
