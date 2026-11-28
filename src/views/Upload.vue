<template>
  <div class="ai-workshop">
    <div class="header-text">
      <h1>哈基米 AI 翻唱工坊</h1>
      <p>上传普通音乐，利用 RVC 模型一键转换为专属哈基米音色</p>
    </div>

    <div class="glass-panel">
      <el-tabs v-model="activeTab">

        <!-- 普通上传 Tab -->
        <el-tab-pane label="音乐上传" name="normal">
          <el-form
            label-width="100px"
            style="max-width: 600px; margin: 0 auto; padding-top: 20px;"
          >
            <!-- 歌曲名称 -->
            <el-form-item label="歌曲名称">
              <el-input
                v-model="form.title"
                placeholder="请输入歌曲名"
                maxlength="100"
              />
            </el-form-item>

            <!-- 歌手 -->
            <el-form-item label="歌手">
              <el-input
                v-model="form.artist"
                placeholder="请输入歌手名"
                maxlength="100"
              />
            </el-form-item>

            <!-- 音频文件 -->
            <el-form-item label="音频文件">
              <el-upload
                action="#"
                :auto-upload="false"
                :on-change="handleNormalFile"
                :on-remove="handleNormalFileRemove"
                accept="audio/*"
                :limit="1"
              >
                <el-button type="primary" round>
                  选择本地音频文件
                </el-button>
                <template #tip>
                  <div class="upload-tip">
                    支持 MP3、WAV 等常见音频格式
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <!-- 封面上传 -->
            <el-form-item label="歌曲封面">
              <div class="upload-field">
                <el-upload
                  action="#"
                  :auto-upload="false"
                  :on-change="handleCoverChange"
                  :on-remove="handleCoverRemove"
                  accept="image/jpeg,image/png,image/webp"
                  :limit="1"
                  list-type="picture"
                >
                  <el-button type="primary" plain round>
                    选择封面图片
                  </el-button>

                  <template #tip>
                    <div class="upload-tip">
                      选填，支持 JPG、PNG、WEBP 格式
                    </div>
                  </template>
                </el-upload>
              </div>
            </el-form-item>

            <!-- LRC 歌词上传 -->
            <el-form-item label="歌曲歌词">
              <el-upload
                action="#"
                :auto-upload="false"
                :on-change="handleLyricChange"
                :on-remove="handleLyricRemove"
                accept=".lrc"
                :limit="1"
              >
                <el-button type="primary" plain round>
                  选择 LRC 歌词文件
                </el-button>

                <template #tip>
                  <div class="upload-tip">
                    选填，仅支持 .lrc 格式歌词文件
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <!-- 歌曲分类 -->
            <el-form-item label="歌曲分类">
              <el-select
                v-model="form.categoryId"
                placeholder="请选择歌曲分类"
                style="width: 100%;"
              >
                <el-option label="人工调音" :value="1" />
                <el-option label="AI 翻唱" :value="2" />
              </el-select>
            </el-form-item>

            <!-- 提交 -->
            <el-form-item>
              <el-button
                type="success"
                round
                size="large"
                style="width: 100%;"
                :loading="normalLoading"
                @click="submitNormal"
              >
                提交上传
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- RVC AI 翻唱 Tab -->
        <el-tab-pane label="RVC AI 哈基米翻唱" name="rvc">
          <div class="rvc-box" v-if="!taskId">

            <!-- 上传区域 -->
            <div class="upload-zone" :class="{ 'has-file': fileName }">
              <input
                type="file"
                id="file-input"
                :key="rvcInputKey"
                class="file-input"
                @change="handleFileChange"
                accept="audio/*"
              />

              <label for="file-input" class="upload-label">
                <div class="icon-wrapper">
                  <el-icon size="48">
                    <Microphone />
                  </el-icon>
                </div>

                <div v-if="!fileName" class="upload-text">
                  <h3>点击上传音频文件</h3>
                  <p>支持 MP3、WAV 格式，不超过 50MB</p>
                </div>

                <div v-else class="file-info">
                  <h3>{{ fileName }}</h3>
                  <p>文件已就绪，点击可重新选择</p>
                </div>
              </label>
            </div>

            <!-- 配置区 -->
            <div class="config-section">
              <div class="config-item">
                <label>选择哈基米音色模型</label>

                <el-select
                  v-model="rvcModel"
                  size="large"
                  class="custom-select"
                  placeholder="请选择音色"
                  style="width: 100%;"
                >
                  <el-option
                    v-for="v in voices"
                    :key="v.voice"
                    :label="`🎤 ${v.voice} (训练步数: ${v.step})`"
                    :value="v.voice"
                  />
                </el-select>
              </div>
            </div>

            <!-- 高级参数 -->
            <el-collapse style="margin-top: 20px;">
              <el-collapse-item
                title="高级参数调节（选填）"
                name="1"
              >
                <div class="advanced-grid">
                  <div>
                    <span>音调转换(半音)：</span>
                    <el-input-number
                      v-model="advanced.transpose"
                      :min="-12"
                      :max="12"
                    />
                    <small>根据音域需要调整</small>
                  </div>

                  <div>
                    <span>相似度：</span>
                    <el-slider
                      v-model="advanced.indexRate"
                      :min="0.3"
                      :max="0.95"
                      :step="0.05"
                    />
                  </div>

                  <div>
                    <span>气声保护：</span>
                    <el-slider
                      v-model="advanced.protect"
                      :min="0"
                      :max="1"
                      :step="0.05"
                    />
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>

            <!-- 版权确认 -->
            <div class="auth-checkbox">
              <el-checkbox v-model="authorized">
                我确认拥有该音频的使用权或已获得授权
              </el-checkbox>
            </div>

            <!-- 提交按钮 -->
            <button
              class="generate-btn"
              :disabled="!canSubmit"
              @click="startRvc"
            >
              <span v-if="!rvcLoading">开始转换</span>

              <span v-else class="loading-text">
                <el-icon class="is-loading">
                  <Loading />
                </el-icon>
                AI 奋力翻唱中...
              </span>
            </button>
          </div>

          <!-- 进度与结果 -->
          <transition name="fade">
            <div v-if="taskId" class="result-panel">
              <h3>AI 转换进度</h3>

              <el-progress
                :percentage="totalProgress"
                :color="customColors"
                :stroke-width="12"
                striped
                striped-flow
              />

              <p class="status-text">
                {{ statusText }}

                <span v-if="taskElapsed">
                  （已用时 {{ Math.round(taskElapsed) }} 秒）
                </span>
              </p>

              <!-- 成功结果 -->
              <div
                v-if="rvcResultUrl"
                class="audio-player-wrapper"
              >
                <h3>🎉 生成成功！</h3>

                <audio
                  :src="rvcResultUrl"
                  controls
                  class="result-audio"
                ></audio>

                <div class="actions">
                  <el-button
                    type="success"
                    size="large"
                    round
                    @click="saveToMyList"
                  >
                    保存至我的音乐库
                  </el-button>

                  <a
                    :href="rvcResultUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    download
                  >
                    <el-button size="large" round>
                      下载成品
                    </el-button>
                  </a>

                  <el-button
                    type="info"
                    size="large"
                    round
                    @click="resetRvc"
                  >
                    再弄一首
                  </el-button>
                </div>
              </div>

              <!-- 失败状态 -->
              <div v-if="rvcError" style="margin-top: 20px;">
                <el-alert
                  :title="rvcError"
                  type="error"
                  show-icon
                />

                <el-button
                  type="info"
                  round
                  @click="resetRvc"
                  style="margin-top: 15px;"
                >
                  重新尝试
                </el-button>
              </div>
            </div>
          </transition>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  computed,
  onMounted,
  onUnmounted
} from 'vue'

