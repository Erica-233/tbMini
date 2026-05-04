import request from './request'

export interface PostVo {
  id: number
  boardId: number
  boardName: string
  userId: number
  userNickname: string
  userAvatar: string
  title: string
  content: string
  imageUrl: string
  status: string
  likeCount: number
  liked: boolean
  createdAt: string
}

export interface ReportVo {
  id: number
  reporterId: number
  reporterNickname: string
  targetType: string
  targetId: number
  reasonCode: string
  reasonText: string
  status: string
  createdAt: string
  resolvedAt: string
}

export interface ModerationActionVo {
  id: number
  moderatorId: number
  moderatorNickname: string
  targetType: string
  targetId: number
  action: string
  reason: string
  createdAt: string
}

export function getPendingPosts(params?: { page?: number; size?: number }) {
  return request.get('/admin/mod-queue/posts', { params })
}

export function approvePost(id: number, reason?: string) {
  return request.post(`/admin/posts/${id}/approve`, null, { params: { reason } })
}

export function removePost(id: number, reason?: string) {
  return request.post(`/admin/posts/${id}/remove`, null, { params: { reason } })
}

export function getReports() {
  return request.get<ReportVo[]>('/admin/reports')
}

export function getActions() {
  return request.get<ModerationActionVo[]>('/admin/actions')
}

export interface BoardCreateRequest {
  name: string
  slug: string
  description?: string
  sortOrder?: number
}

export function createBoard(data: BoardCreateRequest) {
  return request.post('/admin/boards', data)
}
