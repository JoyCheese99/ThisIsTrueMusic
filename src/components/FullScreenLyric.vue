<template>
  <transition name="slide-up">
    <div v-if="player.showLyric" class="lyric-page">
      <!-- 背景模糊 -->
      <div class="bg-blur" :style="{ backgroundImage: `url(${player.currentSong?.coverUrl})` }"></div>
      
      <!-- 顶部关闭按钮 -->
      <div class="header">
        <el-icon class="close-btn" size="32" @click="player.showLyric = false"><ArrowDown /></el-icon>
      </div>

      <!-- 主体内容 -->
      <div class="content">
        <!-- 左：唱片 -->
        <div class="disc-area">
          <div class="disc" :class="{ 'is-playing': player.playing }">
            <img :src="player.currentSong?.coverUrl || 'https://via.placeholder.com/300'" class="disc-img" />
          </div>
          <h1>{{ player.currentSong?.title }}</h1>
          <p>{{ player.currentSong?.artist }}</p>
        </div>

        <!-- 右：歌词滚动区 -->
        <div class="lyric-area" ref="lyricScrollRef">
          <div class="lyric-list" :style="{ transform: `translateY(${offsetY}px)` }">
            <p 
              v-for="(line, index) in lyrics" 
              :key="index"
              :class="['lyric-line', { active: index === currentLineIndex }]"
            >
              {{ line.text }}
            </p>
            <p v-if="lyrics.length === 0" style="color: #888;">纯音乐，请欣赏</p>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/player'

const player = usePlayerStore()
const lyrics = ref([])
const currentLineIndex = ref(0)
const offsetY = ref(0)

// 解析 LRC 歌词 (格式: [00:00.00]歌词内容)
const parseLrc = (lrcText) => {
  if (!lrcText) return []
  const lines = lrcText.split('\n')
  const result = []
  const timeReg = /\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)/
  
  lines.forEach(line => {
    const match = line.match(timeReg)
    if (match) {
      const m = parseInt(match[1])
      const s = parseInt(match[2])
      const ms = parseInt(match[3])
      const time = m * 60 + s + ms / 1000
      const text = match[4].trim()
      if (text) result.push({ time, text })
    }
  })
  return result.sort((a, b) => a.time - b.time)
}

// 监听歌曲变化，重新解析歌词
watch(() => player.currentSong, () => {
  lyrics.value = parseLrc(player.currentSong?.lyric)
  currentLineIndex.value = 0
  offsetY.value = 0
}, { immediate: true })

// 监听时间变化，滚动歌词
watch(() => player.currentTime, (time) => {
  if (lyrics.value.length === 0) return
  
  // 找到当前时间对应的歌词行
  let index = lyrics.value.findIndex((line, i) => {
    const nextLine = lyrics.value[i + 1]
    return time >= line.time && (!nextLine || time < nextLine.time)
  })
  
  if (index === -1) index = 0 // 如果没匹配上（比如前奏），默认第一行
  
  if (index !== currentLineIndex.value) {
    currentLineIndex.value = index
    // 每次上移 40px (根据你实际歌词行高调整)
    offsetY.value = -(index * 40)
  }
})
</script>

<style scoped>
.lyric-page {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: #111;
  color: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.bg-blur {
  position: absolute;
  inset: -100px;
  background-size: cover;
  background-position: center;
  filter: blur(80px);
  opacity: 0.3;
  z-index: 0;
}

.header {
  position: relative;
  z-index: 10;
  padding: 30px;
  display: flex;
  justify-content: flex-start;
}

.close-btn {
  cursor: pointer;
  color: #ccc;
  transition: color 0.3s;
}
.close-btn:hover { color: #fff; }

.content {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10%;
  padding: 0 10%;
}

/* 左侧唱片 */
.disc-area {
  text-align: center;
  width: 400px;
}

.disc {
  width: 300px;
  height: 300px;
  border-radius: 50%;
  margin: 0 auto 30px;
  overflow: hidden;
  border: 15px solid #222;
  box-shadow: 0 0 40px rgba(0,0,0,0.8), inset 0 0 20px rgba(0,0,0,0.8);
  animation: spin 20s linear infinite;
  animation-play-state: paused;
}

.disc.is-playing {
  animation-play-state: running;
}

.disc-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@keyframes spin {
  100% { transform: rotate(360deg); }
}

.disc-area h1 { font-size: 28px; margin: 0 0 10px; }
.disc-area p { color: #aaa; font-size: 16px; }

/* 右侧歌词 */
.lyric-area {
  flex: 1;
  height: 500px;
  overflow: hidden;
  position: relative;
  mask-image: linear-gradient(to bottom, transparent, #000 15%, #000 85%, transparent);
  -webkit-mask-image: linear-gradient(to bottom, transparent, #000 15%, #000 85%, transparent);
}

.lyric-list {
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  padding-top: 230px; /* 让第一句歌词居中 */
}

.lyric-line {
  font-size: 20px;
  color: #666;
  margin: 0;
  height: 40px;
  line-height: 40px;
  transition: all 0.3s;
  text-align: center;
}

.lyric-line.active {
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  text-shadow: 0 0 10px rgba(255,255,255,0.3);
}

/* 动画 */
.slide-up-enter-active, .slide-up-leave-active {
  transition: transform 0.4s ease, opacity 0.4s ease;
}
.slide-up-enter-from, .slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>