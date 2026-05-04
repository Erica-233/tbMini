<template>
  <div class="container">
    <NavBar />
    <div style="display: flex; align-items: center; justify-content: space-between">
      <h2>管理后台</h2>
      <el-button type="primary" @click="showCreateBoardDialog">新建板块</el-button>
    </div>

    <h3>待审核帖子</h3>
    <el-table :data="pendingPosts" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="userNickname" label="作者" width="120" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" type="success" @click="doApprove(scope.row.id)">通过</el-button>
          <el-button size="small" type="danger" @click="doRemove(scope.row.id)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="10"
      :total="total"
      layout="prev, pager, next"
      @current-change="loadPending"
      style="margin-top: 12px"
    />

    <h3 style="margin-top: 24px">待处理举报</h3>
    <el-table :data="reports" style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="targetType" label="类型" width="80" />
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column prop="reasonText" label="原因" />
      <el-table-column prop="reporterNickname" label="举报人" width="120" />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button
            v-if="scope.row.targetType === 'POST'"
            size="small"
            type="danger"
            @click="doRemoveReportedPost(scope.row.targetId)"
          >删除帖子</el-button>
        </template>
      </el-table-column>
    </el-table>

    <h3 style="margin-top: 24px">审计记录</h3>
    <el-table :data="actions" style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="targetType" label="类型" width="80" />
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column prop="action" label="动作" width="80" />
      <el-table-column prop="reason" label="原因" />
      <el-table-column prop="moderatorNickname" label="操作人" width="120" />
    </el-table>

    <el-dialog v-model="boardDialogVisible" title="新建板块" width="500px" @close="resetBoardForm">
      <el-form :model="boardForm" :rules="boardRules" ref="boardFormRef" label-width="80px">
        <el-form-item label="板块名称" prop="name">
          <el-input v-model="boardForm.name" placeholder="如：技术讨论" />
        </el-form-item>
        <el-form-item label="Slug" prop="slug">
          <el-input v-model="boardForm.slug" placeholder="如：tech（用于URL，仅英文/数字/连字符）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="boardForm.description" type="textarea" :rows="3" placeholder="板块描述（可选）" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="boardForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="boardDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="boardSubmitting" @click="doCreateBoard">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getPendingPosts, approvePost, removePost, getReports, getActions, createBoard } from '@/api/admin'
import type { PostVo, ReportVo, ModerationActionVo } from '@/api/admin'
import NavBar from '@/components/NavBar.vue'

const pendingPosts = ref<PostVo[]>([])
const reports = ref<ReportVo[]>([])
const actions = ref<ModerationActionVo[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)

const boardDialogVisible = ref(false)
const boardSubmitting = ref(false)
const boardFormRef = ref<FormInstance>()

const boardForm = reactive({
  name: '',
  slug: '',
  description: '',
  sortOrder: 0
})

const boardRules: FormRules = {
  name: [{ required: true, message: '请输入板块名称', trigger: 'blur' }],
  slug: [
    { required: true, message: '请输入Slug', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9-]+$/, message: 'Slug仅支持英文、数字和连字符', trigger: 'blur' }
  ]
}

function showCreateBoardDialog() {
  boardDialogVisible.value = true
}

function resetBoardForm() {
  boardForm.name = ''
  boardForm.slug = ''
  boardForm.description = ''
  boardForm.sortOrder = 0
  boardFormRef.value?.clearValidate()
}

async function doCreateBoard() {
  const valid = await boardFormRef.value?.validate().catch(() => false)
  if (!valid) return
  boardSubmitting.value = true
  try {
    await createBoard({
      name: boardForm.name,
      slug: boardForm.slug,
      description: boardForm.description || undefined,
      sortOrder: boardForm.sortOrder
    })
    ElMessage.success('板块创建成功')
    boardDialogVisible.value = false
  } finally {
    boardSubmitting.value = false
  }
}

async function loadPending() {
  loading.value = true
  try {
    const pres: any = await getPendingPosts({ page: page.value, size: 10 })
    pendingPosts.value = pres.data?.records || []
    total.value = pres.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadReports() {
  const rres: any = await getReports()
  reports.value = rres.data || []
}

async function loadActions() {
  const ares: any = await getActions()
  actions.value = ares.data || []
}

async function doApprove(id: number) {
  await approvePost(id)
  ElMessage.success('已通过')
  loadPending()
}

async function doRemove(id: number) {
  await removePost(id, '管理员移除')
  ElMessage.success('已移除')
  loadPending()
}

async function doRemoveReportedPost(targetId: number) {
  try {
    await ElMessageBox.confirm('确定要删除该被举报的帖子吗？', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removePost(targetId, '管理员删除被举报帖子')
    ElMessage.success('帖子已删除')
    loadPending()
    loadReports()
    loadActions()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadPending()
  loadReports()
  loadActions()
})
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
}
</style>
