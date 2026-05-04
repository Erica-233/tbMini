import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listComments, createComment } from '@/api/comment'
import type { CommentVo, CreateCommentData } from '@/api/comment'

export const useCommentStore = defineStore('comment', () => {
  const comments = ref<CommentVo[]>([])

  async function fetchComments(postId: number) {
    const res: any = await listComments(postId)
    comments.value = res.data || []
  }

  async function addComment(data: CreateCommentData) {
    await createComment(data)
    await fetchComments(data.postId)
  }

  return { comments, fetchComments, addComment }
})
