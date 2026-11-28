<template>
  <div v-if="player.currentSong" class="global-player">
    <div class="player-inner">

      <!-- 左侧歌曲信息 -->
      <div class="song-info">
        <img
          :src="player.currentSong.coverUrl || 'https://via.placeholder.com/60'"
          class="cover"
          alt="歌曲封面"
        />

        <div class="meta">
          <div class="title">{{ player.currentSong.title }}</div>
          <div class="artist">{{ player.currentSong.artist }}</div>
        </div>
      </div>

      <!-- 中间控制区 -->
      <div class="player-controls">
        <div class="buttons">
          <el-button
            circle
            text
            bg
            :icon="ArrowLeft"
            @click="player.prev()"
            class="ctrl-btn"
          />

          <button class="play-btn" @click="toggle">
            <el-icon size="24">
              <VideoPause v-if="player.playing" />
              <VideoPlay v-else />
            </el-icon>
          </button>

          <el-button
            circle
            text
            bg
            :icon="ArrowRight"
            @click="player.next()"
            class="ctrl-btn"
          />
        </div>

        <div class="progress-bar">
          <span class="time">{{ format(player.currentTime) }}</span>

          <el-slider
  v-model="percent"
  :min="0"
  :max="100"
  :show-tooltip="false"
  @input="handleSeekInput"
  @change="handleSeekChange"
  class="custom-slider"
/>

          <span class="time">{{ format(player.duration) }}</span>
        </div>
      </div>

                <!-- 歌词入口 -->
<div class="lyric-btn" @click="player.showLyric = true">
  <el-icon size="22">
    <Operation />
  </el-icon>
  <span>歌词</span>
</div>
      <!-- 右侧音量控制 -->
      <div class="extra-actions">
        <el-popover
          placement="top"
          :width="150"
          trigger="click"
        >
          <template #reference>
            <div class="volume-icon-btn">
              <el-icon size="20" color="#a1a1aa">
                <Headset />
              </el-icon>
            </div>
          </template>

          <div class="volume-control">
            <el-icon>
              <Mute v-if="player.volume === 0" />
              <Microphone v-else />
            </el-icon>

            <!-- 音量滑块：0~100 -->
            <el-slider
              v-model="volumePercent"
              :min="0"
              :max="100"
              :step="1"
              vertical
              height="100px"
              style="margin: 10px 0;"
            />

            <span class="volume-text">
              {{ volumePercent }}%
            </span>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- 音频播放器 -->
    <audio
      ref="audioRef"
      :src="player.currentSong.audioUrl"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoaded"
      @play="reportPlay"
      @ended="player.next()"
    ></audio>
  </div>
</template>

<script setup>
import {
  ref,
  computed,
  watch,
  nextTick
} from 'vue'

import {
  VideoPlay,
  VideoPause,
  ArrowLeft,
  ArrowRight,
  Headset,
  Mute,
  Microphone,
  Operation
} from '@element-plus/icons-vue'

import { usePlayerStore } from '@/stores/player'
import { reportSongPlayApi } from '@/api/song'

const player = usePlayerStore()
const audioRef = ref(null)

// ==========================================
// 1. 播放记录上报
// ==========================================

// 记录已经上报过的歌曲 ID，避免重复上报
const reportedSongId = ref(null)

// ==========================================
// 2. 播放进度控制
// ==========================================

// 是否正在拖动进度条
const isSeeking = ref(false)

// 拖动过程中临时保存的进度
const seekingPercent = ref(0)

// 播放进度百分比
const percent = computed({
  get: () => {
    // 拖动期间优先显示拖动位置
    if (isSeeking.value) {
      return seekingPercent.value
    }

    return player.duration
      ? (player.currentTime / player.duration) * 100
      : 0
  },

  set: (value) => {
    seekingPercent.value = value
  }
})

// 开始拖动或拖动过程中
function handleSeekInput(value) {
  isSeeking.value = true
  seekingPercent.value = Number(value) || 0
}

// 松开滑块，跳转到指定位置
function handleSeekChange(value) {
  if (!audioRef.value || !player.duration) {
    isSeeking.value = false
    return
  }

  const safePercent = Math.max(
    0,
    Math.min(100, Number(value) || 0)
  )

  const targetTime = (safePercent / 100) * player.duration

  // 修改音频实际播放位置
  audioRef.value.currentTime = targetTime

  // 同步 Pinia 播放时间
  player.currentTime = targetTime

  // 解除拖动锁定
  isSeeking.value = false
}

// ==========================================
// 3. 音量控制
// ==========================================

// Pinia 内部音量范围：0~1
// 滑块显示范围：0~100
const volumePercent = computed({
  get: () => {
    return Math.round(player.volume * 100)
  },

  set: (value) => {
    const safeValue = Math.max(
      0,
      Math.min(100, Number(value) || 0)
    )

    player.volume = safeValue / 100
  }
})

