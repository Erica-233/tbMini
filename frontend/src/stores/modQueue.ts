import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getPendingPosts, approvePost, removePost, getReports, getActions } from '@/api/admin'
import type { PostVo, ReportVo, ModerationActionVo } from '@/api/admin'

export const useModQueueStore = defineStore('modQueue', () => {
  const pendingPosts = ref<PostVo[]>([])
  const reports = ref<ReportVo[]>([])
  const actions = ref<ModerationActionVo[]>([])
  const total = ref(0)

  async function fetchPendingPosts(page = 1, size = 10) {
    const res: any = await getPendingPosts({ page, size })
    pendingPosts.value = res.data?.records || []
    total.value = res.data?.total || 0
  }

  async function fetchReports() {
    const res: any = await getReports()
    reports.value = res.data || []
  }

  async function fetchActions() {
    const res: any = await getActions()
    actions.value = res.data || []
  }

  async function approve(id: number, reason?: string) {
    await approvePost(id, reason)
    await fetchPendingPosts()
  }

  async function remove(id: number, reason?: string) {
    await removePost(id, reason)
    await fetchPendingPosts()
  }

  return { pendingPosts, reports, actions, total, fetchPendingPosts, fetchReports, fetchActions, approve, remove }
})
