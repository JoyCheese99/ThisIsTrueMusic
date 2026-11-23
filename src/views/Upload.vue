<template>
  <div class="ai-workshop">
    <div class="header-text">
      <h1>哈基米 AI 翻唱工坊</h1>
      <p>上传普通音乐，利用 RVC 模型一键转换为专属哈基米音色</p>
    </div>

    <div class="glass-panel">
      <el-tabs v-model="activeTab">
        
        <!-- 普通上传 Tab (已对接真实接口) -->
        <el-tab-pane label="普通音乐上传" name="normal">
          <el-form label-width="100px" style="max-width: 600px; margin: 0 auto; padding-top: 20px;">
            <el-form-item label="歌曲名称">
              <el-input v-model="form.title" placeholder="请输入歌曲名" />
            </el-form-item>
            <el-form-item label="歌手">
              <el-input v-model="form.artist" placeholder="请输入歌手名" />
            </el-form-item>
            <el-form-item label="音频文件">
              <el-upload 
                action="#" 
                :auto-upload="false" 
                :on-change="handleNormalFile" 
                accept="audio/*"
                :limit="1"
              >
                <el-button type="primary" round>选择本地 MP3 文件</el-button>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="success" round size="large" style="width: 100%;" @click="submitNormal">
                提交上传
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="RVC AI 哈基米翻唱" name="rvc">
          <div class="rvc-box" v-if="!taskId">
            <!-- 上传区域 -->
            <div class="upload-zone" :class="{ 'has-file': fileName }">
              <input type="file" id="file-input" class="file-input" @change="handleFileChange" accept="audio/*" />
              <label for="file-input" class="upload-label">
                <div class="icon-wrapper">
                  <el-icon size="48"><Microphone /></el-icon>
                </div>
                <div v-if="!fileName" class="upload-text">
                  <h3>点击上传音频文件</h3>
                  <p>支持 MP3, WAV 格式, 不超过50MB</p>
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
                <el-select v-model="rvcModel" size="large" class="custom-select" placeholder="请选择音色">
                  <el-option 
                    v-for="v in voices" 
                    :key="v.voice" 
                    :label="`🎤 ${v.voice} (训练步数: ${v.step})`" 
                    :value="v.voice" 
                  />
                </el-select>
              </div>
            </div>

            <!-- 高级参数 (折叠面板) -->
            <el-collapse style="margin-top: 20px;">
              <el-collapse-item title="高级参数调节（选填）" name="1">
                <div class="advanced-grid">
                  <div>
                    <span>音调转换(半音)：</span>
                    <el-input-number v-model="advanced.transpose" :min="-12" :max="12" />
                    <small>男转女建议 +12</small>
                  </div>
                  <div>
                    <span>相似度 (越高越像)：</span>
                    <el-slider v-model="advanced.indexRate" :min="0.3" :max="0.95" :step="0.05" />
                  </div>
                  <div>
                    <span>气声保护 (防破音)：</span>
                    <el-slider v-model="advanced.protect" :min="0" :max="1" :step="0.05" />
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
            <button class="generate-btn" :disabled="!canSubmit" @click="startRvc">
              <span v-if="!rvcLoading">开始转换</span>
              <span v-else class="loading-text">
                <el-icon class="is-loading"><Loading /></el-icon> AI 奋力翻唱中...
              </span>
            </button>
          </div>

          <!-- 进度与结果 -->
          <transition name="fade">
            <div v-if="taskId" class="result-panel">
              <h3>AI 转换进度</h3>
              
              <!-- 进度分两段：上传进度 + 处理进度 -->
              <el-progress 
                :percentage="totalProgress" 
                :color="customColors" 
                :stroke-width="12"
                striped striped-flow
              />
              <p class="status-text">
                {{ statusText }} 
                <span v-if="taskElapsed">(已用时 {{ Math.round(taskElapsed) }} 秒)</span>
              </p>

              <div v-if="rvcResultUrl" class="audio-player-wrapper">
                <h3>🎉 生成成功！</h3>
                <audio :src="rvcResultUrl" controls class="result-audio"></audio>
                <div class="actions">
                  <a :href="rvcResultUrl" target="_blank" download>
                    <el-button type="success" size="large" round>下载成品</el-button>
                  </a>
                  <a :href="`/api/ai-cover/${taskId}/file`" target="_blank">
                    <el-button type="primary" size="large" round>长期分享链接</el-button>
                  </a>
                  <el-button type="info" size="large" round @click="resetRvc">再弄一首</el-button>
                </div>
              </div>

              <!-- 失败状态 -->
              <div v-if="rvcError" style="margin-top: 20px;">
                <el-alert :title="rvcError" type="error" show-icon />
                <el-button type="info" round @click="resetRvc" style="margin-top: 15px;">重新尝试</el-button>
              </div>
            </div>
          </transition>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone, Loading } from '@element-plus/icons-vue'
import { uploadSongApi } from '@/api/song' // 引入真实上传接口
import { listVoicesApi, createCoverApi, getTaskApi } from '@/api/aiCover'

