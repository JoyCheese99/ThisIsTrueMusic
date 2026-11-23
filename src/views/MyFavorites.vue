<template>
  <div class="favorites-page">
    <div class="page-header">
      <h1>我的收藏</h1>
      <p>你喜欢的哈基米都在这里</p>
    </div>

    <!-- 如果没数据，显示空状态 -->
    <el-empty v-if="favorites.length === 0" description="还没有收藏任何歌曲哦~" />

    <!-- 有数据展示网格 -->
    <div v-else class="grid-container">
      <div v-for="song in favorites" :key="song.id" class="music-card" @click="play(song)">
        <div class="cover-wrapper">
          <img :src="song.coverUrl" class="cover-img" />
          <div class="play-overlay">
            <el-icon class="play-icon"><VideoPlay /></el-icon>
          </div>
        </div>
        <div class="info-wrapper">
          <h3 class="title">{{ song.title }}</h3>
          <p class="artist">{{ song.artist }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { usePlayerStore } from '@/stores/player'
import { VideoPlay } from '@element-plus/icons-vue'

const player = usePlayerStore()

// 模拟收藏的数据 (实际开发中由接口获取)
const favorites = ref([
  { id: 1, title: '哈基米之歌 (原版)', artist: '蜂蜜水', coverUrl: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500&q=80', audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3' }
])

const play = (song) => {
  player.playSong(song, favorites.value)
}
</script>

<style scoped>
.page-header { margin-bottom: 40px; }
.page-header h1 { font-size: 32px; font-weight: 800; margin: 0 0 8px 0; color: var(--text-main); }
.page-header p { color: var(--text-muted); font-size: 16px; margin: 0; }

/* 复用与 SongList 相同的网格样式 */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
}
.music-card {
  background: #ffffff; border-radius: 16px; padding: 12px; cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}
.music-card:hover { transform: translateY(-8px); box-shadow: 0 12px 30px rgba(139, 92, 246, 0.15); }
.cover-wrapper { position: relative; width: 100%; aspect-ratio: 1; border-radius: 12px; overflow: hidden; }
.cover-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
.music-card:hover .cover-img { transform: scale(1.08); }
.play-overlay {
  position: absolute; inset: 0; background: rgba(0, 0, 0, 0.3);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.3s ease; backdrop-filter: blur(2px);
}
.music-card:hover .play-overlay { opacity: 1; }
.play-icon { font-size: 48px; color: rgba(255, 255, 255, 0.9); }
.info-wrapper { padding: 16px 4px 8px 4px; }
.title { font-size: 16px; font-weight: 700; margin: 0 0 6px 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.artist { font-size: 13px; color: var(--text-muted); margin: 0; }
</style>