import { ElMessage } from 'element-plus'
import {
  Microphone,
  Loading
} from '@element-plus/icons-vue'

import { uploadSongApi } from '@/api/song'

import {
  listVoicesApi,
  createCoverApi,
  getTaskApi
} from '@/api/aiCover'

// ==========================================
// 页面状态
// ==========================================

const activeTab = ref('rvc')
const normalLoading = ref(false)

const customColors = [
  { color: '#8b5cf6', percentage: 20 },
  { color: '#d946ef', percentage: 40 },
  { color: '#ec4899', percentage: 60 },
  { color: '#f43f5e', percentage: 80 },
  { color: '#10b981', percentage: 100 }
]

// ==========================================
// 1. 普通音乐上传
// ==========================================

const form = reactive({
  title: '',
  artist: '',
  categoryId: 1
})

// 音频文件
const normalFile = ref(null)

// 封面文件
const coverFile = ref(null)

// 歌词文本
const lyricText = ref('')

// 音频文件选择
const handleNormalFile = (uploadFile) => {
  const file = uploadFile.raw

  if (!file) return

  if (!file.type.startsWith('audio/')) {
    ElMessage.error('请选择有效的音频文件')
    normalFile.value = null
    return
  }

  normalFile.value = file
}

// 删除音频文件
const handleNormalFileRemove = () => {
  normalFile.value = null
}