const activeTab = ref('rvc')
const customColors = [
  { color: '#8b5cf6', percentage: 20 }, { color: '#d946ef', percentage: 40 },
  { color: '#ec4899', percentage: 60 }, { color: '#f43f5e', percentage: 80 },
  { color: '#10b981', percentage: 100 }
]
// 1. 普通上传逻辑 (对接真实接口)
const form = reactive({ title: '', artist: '' })
const normalFile = ref(null)

const handleNormalFile = (file) => {
  normalFile.value = file.raw
}

const submitNormal = async () => {
  if (!form.title || !form.artist) return ElMessage.warning('请填写歌曲名称和歌手')
  if (!normalFile.value) return ElMessage.warning('请选择音频文件')

  const formData = new FormData()
  formData.append('audio', normalFile.value)
  formData.append('title', form.title)
  formData.append('artist', form.artist)
  formData.append('categoryId', 1) // 文档要求必传，这里先固定为 1
  
  try {
    await uploadSongApi(formData)
    ElMessage.success('上传成功！')
    form.title = ''
    form.artist = ''
    normalFile.value = null
  } catch (error) {
    console.error(error)
  }
}
// 2. RVC AI 转换逻辑
const voices = ref([])
const rvcModel = ref('')
const rvcFile = ref(null)
const fileName = ref('')
const authorized = ref(false)

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
const processStatus = ref('') // PENDING, RUNNING, DONE, FAILED
const processStage = ref('')  // queued, separating, converting, mixing, done
const uploadPercent = ref(0)  // 0-100
const serverPercent = ref(0)  // 0-100

let pollingTimer = null
let startTime = 0

const statusMap = {
  queued: '排队中，等待 GPU 资源...',
  separating: '正在分离人声与伴奏...',
  converting: '正在提取哈基米音色特征并转换...',
  mixing: '正在混音与后期处理...',
  done: '转换完成！',
  failed: '转换失败'
}

const statusText = computed(() => {
  if (uploadPercent.value < 100) return `正在上传音频文件 (${uploadPercent.value}%)`
  if (processStatus.value === 'FAILED') return '转换失败'
  return statusMap[processStage.value] || '处理中...'
})

// 进度计算：上传占 20%，处理占 80%
const totalProgress = computed(() => {
  if (uploadPercent.value < 100) return uploadPercent.value * 0.2
  return 20 + (serverPercent.value * 0.8)
})

const canSubmit = computed(() => {
  return rvcFile.value && rvcModel.value && authorized.value && !rvcLoading.value
})

onMounted(async () => {
  try {
    voices.value = await listVoicesApi()
    if (voices.value.length > 0) rvcModel.value = voices.value[0].voice
  } catch (error) {
    ElMessage.error('获取音色失败，AI 服务可能暂不可用')
  }
})

onUnmounted(() => {
  if (pollingTimer) clearInterval(pollingTimer)
})

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 50 * 1024 * 1024) {
    return ElMessage.error('文件不能超过 50MB')
  }
  rvcFile.value = file
  fileName.value = file.name
}

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
    const res = await createCoverApi(formData, (progressEvent) => {
      const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      uploadPercent.value = percentCompleted
    })
    
    // 上传成功，开始轮询
    taskId.value = res.taskId
    processStatus.value = res.status
    processStage.value = res.stage
    polling()
    
  } catch (error) {
    rvcLoading.value = false
    rvcError.value = error.message || '提交任务失败'
  }
}

const polling = () => {
  if (pollingTimer) clearInterval(pollingTimer)
  
  pollingTimer = setInterval(async () => {
    try {
      const res = await getTaskApi(taskId.value)
      
      processStatus.value = res.status
      processStage.value = res.stage
      serverPercent.value = res.progress || 0
      taskElapsed.value = res.elapsed || ((Date.now() - startTime) / 1000)
      
      if (res.status === 'DONE') {
        clearInterval(pollingTimer)
        rvcLoading.value = false
        rvcResultUrl.value = res.resultUrl // 给 audio 标签用
        ElMessage.success('哈基米翻唱生成完毕！')
      } else if (res.status === 'FAILED') {
        clearInterval(pollingTimer)
        rvcLoading.value = false
        rvcError.value = res.error || '处理失败，请重试'
      }
    } catch (error) {
      clearInterval(pollingTimer)
      rvcLoading.value = false
      rvcError.value = '查询进度失败，请检查网络'
    }
  }, 3000)
}

