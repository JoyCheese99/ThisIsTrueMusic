<template>
  <div class="song-list-page" v-loading="loading">
    <!-- 创作者信息头部 -->
    <div class="creator-header" v-if="creator">
      <el-avatar :size="100" :src="formatUrl(creator.avatarUrl)" style="background: var(--primary-gradient); color: white; font-size: 40px; font-weight: bold;">
        {{ (creator.nickname || creator.username).charAt(0).toUpperCase() }}
      </el-avatar>
      <div class="info">
        <h1>{{ creator.nickname }}</h1>
        <p>@{{ creator.username }}</p>
      </div>
    </div>

    <h2 style="margin-bottom: 24px; color: var(--text-main);">TA 的歌曲 ({{ creatorSongs.length }})</h2>
    
    <!-- 歌曲网格列表 -->
    <div class="grid-container">
      <el-empty v-if="creatorSongs.length === 0 && !loading" description="这位神人还没有上传歌曲哦~" style="grid-column: 1 / -1;" />
      
      <div v-for="song in creatorSongs" :key="song.id" class="music-card">
        <div class="cover-wrapper" @click="play(song)">
          <img :src="formatUrl(song.coverUrl) || 'https://via.placeholder.com/300'" class="cover-img" loading="lazy" />
          <div class="play-overlay">
            <el-icon class="play-icon"><VideoPlay /></el-icon>
          </div>
        </div>
        <div class="info-wrapper">
          <h3 class="title" @click="$router.push(`/song/${song.id}`)">
            {{ song.title }}
          </h3>
          <p class="artist">{{ song.artist }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { VideoPlay } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/player'
import { getCreatorDetailApi, getCreatorSongsApi } from '@/api/creator'

const route = useRoute()
const player = usePlayerStore()

const creator = ref(null)
const creatorSongs = ref([])
const loading = ref(true)

const formatUrl = (url) => url ? url.replace('127.0.0.1', '26.89.197.25') : ''

const fetchCreatorData = async () => {
  loading.value = true
  try {
    // 并发请求：获取创作者信息和TA的歌曲
    const [info, songsRes] = await Promise.all([
      getCreatorDetailApi(route.params.id),
      getCreatorSongsApi(route.params.id, { page: 1, size: 50 })
    ])
    
    creator.value = info
    creatorSongs.value = songsRes.records.map(s => ({ 
      ...s, 
      audioUrl: formatUrl(s.audioUrl), 
      coverUrl: formatUrl(s.coverUrl) 
    }))
  } catch (error) {
    console.error('获取创作者数据失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCreatorData()
})

// 播放该创作者的所有歌曲
const play = (song) => {
  player.playSong(song, creatorSongs.value)
}
</script>

<style scoped>
.creator-header { 
  display: flex; 
  align-items: center; 
  gap: 30px; 
  margin-bottom: 40px; 
  padding: 40px; 
  background: #fff; 
  border-radius: 24px; 
  box-shadow: 0 10px 30px rgba(0,0,0,0.03);
}

.creator-header h1 { 
  margin: 0 0 8px 0; 
  font-size: 32px;
  color: var(--text-main);
}

.creator-header p { 
  color: var(--text-muted); 
  font-size: 16px;
  margin: 0; 
}

/* 复用全局的网格卡片样式 */
.grid-container {
  min-height: 300px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
}

.music-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 12px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.music-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(139, 92, 246, 0.15);
}

.cover-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.music-card:hover .cover-img {
  transform: scale(1.08);
}

.play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(2px);
}

.music-card:hover .play-overlay {
  opacity: 1;
}

.play-icon {
  font-size: 48px;
  color: rgba(255, 255, 255, 0.9);
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.3));
  transition: transform 0.3s ease;
}

.play-icon:hover {
  transform: scale(1.1);
}

.info-wrapper {
  padding: 16px 4px 8px 4px;
}

.title {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 6px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: color 0.2s ease;
}

.title:hover {
  color: #8b5cf6;
}

.artist {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}
</style>