// 封面文件选择
const handleCoverChange = (uploadFile) => {
  const file = uploadFile.raw

  if (!file) return

  const allowedTypes = [
    'image/jpeg',
    'image/png',
    'image/webp'
  ]

  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('封面仅支持 JPG、PNG、WEBP 格式')
    coverFile.value = null
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('封面图片不能超过 5MB')
    coverFile.value = null
    return
  }

  coverFile.value = file
}

// 删除封面
const handleCoverRemove = () => {
  coverFile.value = null
}

// LRC 歌词文件选择
const handleLyricChange = async (uploadFile) => {
  const file = uploadFile.raw

  if (!file) return

  if (!file.name.toLowerCase().endsWith('.lrc')) {
    ElMessage.error('请选择 LRC 格式的歌词文件')
    lyricText.value = ''
    return
  }

  try {
    const text = await file.text()

    if (!text.trim()) {
      ElMessage.warning('歌词文件内容为空')
      lyricText.value = ''
      return
    }

    lyricText.value = text

    ElMessage.success('歌词读取成功')
  } catch (error) {
    console.error('歌词读取失败：', error)
    ElMessage.error('歌词文件读取失败')
    lyricText.value = ''
  }
}

// 删除歌词
const handleLyricRemove = () => {
  lyricText.value = ''
}

// 提交普通音乐
const submitNormal = async () => {
  if (!form.title.trim() || !form.artist.trim()) {
    return ElMessage.warning('请填写歌曲名称和歌手')
  }

  if (!normalFile.value) {
    return ElMessage.warning('请选择音频文件')
  }

  if (![1, 2].includes(form.categoryId)) {
    return ElMessage.warning('请选择歌曲分类')
  }

  const formData = new FormData()

  // 必填字段
  formData.append('audio', normalFile.value)
  formData.append('title', form.title.trim())
  formData.append('artist', form.artist.trim())
  formData.append('categoryId', form.categoryId)

  // 选填字段：封面
  if (coverFile.value) {
    formData.append('cover', coverFile.value)
  }

  // 选填字段：歌词
  if (lyricText.value.trim()) {
    formData.append('lyric', lyricText.value)
  }

  normalLoading.value = true

  try {
    await uploadSongApi(formData)

    ElMessage.success('上传成功！')

    // 清空表单
    form.title = ''
    form.artist = ''
    form.categoryId = 1

    normalFile.value = null
    coverFile.value = null
    lyricText.value = ''

    // 清空 Element Plus 上传组件中的文件列表
    normalUploadKey.value++
  } catch (error) {
    console.error('歌曲上传失败：', error)
  } finally {
    normalLoading.value = false
  }
}

// 通过 key 重置上传组件
const normalUploadKey = ref(0)

// ==========================================
// 2. RVC AI 转换逻辑
// ==========================================

const voices = ref([])
const rvcModel = ref('')
const rvcFile = ref(null)
const fileName = ref('')
const authorized = ref(false)

const rvcInputKey = ref(0)

const advanced = reactive({
  transpose: 0,
  indexRate: 0.75,
  protect: 0.33
})

const taskId = ref('')
const rvcLoading = ref(false)
const taskElapsed = ref(0)
const rvcError = ref('')
const rvcResultUrl = ref('')

const processStatus = ref('')
const processStage = ref('')

const uploadPercent = ref(0)
const serverPercent = ref(0)

let pollingTimer = null
let startTime = 0

const statusMap = {
  queued: '排队中，等待 GPU 资源...',
  separating: '正在分离人声与伴奏...',
  converting: '正在提取音色特征并转换...',
  mixing: '正在混音与后期处理...',
  done: '转换完成！',
  failed: '转换失败'
}