const resetRvc = () => {
  if (pollingTimer) clearInterval(pollingTimer)
  taskId.value = ''
  rvcFile.value = null
  fileName.value = ''
  authorized.value = false
  rvcLoading.value = false
  rvcResultUrl.value = ''
  uploadPercent.value = 0
  serverPercent.value = 0
  taskElapsed.value = 0
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
.header-text p { color: var(--text-muted); font-size: 16px; }

.glass-panel {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

/* --- 普通上传样式微调 --- */
:deep(.el-tabs__item) { font-size: 16px; font-weight: 600; }
:deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #e2e8f0; }

/* --- RVC 区域样式 --- */
.rvc-box { text-align: center; padding: 20px 0; }

.upload-zone {
  position: relative;
  border: 2px dashed #cbd5e1;
  border-radius: 16px;
  background: #f8fafc;
  transition: all 0.3s ease;
  overflow: hidden;
}
.upload-zone:hover { border-color: #8b5cf6; background: #f5f3ff; }
.upload-zone.has-file { border-color: #10b981; background: #ecfdf5; }

.file-input { display: none; }
.upload-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  cursor: pointer;
}
.icon-wrapper {
  width: 80px; height: 80px; border-radius: 50%;
  background: white; color: #8b5cf6;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 10px 20px rgba(139, 92, 246, 0.1);
  margin-bottom: 20px;
}
.has-file .icon-wrapper { color: #10b981; box-shadow: 0 10px 20px rgba(16, 185, 129, 0.1); }
.upload-text h3, .file-info h3 { font-size: 18px; margin: 0 0 8px 0; color: var(--text-main); }
.upload-text p, .file-info p { color: var(--text-muted); margin: 0; font-size: 14px; }

.config-section {
  margin-top: 32px;
  display: flex;
  align-items: flex-end;
  gap: 24px;
}
.config-item { flex: 1; text-align: left; }
.config-item label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 8px; color: var(--text-main); }
:deep(.custom-select .el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.generate-btn {
  height: 50px;
  padding: 0 40px;
  border-radius: 12px;
  border: none;
  background: var(--primary-gradient);
  color: white;
  font-size: 16px;
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
  margin-top: 40px;
  background: white;
  padding: 32px;
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  text-align: center;
}
.result-panel h3 { margin-top: 0; color: var(--text-main); }
.status-text { font-size: 14px; color: #8b5cf6; margin-top: 12px; font-weight: 500;}

.audio-player-wrapper { margin-top: 30px; }
.result-audio { width: 100%; margin: 20px 0; border-radius: 50px; outline: none; }
.result-audio::-webkit-media-controls-panel { background-color: #f1f5f9; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.5s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.ai-workshop { max-width: 900px; margin: 0 auto; }
.header-text { text-align: center; margin-bottom: 40px; }
.header-text h1 { font-size: 36px; font-weight: 800; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 12px; }
.header-text p { color: var(--text-muted); font-size: 16px; }

.glass-panel { background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(20px); border-radius: 24px; padding: 40px; box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05); border: 1px solid rgba(255, 255, 255, 0.5); }

.rvc-box { text-align: center; padding: 20px 0; }
.upload-zone { position: relative; border: 2px dashed #cbd5e1; border-radius: 16px; background: #f8fafc; transition: all 0.3s ease; overflow: hidden; }
.upload-zone:hover { border-color: #8b5cf6; background: #f5f3ff; }
.upload-zone.has-file { border-color: #10b981; background: #ecfdf5; }
.file-input { display: none; }
.upload-label { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 20px; cursor: pointer; }
.icon-wrapper { width: 80px; height: 80px; border-radius: 50%; background: white; color: #8b5cf6; display: flex; align-items: center; justify-content: center; box-shadow: 0 10px 20px rgba(139, 92, 246, 0.1); margin-bottom: 20px; }
.has-file .icon-wrapper { color: #10b981; box-shadow: 0 10px 20px rgba(16, 185, 129, 0.1); }
.upload-text h3, .file-info h3 { font-size: 18px; margin: 0 0 8px 0; color: var(--text-main); }
.upload-text p, .file-info p { color: var(--text-muted); margin: 0; font-size: 14px; }

.config-section { margin-top: 32px; display: flex; flex-direction: column; gap: 24px; }
.config-item { text-align: left; }
.config-item label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 8px; color: var(--text-main); }
:deep(.custom-select .el-input__wrapper) { border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }

.advanced-grid { display: flex; flex-direction: column; gap: 16px; text-align: left; }
.advanced-grid span { display: inline-block; width: 140px; color: var(--text-main); }
.advanced-grid small { color: #888; margin-left: 10px; }

.auth-checkbox { margin-top: 24px; text-align: left; }
:deep(.el-checkbox__label) { color: var(--text-main); font-weight: 500; }

.generate-btn { width: 100%; margin-top: 24px; height: 54px; border-radius: 12px; border: none; background: var(--primary-gradient); color: white; font-size: 18px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; box-shadow: 0 10px 20px rgba(236, 72, 153, 0.3); }
.generate-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 15px 25px rgba(236, 72, 153, 0.4); }
.generate-btn:disabled { opacity: 0.6; cursor: not-allowed; box-shadow: none; }

.result-panel { margin-top: 20px; background: white; padding: 40px; border-radius: 24px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); text-align: center; }
.result-panel h3 { margin-top: 0; color: var(--text-main); }
.status-text { font-size: 15px; color: #8b5cf6; margin-top: 16px; font-weight: 500; }

.audio-player-wrapper { margin-top: 30px; padding-top: 30px; border-top: 1px dashed #e2e8f0; }
.result-audio { width: 100%; margin: 20px 0; border-radius: 50px; outline: none; }
.actions { display: flex; justify-content: center; gap: 12px; margin-top: 10px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.5s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>