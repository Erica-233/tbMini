<template>
  <div class="image-uploader">
    <div class="preview-list">
      <div v-for="(file, idx) in modelValue" :key="idx" class="preview-item">
        <el-image :src="previewUrl(file)" fit="cover" style="width: 100px; height: 100px; border-radius: 8px" />
        <el-icon class="remove-btn" @click.stop="remove(idx)"><Close /></el-icon>
      </div>
      <div v-if="modelValue.length < max" class="upload-trigger" @click="triggerFile">
        <el-icon :size="28"><Plus /></el-icon>
        <div class="tip">上传图片</div>
      </div>
    </div>
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      multiple
      style="display: none"
      @change="onFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

interface Props {
  modelValue: File[]
  max?: number
}

const props = withDefaults(defineProps<Props>(), {
  max: 9
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: File[]): void
}>()

const fileInput = ref<HTMLInputElement | null>(null)

function triggerFile() {
  fileInput.value?.click()
}

function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const files = target.files
  if (!files) return

  const arr = [...props.modelValue]
  for (let i = 0; i < files.length; i++) {
    if (arr.length >= props.max) {
      ElMessage.warning(`最多上传 ${props.max} 张图片`)
      break
    }
    if (!files[i].type.startsWith('image/')) {
      ElMessage.warning('只能上传图片文件')
      continue
    }
    arr.push(files[i])
  }
  emit('update:modelValue', arr)
  if (fileInput.value) fileInput.value.value = ''
}

function remove(idx: number) {
  const arr = [...props.modelValue]
  arr.splice(idx, 1)
  emit('update:modelValue', arr)
}

function previewUrl(file: File) {
  return URL.createObjectURL(file)
}
</script>

<style scoped>
.image-uploader {
  width: 100%;
}
.preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.preview-item {
  position: relative;
}
.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.upload-trigger {
  width: 100px;
  height: 100px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  cursor: pointer;
  transition: border-color 0.2s;
}
.upload-trigger:hover {
  border-color: #409eff;
  color: #409eff;
}
.tip {
  font-size: 12px;
  margin-top: 4px;
}
</style>