// 状态文本
const statusText = computed(() => {
  if (uploadPercent.value < 100) {
    return `正在上传音频文件 (${uploadPercent.value}%)`
  }

  if (processStatus.value === 'FAILED') {
    return '转换失败'
  }

  return statusMap[processStage.value] || '处理中...'
})

// 总进度
const totalProgress = computed(() => {
  if (uploadPercent.value < 100) {
    return Math.round(uploadPercent.value * 0.2)
  }

  return Math.min(
    100,
    Math.round(20 + serverPercent.value * 0.8)
  )
})

// 是否允许提交
const canSubmit = computed(() => {
  return (
    rvcFile.value &&
    rvcModel.value &&
    authorized.value &&
    !rvcLoading.value
  )
})

// 获取音色列表
onMounted(async () => {
  try {
    const res = await listVoicesApi()

    voices.value = Array.isArray(res) ? res : []

    if (voices.value.length > 0) {
      rvcModel.value = voices.value[0].voice
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取音色失败，AI 服务可能暂不可用')
  }
})

// 页面卸载时清除轮询
onUnmounted(() => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
})

// 选择 RVC 音频
const handleFileChange = (e) => {
  const file = e.target.files?.[0]

  if (!file) return

  if (!file.type.startsWith('audio/')) {
    ElMessage.error('请选择有效的音频文件')
    rvcFile.value = null
    fileName.value = ''
    return
  }

  if (file.size > 50 * 1024 * 1024) {
    ElMessage.error('文件不能超过 50MB')
    rvcFile.value = null
    fileName.value = ''
    return
  }

  rvcFile.value = file
  fileName.value = file.name
}

// 开始 RVC 转换
const startRvc = async () => {
  if (!canSubmit.value) return

  rvcLoading.value = true
  rvcError.value = ''

  uploadPercent.value = 0
  serverPercent.value = 0

  startTime = Date.now()

  const formData = new FormData()

  formData.append('file', rvcFile.value)
  formData.append('voice', rvcModel.value)
  formData.append('authorized', true)
  formData.append('transpose', advanced.transpose)
  formData.append('indexRate', advanced.indexRate)
  formData.append('protect', advanced.protect)

  try {
    const res = await createCoverApi(
      formData,
      (progressEvent) => {
        if (!progressEvent.total) return

        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        )

        uploadPercent.value = Math.min(100, percentCompleted)
      }
    )

    taskId.value = res.taskId
    processStatus.value = res.status
    processStage.value = res.stage

    polling()
  } catch (error) {
    console.error(error)

    rvcLoading.value = false
    rvcError.value = error.message || '提交任务失败'
  }
}

// 查询转换进度
const polling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
  }

  pollingTimer = setInterval(async () => {
    try {
      const res = await getTaskApi(taskId.value)

      processStatus.value = res.status
      processStage.value = res.stage

      serverPercent.value = res.progress || 0

      taskElapsed.value =
        res.elapsed || ((Date.now() - startTime) / 1000)

      if (res.status === 'DONE') {
        clearInterval(pollingTimer)
        pollingTimer = null

        rvcLoading.value = false
        rvcResultUrl.value = res.resultUrl

        ElMessage.success('哈基米翻唱生成完毕！')
      } else if (res.status === 'FAILED') {
        clearInterval(pollingTimer)
        pollingTimer = null

        rvcLoading.value = false
        rvcError.value = res.error || '处理失败，请重试'
      }
    } catch (error) {
      console.error(error)

      clearInterval(pollingTimer)
      pollingTimer = null

      rvcLoading.value = false
      rvcError.value = '查询进度失败，请检查网络'
    }
  }, 3000)
}

// ==========================================
// 3. 保存 AI 翻唱到音乐库
// 分类固定为 2
// ==========================================

