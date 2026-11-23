<template>
  <div v-if="player.currentSong" class="global-player">
    <div class="player-inner">
      <!-- 左侧歌曲信息 -->
      <div class="song-info">
        <img :src="player.currentSong.coverUrl || 'https://via.placeholder.com/60'" class="cover" />
        <div class="meta">
          <div class="title">{{ player.currentSong.title }}</div>
          <div class="artist">{{ player.currentSong.artist }}</div>
        </div>
      </div>

      <!-- 中间控制区 -->
      <div class="player-controls">
        <div class="buttons">
          <el-button circle text bg :icon="ArrowLeft" @click="player.prev()" class="ctrl-btn" />
          <button class="play-btn" @click="toggle">
            <el-icon size="24"><VideoPause v-if="player.playing" /><VideoPlay v-else /></el-icon>
          </button>
          <el-button circle text bg :icon="ArrowRight" @click="player.next()" class="ctrl-btn" />
        </div>
        
        <div class="progress-bar">
          <span class="time">{{ format(player.currentTime) }}</span>
          <el-slider 
            v-model="percent" 
            :show-tooltip="false" 
            @change="seek" 
            class="custom-slider"
          />
          <span class="time">{{ format(player.duration) }}</span>
        </div>
      </div>

      <!-- 右侧占位（音量等） -->
      <div class="extra-actions">
        <el-popover placement="top" :width="150" trigger="click">
          <template #reference>
            <div class="volume-icon-btn">
              <el-icon size="20" color="#a1a1aa"><Headset /></el-icon>
            </div>
          </template>
          <div class="volume-control">
            <el-icon><Mute v-if="player.volume === 0" /><Microphone v-else /></el-icon>
            <el-slider v-model="player.volume" vertical height="100px" style="margin: 10px 0;" />
            <span style="font-size: 12px; color: #666;">{{ Math.round(player.volume * 100) }}%</span>
          </div>
        </el-popover>
      </div>
    </div>

    <audio
      ref="audioRef"
      :src="player.currentSong.audioUrl"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoaded"
      @ended="player.next()"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { VideoPlay, VideoPause, ArrowLeft, ArrowRight, Operation } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/player'

const player = usePlayerStore()
const audioRef = ref()

const percent = computed({
  get: () => (player.duration ? (player.currentTime / player.duration) * 100 : 0),
  set: () => {}
})

watch(() => player.currentSong, async () => {
  await nextTick()
  audioRef.value?.play()
})

watch(() => player.playing, val => {
  if (!audioRef.value) return
  val ? audioRef.value.play() : audioRef.value.pause()
})

watch(() => player.volume, (val) => {
  if (audioRef.value) audioRef.value.volume = val
})

function toggle() { player.playing = !player.playing }
function onTimeUpdate() { player.currentTime = audioRef.value.currentTime }
function onLoaded() { 
  player.duration = audioRef.value.duration 
  audioRef.value.volume = player.volume // 初始化音量
}
function seek(val) { audioRef.value.currentTime = (val / 100) * player.duration }
function format(s) {
  if (!s) return '00:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}
</script>

<style scoped>
.global-player {
  position: fixed; bottom: 0; left: 0; right: 0; height: 90px;
  background: rgba(24, 24, 27, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 1000;
  color: #fff;
  box-shadow: 0 -10px 40px rgba(0,0,0,0.1);
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
  display: flex; align-items: center; gap: 16px; width: 280px;
}
.cover {
  width: 56px; height: 56px; border-radius: 8px; object-fit: cover;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}
.meta { display: flex; flex-direction: column; gap: 4px; overflow: hidden; }
.title { font-size: 15px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.artist { font-size: 12px; color: #a1a1aa; }

.player-controls {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  max-width: 600px;
}

.buttons {
  display: flex; align-items: center; gap: 24px;
}
.ctrl-btn {
  color: #a1a1aa !important;
  background: transparent !important;
}
.ctrl-btn:hover { color: #fff !important; }

.play-btn {
  width: 44px; height: 44px; border-radius: 50%;
  background: #fff;
  color: #18181b;
  border: none;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(255,255,255,0.2);
}
.play-btn:hover {
  transform: scale(1.05);
  background: #f4f4f5;
}

.progress-bar {
  width: 100%;
  display: flex; align-items: center; gap: 16px;
}
.time { font-size: 12px; color: #a1a1aa; width: 40px; text-align: center; font-variant-numeric: tabular-nums;}

:deep(.el-slider__runway) { background-color: rgba(255,255,255,0.2); }
:deep(.el-slider__bar) { background: var(--primary-gradient); }
:deep(.el-slider__button) { border-color: #ec4899; width: 12px; height: 12px; }

.extra-actions { width: 280px; display: flex; justify-content: flex-end; }

.song-info { cursor: pointer; transition: opacity 0.2s; }
.song-info:hover { opacity: 0.8; }
.volume-icon-btn { cursor: pointer; display: flex; align-items: center; justify-content: center; padding: 8px; border-radius: 50%; transition: background 0.2s; }
.volume-icon-btn:hover { background: rgba(255,255,255,0.1); }
.volume-control { display: flex; flex-direction: column; align-items: center; justify-content: center; }
</style>