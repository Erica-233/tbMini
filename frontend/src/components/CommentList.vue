<template>
  <div class="comment-list">
    <div v-for="c in comments" :key="c.id" class="comment-item">
      <div class="comment-main">
        <el-avatar v-if="c.userAvatar" :src="c.userAvatar" :size="28" />
        <el-avatar v-else :size="28">{{ c.userNickname?.[0] }}</el-avatar>
        <div class="comment-body">
          <div class="comment-header">
            <span class="nickname">{{ c.userNickname }}</span>
            <span class="time">{{ formatDate(c.createdAt) }}</span>
          </div>
          <div class="comment-content">{{ c.bodyMd }}</div>
          <div class="comment-actions">
            <el-button link size="small" @click="startReply(c.id)">回复</el-button>
            <el-button link size="small" @click="openReport('COMMENT', c.id)">举报</el-button>
          </div>
          <div v-if="replyTo === c.id" class="reply-box">
            <el-input v-model="replyContent" type="textarea" :rows="2" placeholder="回复..." />
            <div class="reply-actions">
              <el-button size="small" @click="replyTo = null">取消</el-button>
              <el-button type="primary" size="small" @click="submitReply(c.id)">发送</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="c.children?.length" class="children">
        <div v-for="r in c.children" :key="r.id" class="child-item">
          <el-avatar v-if="r.userAvatar" :src="r.userAvatar" :size="24" />
          <el-avatar v-else :size="24">{{ r.userNickname?.[0] }}</el-avatar>
          <div class="child-body">
            <div class="child-header">
              <span class="nickname">{{ r.userNickname }}</span>
              <span class="time">{{ formatDate(r.createdAt) }}</span>
            </div>
            <div class="child-content">{{ r.bodyMd }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { CommentVo } from '@/api/comment'

interface Props {
  comments: CommentVo[]
  postId: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'reply', parentId: number, content: string): void
  (e: 'report', type: string, id: number): void
}>()

const replyTo = ref<number | null>(null)
const replyContent = ref('')

function startReply(id: number) {
  replyTo.value = id
  replyContent.value = ''
}

function submitReply(parentId: number) {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  emit('reply', parentId, replyContent.value)
  replyTo.value = null
  replyContent.value = ''
}

function openReport(type: string, id: number) {
  emit('report', type, id)
}

function formatDate(d: string) {
  return d ? d.replace('T', ' ').slice(0, 16) : ''
}
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.comment-item {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
}
.comment-main {
  display: flex;
  gap: 10px;
}
.comment-body {
  flex: 1;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.nickname {
  font-weight: 600;
  font-size: 14px;
}
.time {
  color: #999;
  font-size: 12px;
}
.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}
.comment-actions {
  margin-top: 6px;
}
.reply-box {
  margin-top: 8px;
}
.reply-actions {
  margin-top: 6px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.children {
  margin-top: 10px;
  padding-left: 38px;
  border-left: 2px solid #eee;
}
.child-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.child-body {
  flex: 1;
}
.child-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}
.child-content {
  font-size: 13px;
  color: #555;
}
</style>
