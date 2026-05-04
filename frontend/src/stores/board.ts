import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listBoards, getBoardBySlug, listBoardPosts } from '@/api/board'
import type { Board, BoardDetailResponse } from '@/api/board'

export const useBoardStore = defineStore('board', () => {
  const boards = ref<Board[]>([])
  const currentBoard = ref<BoardDetailResponse | null>(null)
  const boardPosts = ref<any[]>([])
  const total = ref(0)

  async function fetchBoards() {
    const res: any = await listBoards()
    boards.value = res.data || []
  }

  async function fetchBoardBySlug(slug: string) {
    const res: any = await getBoardBySlug(slug)
    currentBoard.value = res.data
  }

  async function fetchBoardPosts(slug: string, page = 1, pageSize = 20) {
    const res: any = await listBoardPosts(slug, { page, pageSize })
    boardPosts.value = res.data?.records || []
    total.value = res.data?.total || 0
  }

  return { boards, currentBoard, boardPosts, total, fetchBoards, fetchBoardBySlug, fetchBoardPosts }
})
