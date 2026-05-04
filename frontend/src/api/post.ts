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

export interface PostDetailResponse {
  id: number
  boardId: number
  boardName: string
  userId: number
  userNickname: string
  userAvatar: string
  title: string
  bodyMd: string
  status: string
  likeCount: number
  liked: boolean
  createdAt: string
  updatedAt: string
  images: string[]
}

export interface PostListItemVo {
  id: number
  boardId: number
  boardName: string
  userId: number
  userNickname: string
  userAvatar: string
  title: string
  summary: string
  imageUrl: string
  likeCount: number
  commentCount: number
  createdAt: string
}

export function listPosts(params: { boardId?: number; page?: number; size?: number }) {
  return request.get('/posts', { params })
}

export function getPost(id: number) {
  return request.get<PostDetailResponse>(`/posts/${id}`)
}

export function createPost(data: FormData) {
  return request.post<PostDetailResponse>('/posts', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function likePost(id: number) {
  return request.post(`/posts/${id}/like`)
}

export function unlikePost(id: number) {
  return request.delete(`/posts/${id}/like`)
}

export function deletePost(id: number) {
  return request.delete(`/posts/${id}`)
}
