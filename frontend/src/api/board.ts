import request from './request'

export interface Board {
  id: number
  name: string
  slug: string
  description: string
  createdAt: string
}

export interface BoardDetailResponse {
  id: number
  name: string
  slug: string
  description: string
  isActive: boolean
  createdAt: string
}

export function listBoards() {
  return request.get<Board[]>('/boards')
}

export function getBoardBySlug(slug: string) {
  return request.get<BoardDetailResponse>(`/boards/${slug}`)
}

export function listBoardPosts(slug: string, params: { page?: number; pageSize?: number }) {
  return request.get(`/boards/${slug}/posts`, { params })
}
