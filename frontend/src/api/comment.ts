import request from './request'

export interface CommentVo {
  id: number
  postId: number
  userId: number
  userNickname: string
  userAvatar: string
  parentId: number | null
  bodyMd: string
  status: string
  removed: boolean
  createdAt: string
  children: CommentVo[]
}

export interface CreateCommentData {
  postId: number
  parentId?: number
  bodyMd: string
}

export function listComments(postId: number) {
  return request.get<CommentVo[]>(`/posts/${postId}/comments`)
}

export function createComment(data: CreateCommentData) {
  return request.post<CommentVo>('/comments', data)
}
