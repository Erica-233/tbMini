import request from './request'

export interface RegisterData {
  email: string
  password: string
  nickname: string
}

export interface LoginData {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  user: {
    id: number
    email: string
    username: string
    nickname: string
    role: string
    avatar: string
  }
}

export function register(data: RegisterData) {
  return request.post('/auth/register', data)
}

export function login(data: LoginData) {
  return request.post<LoginResponse>('/auth/login', data)
}

export function getMe() {
  return request.get('/auth/me')
}
