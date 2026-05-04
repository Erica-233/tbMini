import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listPosts, getPost, likePost, unlikePost } from '@/api/post'
import type { PostDetailResponse, PostListItemVo } from '@/api/post'

export const usePostStore = defineStore('post', () => {
  const posts = ref<PostListItemVo[]>([])
  const currentPost = ref<PostDetailResponse | null>(null)
  const total = ref(0)

  async function fetchPosts(params: { boardId?: number; page?: number; size?: number }) {
    const res: any = await listPosts(params)
    posts.value = res.data?.records || []
    total.value = res.data?.total || 0
  }

  async function fetchPost(id: number) {
    const res: any = await getPost(id)
    currentPost.value = res.data
  }

  async function toggleLike(id: number) {
    if (currentPost.value?.liked) {
      await unlikePost(id)
      currentPost.value.liked = false
      currentPost.value.likeCount--
    } else {
      await likePost(id)
      if (currentPost.value) {
        currentPost.value.liked = true
        currentPost.value.likeCount++
      }
    }
  }

  return { posts, currentPost, total, fetchPosts, fetchPost, toggleLike }
})
