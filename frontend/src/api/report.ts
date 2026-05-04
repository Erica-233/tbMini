import request from './request'

export interface ReportData {
  targetType: string
  targetId: number
  reason: string
}

export function report(data: ReportData) {
  return request.post('/reports', data)
}
