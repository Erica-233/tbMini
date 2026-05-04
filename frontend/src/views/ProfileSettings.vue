<template>
  <div class="container">
    <NavBar />
    <el-card style="max-width: 600px; margin: 0 auto">
      <h2 style="text-align: center; margin-bottom: 24px">账号设置</h2>
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="头像">
          <div class="avatar-uploader">
            <el-avatar v-if="previewAvatar" :src="previewAvatar" :size="100" />
            <el-avatar v-else :size="100">{{ form.nickname?.[0] }}</el-avatar>
            <el-upload
              class="upload-btn"
              action=""
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
              accept="image/png,image/jpeg,image/webp"
            >
              <el-button type="primary" plain>更换头像</el-button>
            </el-upload>
          </div>
          <div class="tip">支持 png/jpg/webp，大小不超过 2MB</div>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="2-20个字符，支持中文/英文/数字/下划线" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSave" :loading="saving">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { updateProfile, uploadAvatar } from '@/api/user'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const saving = ref(false)
const previewAvatar = ref('')
const avatarFile = ref<File | null>(null)

const form = reactive({
  nickname: ''
})

const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
    {
      pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/,
      message: '只能包含中文、英文、数字或下划线',
      trigger: 'blur'
    }
  ]
}

function handleAvatarChange(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  if (raw.size > 2 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过 2MB')
    return
  }
  avatarFile.value = raw
  previewAvatar.value = URL.createObjectURL(raw)
}

async function onSave() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      let avatarUrl = userStore.user?.avatar || ''
      if (avatarFile.value) {
        const res: any = await uploadAvatar(avatarFile.value)
        avatarUrl = res.data?.url || avatarUrl
        previewAvatar.value = avatarUrl
      }
      await updateProfile({ nickname: form.nickname, avatar: avatarUrl || undefined })
      ElMessage.success('保存成功')
      if (userStore.user) {
        userStore.user.nickname = form.nickname
        userStore.user.avatar = avatarUrl
        localStorage.setItem('user', JSON.stringify(userStore.user))
      }
    } catch {
      // handled by interceptor
    } finally {
      saving.value = false
    }
  })
}

onMounted(() => {
  if (userStore.user) {
    form.nickname = userStore.user.nickname
    previewAvatar.value = userStore.user.avatar || ''
  }
})
</script>

<style scoped>
.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 16px;
}
.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 16px;
}
.tip {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>
