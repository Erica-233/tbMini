import request from './request'

export interface UserProfile {
  id: number
  username: string
  nickname: string
  avatar: string
  bio: string
  createdAt: string
}

export interface UpdateProfileData {
  nickname: string
  avatar?: string
}

export function getUserProfile(username: string) {
  return request.get<UserProfile>(`/users/${username}`)
}

export function updateProfile(data: UpdateProfileData) {
  return request.put('/users/me', data)
}

export function getUserPosts(username: string, params: { page?: number; size?: number }) {
  return request.get(`/users/${username}/posts`, { params })
}

export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<{ url: string }>('/upload/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
