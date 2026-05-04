<template>
  <div class="post-card" @click="goDetail">
    <div class="post-main">
      <div class="vote-section">
        <button class="vote-btn" @click.stop="handleLike">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="{ liked: post.liked }">
            <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
          </svg>
        </button>
        <span class="vote-count" :class="{ liked: post.liked }">{{ post.likeCount || 0 }}</span>
      </div>

      <div class="post-content">
        <div class="post-header">
          <div class="author-info">
            <el-avatar v-if="post.userAvatar" :src="post.userAvatar" :size="24" />
            <el-avatar v-else :size="24">{{ post.userNickname?.[0] }}</el-avatar>
            <span class="author-name" @click.stop="goUser">{{ post.userNickname }}</span>
            <span class="separator">·</span>
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          </div>
          <el-tag v-if="post.boardName" size="small" effect="plain" class="board-tag">
            {{ post.boardName }}
          </el-tag>
        </div>

        <h3 class="post-title">{{ post.title }}</h3>
        <p class="post-summary">{{ post.summary || post.content }}</p>

        <div v-if="post.imageUrl || post.images?.length" class="post-images">
          <div class="image-grid" :class="`grid-${Math.min(displayImages.length, 3)}`">
            <div
              v-for="(img, idx) in displayImages"
              :key="idx"
              class="image-wrapper"
            >
              <img :src="img" :alt="post.title" loading="lazy" />
            </div>
          </div>
        </div>

        <div class="post-footer">
          <div class="action-btns">
            <span class="action-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
              </svg>
              {{ post.commentCount || 0 }} 评论
            </span>
            <span class="action-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="18" cy="5" r="3"/>
                <circle cx="6" cy="12" r="3"/>
                <circle cx="18" cy="19" r="3"/>
                <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
              </svg>
              分享
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

interface PostCardProps {
  post: any
}

const props = defineProps<PostCardProps>()
const router = useRouter()

const displayImages = computed(() => {
  const images = []
  if (props.post.imageUrl) images.push(props.post.imageUrl)
  if (props.post.images?.length) {
    props.post.images.forEach((img: string) => {
      if (!images.includes(img)) images.push(img)
    })
  }
  return images.slice(0, 3)
})

function goDetail() {
  router.push(`/p/${props.post.id}`)
}

function goUser() {
  if (props.post.username) {
    router.push(`/u/${props.post.username}`)
  }
}

function handleLike() {
  // Like functionality will be handled in detail page for now
  goDetail()
}

function formatTime(d: string) {
  if (!d) return ''
  const date = new Date(d)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return d.replace('T', ' ').slice(0, 10)
}
</script>

<style scoped>
.post-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow 0.3s ease, transform 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
  overflow: hidden;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.04);
  transform: translateY(-1px);
}

.post-main {
  display: flex;
}

.vote-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  background: #fafbfc;
  border-right: 1px solid #f0f0f0;
  min-width: 48px;
}

.vote-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: background 0.2s;
  color: #667085;
}

.vote-btn:hover {
  background: #e4e7ec;
}

.vote-btn svg {
  width: 20px;
  height: 20px;
}

.vote-btn svg.liked {
  fill: #f43f5e;
  stroke: #f43f5e;
}

.vote-count {
  font-size: 13px;
  font-weight: 600;
  color: #667085;
  margin-top: 4px;
}

.vote-count.liked {
  color: #f43f5e;
}

.post-content {
  flex: 1;
  padding: 16px;
  min-width: 0;
}

.post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 13px;
  font-weight: 500;
  color: #344054;
  cursor: pointer;
}

.author-name:hover {
  color: #4f46e5;
  text-decoration: underline;
}

.separator {
  color: #d0d5dd;
  font-size: 12px;
}

.post-time {
  font-size: 12px;
  color: #98a2b3;
}

.board-tag {
  font-size: 12px;
  border-radius: 6px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.post-summary {
  font-size: 14px;
  color: #667085;
  line-height: 1.6;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-images {
  margin-top: 12px;
}

.image-grid {
  display: grid;
  gap: 8px;
}

.image-grid.grid-1 {
  grid-template-columns: 1fr;
  max-width: 400px;
}

.image-grid.grid-2 {
  grid-template-columns: repeat(2, 1fr);
  max-width: 400px;
}

.image-grid.grid-3 {
  grid-template-columns: repeat(3, 1fr);
  max-width: 400px;
}

.image-wrapper {
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  background: #f2f4f7;
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.post-card:hover .image-wrapper img {
  transform: scale(1.03);
}

.post-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f2f4f7;
}

.action-btns {
  display: flex;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #667085;
  cursor: pointer;
  transition: color 0.2s;
}

.action-btn:hover {
  color: #4f46e5;
}

.action-btn svg {
  width: 16px;
  height: 16px;
}
</style>