// ==========================================
// 4. 监听歌曲切换
// ==========================================

watch(
  () => player.currentSong?.id,

  async (newId, oldId) => {
    if (newId === oldId) return

    // 新歌曲允许重新上报播放记录
    reportedSongId.value = null

    // 重置进度条拖动状态
    isSeeking.value = false
    seekingPercent.value = 0

    await nextTick()

    if (!audioRef.value) return

    // 重置播放进度
    audioRef.value.currentTime = 0

    player.currentTime = 0
    player.duration = 0

    // 同步音量
    audioRef.value.volume = player.volume

    // 如果当前处于播放状态，自动播放新歌曲
    if (player.playing) {
      audioRef.value.play().catch(error => {
        console.error('音频播放失败：', error)
        player.playing = false
      })
    }
  }
)

// ==========================================
// 5. 监听播放状态
// ==========================================

watch(
  () => player.playing,

  (val) => {
    if (!audioRef.value) return

    if (val) {
      audioRef.value.play().catch(error => {
        console.error('音频播放失败：', error)
        player.playing = false
      })
    } else {
      audioRef.value.pause()
    }
  }
)

// ==========================================
// 6. 监听音量变化
// ==========================================

watch(
  () => player.volume,

  (val) => {
    if (!audioRef.value) return

    // 限制音量在 0~1 之间
    const safeVolume = Math.max(
      0,
      Math.min(1, Number(val) || 0)
    )

    audioRef.value.volume = safeVolume
  },

  { immediate: true }
)

// ==========================================
// 7. 音频真正开始播放时上报记录
// ==========================================

const reportPlay = async () => {
  const songId = player.currentSong?.id

  if (!songId) return

  // 同一首歌曲暂停后继续播放，不重复上报
  if (reportedSongId.value === songId) return

  // 先标记，防止短时间内重复触发
  reportedSongId.value = songId

  try {
    await reportSongPlayApi(songId)

    console.log('播放记录上报成功：', songId)
  } catch (error) {
    console.error('播放记录上报失败：', error)

    // 上报失败后允许再次尝试
    if (reportedSongId.value === songId) {
      reportedSongId.value = null
    }
  }
}

// ==========================================
// 8. 播放 / 暂停
// ==========================================

function toggle() {
  player.playing = !player.playing
}

// ==========================================
// 9. 更新播放进度
// ==========================================

function onTimeUpdate() {
  if (!audioRef.value) return

  // 拖动期间不使用旧播放时间覆盖滑块
  if (isSeeking.value) return

  player.currentTime = audioRef.value.currentTime
}

// ==========================================
// 10. 音频加载完成
// ==========================================

function onLoaded() {
  if (!audioRef.value) return

  player.duration = audioRef.value.duration || 0

  // 音频加载完成后同步音量
  audioRef.value.volume = player.volume
}

// ==========================================
// 11. 时间格式化
// ==========================================

function format(s) {
  if (!s || !Number.isFinite(s)) {
    return '00:00'
  }

  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)

  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}
</script>

<style scoped>
.global-player {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 90px;
  background: rgba(24, 24, 27, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 1000;
  color: #fff;
  box-shadow: 0 -10px 40px rgba(0, 0, 0, 0.1);
}

.player-inner {
  max-width: 1400px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
}

.song-info {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 280px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.song-info:hover {
  opacity: 0.8;
}

.cover {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.title {
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.artist {
  font-size: 12px;
  color: #a1a1aa;
}

.player-controls {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  max-width: 600px;
}

.buttons {
  display: flex;
  align-items: center;
  gap: 24px;
}

.ctrl-btn {
  color: #a1a1aa !important;
  background: transparent !important;
}

.ctrl-btn:hover {
  color: #fff !important;
}

.play-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #fff;
  color: #18181b;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.2);
}

.play-btn:hover {
  transform: scale(1.05);
  background: #f4f4f5;
}

.progress-bar {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
}

.time {
  font-size: 12px;
  color: #a1a1aa;
  width: 40px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}

:deep(.el-slider__runway) {
  background-color: rgba(255, 255, 255, 0.2);
}

:deep(.el-slider__bar) {
  background: var(--primary-gradient);
}

:deep(.el-slider__button) {
  border-color: #ec4899;
  width: 12px;
  height: 12px;
}

.extra-actions {
  width: 280px;
  display: flex;
  justify-content: flex-end;
}

.volume-icon-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: 50%;
  transition: background 0.2s;
}

.volume-icon-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.volume-control {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.volume-text {
  font-size: 12px;
  color: #666;
}
.lyric-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #a1a1aa;
  cursor: pointer;
  margin-right: 20px;
  transition: color 0.2s;
}

.lyric-btn:hover {
  color: #ec4899;
}

.lyric-btn span {
  font-size: 13px;
}
</style>