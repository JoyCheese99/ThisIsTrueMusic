<template>
  <div class="song-list-page">
    <div class="page-header">
      <h1>歌单详情</h1>
    </div>
    <!-- 增加一个加载动画 -->
    <div v-loading="loading" element-loading-text="哈基米正在赶来..." class="grid-container">
      <!-- 如果没歌曲显示空状态 -->
      <el-empty v-if="songs.length === 0 && !loading" description="这个歌单里还没有歌曲哦~" style="grid-column: 1 / -1;" />
      
      <div v-for="song in songs" :key="song.id" class="music-card">
        <!-- 修正：后端可能返回 127.0.0.1，做个容错替换 -->
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

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination 
        background 
        layout="prev, pager, next" 
        :total="total" 
        :page-size="pageSize"
        v-model:current-page="currentPage"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { VideoPlay } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/player'
import { getPlaylistSongsApi } from '@/api/playlist'

const route = useRoute()
const player = usePlayerStore()

// 路由参数里的歌单 ID
const playlistId = route.params.id

const songs = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12) // 歌单里每页展示 12 个

// 处理 URL（针对后端返回 127.0.0.1 的情况）
const formatUrl = (url) => {
  if (!url) return ''
  return url.replace('127.0.0.1', '26.89.197.25')
}

// 获取歌单歌曲数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPlaylistSongsApi(playlistId, { 
      page: currentPage.value, 
      size: pageSize.value 
    })
    
    // 处理后端返回的图片和音频地址
    songs.value = res.records.map(song => ({
      ...song,
      audioUrl: formatUrl(song.audioUrl),
      coverUrl: formatUrl(song.coverUrl)
    }))
    total.value = res.total
  } catch (error) {
    console.error('获取歌单歌曲失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})

// 播放
const play = (song) => {
  player.playSong(song, songs.value)
}
</script>

<style scoped>
.page-header {
  margin-bottom: 30px;
}
.page-header h1 {
  font-size: 32px;
  font-weight: 800;
  margin: 0;
  color: var(--text-main);
}

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

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>