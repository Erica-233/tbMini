<template>
  <el-card class="post-card" @click="goDetail">
    <div class="header">
      <el-avatar v-if="post.userAvatar" :src="post.userAvatar" :size="32" />
      <el-avatar v-else :size="32">{{ post.userNickname?.[0] }}</el-avatar>
      <div class="meta">
        <span class="nickname">{{ post.userNickname }}</span>
        <span class="time">{{ formatDate(post.createdAt) }}</span>
      </div>
      <el-tag v-if="post.boardName" size="small" type="info">{{ post.boardName }}</el-tag>
    </div>
    <div class="title">{{ post.title }}</div>
    <div class="summary">{{ post.summary || post.content }}</div>
    <div v-if="post.imageUrl || post.images?.length" class="images">
      <el-image
        v-if="post.imageUrl"
        :src="post.imageUrl"
        fit="cover"
        style="width: 120px; height: 120px; border-radius: 8px"
      />
      <el-image
        v-for="(img, idx) in post.images?.slice(0, 3)"
        :key="idx"
        :src="img"
        fit="cover"
        style="width: 120px; height: 120px; border-radius: 8px; margin-left: 8px"
      />
    </div>
    <div class="footer">
      <span>👍 {{ post.likeCount || 0 }}</span>
      <span v-if="post.commentCount !== undefined">💬 {{ post.commentCount }}</span>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface PostCardProps {
  post: any
}

const props = defineProps<PostCardProps>()
const router = useRouter()

function goDetail() {
  router.push(`/p/${props.post.id}`)
}

function formatDate(d: string) {
  return d ? d.replace('T', ' ').slice(0, 16) : ''
}
</script>

<style scoped>
.post-card {
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.meta {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.nickname {
  font-weight: 600;
  font-size: 14px;
}
.time {
  color: #999;
  font-size: 12px;
}
.title {
  font-weight: 700;
  font-size: 16px;
  margin-bottom: 6px;
}
.summary {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
.images {
  margin-top: 8px;
  display: flex;
}
.footer {
  margin-top: 10px;
  color: #999;
  font-size: 13px;
  display: flex;
  gap: 16px;
}
</style>