const saveToMyList = async () => {
  if (!rvcResultUrl.value) return

  const loadingMsg = ElMessage({
    message: '正在保存到音乐库...',
    type: 'info',
    duration: 0
  })

  try {
    // 下载生成的音频
    const response = await fetch(rvcResultUrl.value)

    if (!response.ok) {
      throw new Error('获取生成音频失败')
    }

    const blob = await response.blob()

    const file = new File(
      [blob],
      `AI翻唱_${Date.now()}.mp3`,
      { type: 'audio/mpeg' }
    )

    // 构建 FormData
    const formData = new FormData()

    formData.append('audio', file)

    formData.append(
      'title',
      `AI翻唱 - ${fileName.value || '哈基米之歌'}`
    )

    formData.append('artist', '哈基米 AI')
    formData.append('categoryId', 2)

    // 上传
    await uploadSongApi(formData)

    loadingMsg.close()

    ElMessage.success(
      '已成功保存到您的音乐库！快去“我的上传”看看吧'
    )
  } catch (error) {
    console.error(error)

    loadingMsg.close()

    ElMessage.error(
      '保存失败，可能是跨域限制，请直接点击上方下载按钮'
    )
  }
}

// ==========================================
// 4. 重置 RVC 页面
// ==========================================

const resetRvc = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }

  taskId.value = ''

  rvcFile.value = null
  fileName.value = ''

  authorized.value = false
  rvcLoading.value = false

  rvcResultUrl.value = ''
  rvcError.value = ''

  uploadPercent.value = 0
  serverPercent.value = 0
  taskElapsed.value = 0

  processStatus.value = ''
  processStage.value = ''

  rvcInputKey.value++
}
</script>

<style scoped>
.ai-workshop {
  max-width: 900px;
  margin: 0 auto;
}

.header-text {
  text-align: center;
  margin-bottom: 40px;
}

.header-text h1 {
  font-size: 36px;
  font-weight: 800;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 12px;
}

.header-text p {
  color: var(--text-muted);
  font-size: 16px;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #e2e8f0;
}

.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}

.upload-field {
  width: 100%;
}

.rvc-box {
  text-align: center;
  padding: 20px 0;
}

.upload-zone {
  position: relative;
  border: 2px dashed #cbd5e1;
  border-radius: 16px;
  background: #f8fafc;
  transition: all 0.3s ease;
  overflow: hidden;
}

.upload-zone:hover {
  border-color: #8b5cf6;
  background: #f5f3ff;
}

.upload-zone.has-file {
  border-color: #10b981;
  background: #ecfdf5;
}

.file-input {
  display: none;
}

.upload-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  cursor: pointer;
}

.icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: white;
  color: #8b5cf6;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 20px rgba(139, 92, 246, 0.1);
  margin-bottom: 20px;
}

.has-file .icon-wrapper {
  color: #10b981;
  box-shadow: 0 10px 20px rgba(16, 185, 129, 0.1);
}

.upload-text h3,
.file-info h3 {
  font-size: 18px;
  margin: 0 0 8px 0;
  color: var(--text-main);
  word-break: break-all;
}

.upload-text p,
.file-info p {
  color: var(--text-muted);
  margin: 0;
  font-size: 14px;
}

.config-section {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.config-item {
  text-align: left;
}

.config-item label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-main);
}

:deep(.custom-select .el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.advanced-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  text-align: left;
}

.advanced-grid span {
  display: inline-block;
  width: 140px;
  color: var(--text-main);
}

.advanced-grid small {
  color: #888;
  margin-left: 10px;
}

.auth-checkbox {
  margin-top: 24px;
  text-align: left;
}

:deep(.el-checkbox__label) {
  color: var(--text-main);
  font-weight: 500;
}

.generate-btn {
  width: 100%;
  margin-top: 24px;
  height: 54px;
  border-radius: 12px;
  border: none;
  background: var(--primary-gradient);
  color: white;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 20px rgba(236, 72, 153, 0.3);
}

.generate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 25px rgba(236, 72, 153, 0.4);
}

.generate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.result-panel {
  margin-top: 20px;
  background: white;
  padding: 40px;
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  text-align: center;
}

.result-panel h3 {
  margin-top: 0;
  color: var(--text-main);
}

.status-text {
  font-size: 15px;
  color: #8b5cf6;
  margin-top: 16px;
  font-weight: 500;
}

.audio-player-wrapper {
  margin-top: 30px;
  padding-top: 30px;
  border-top: 1px dashed #e2e8f0;
}

.result-audio {
  width: 100%;
  margin: 20px 0;
  border-radius: 50px;
  outline: none;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.actions a {
  text-decoration: none;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>