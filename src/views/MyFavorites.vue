<template>
  <div class="favorites-page">
    <div class="page-header">
      <h1>我喜欢的音乐</h1>
      <p>你红心收藏的哈基米都在这里</p>
    </div>

    <!-- 加载状态、错误状态和空状态 -->
    <div v-if="loading" class="state-message">正在加载收藏列表...</div>
    <el-result v-else-if="errorMessage" icon="error" :sub-title="errorMessage">
      <template #extra>
        <el-button type="primary" @click="fetchFavorites">重新加载</el-button>
      </template>
    </el-result>
    <el-empty v-else-if="favorites.length === 0" description="还没有收藏任何歌曲哦~" />

    <!-- 有数据展示网格 -->
    <div v-else class="grid-container">
      <div v-for="song in favorites" :key="song.id" class="music-card">
        <!-- 点击封面播放 -->
        <div class="cover-wrapper" @click="play(song)">
          <img :src="song.coverUrl" class="cover-img" />
          <div class="play-overlay">
            <el-icon class="play-icon"><VideoPlay /></el-icon>
          </div>
        </div>
        <div class="info-wrapper">
          <!-- 点击标题去详情页 -->
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
import { onMounted, ref } from 'vue'
import { usePlayerStore } from '@/stores/player'
import { VideoPlay } from '@element-plus/icons-vue'
import { getMyFavoritesApi } from '@/api/song'

const player = usePlayerStore()

const favorites = ref([])
const loading = ref(false)
const errorMessage = ref('')

// 图片地址处理
const formatUrl = (url) => url ? url.replace('127.0.0.1', '26.89.197.25') : ''

const fetchFavorites = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    // 拦截器已经解包，res 就是文档里的 { total, current, size, records }
    const res = await getMyFavoritesApi({ page: 1, size: 100 })
    
    // 根据文档 2.3，数据在 records 里
    const records = res.records || []
    
    // 映射字段并处理 URL
    favorites.value = records.map(song => ({
      ...song,
      coverUrl: formatUrl(song.coverUrl) || 'https://via.placeholder.com/300',
      audioUrl: formatUrl(song.audioUrl)
    }))
  } catch (error) {
    console.error('获取我的收藏失败：', error)
    errorMessage.value = error.message || '收藏列表加载失败，请稍后重试'
    favorites.value = []
  } finally {
    loading.value = false
  }
}

const play = (song) => {
  player.playSong(song, favorites.value)
}

onMounted(fetchFavorites)
</script>

<style scoped>
.page-header { margin-bottom: 40px; }
.state-message { padding: 40px 0; color: var(--text-muted); text-align: center; }
.page-header h1 { font-size: 32px; font-weight: 800; margin: 0 0 8px 0; color: var(--text-main); }
.page-header p { color: var(--text-muted); font-size: 16px; margin: 0; }

.grid-container { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 24px; min-height: 200px; }
.music-card { background: #ffffff; border-radius: 16px; padding: 12px; transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04); }
.music-card:hover { transform: translateY(-8px); box-shadow: 0 12px 30px rgba(139, 92, 246, 0.15); }
.cover-wrapper { position: relative; width: 100%; aspect-ratio: 1; border-radius: 12px; overflow: hidden; cursor: pointer; }
.cover-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
.music-card:hover .cover-img { transform: scale(1.08); }
.play-overlay { position: absolute; inset: 0; background: rgba(0, 0, 0, 0.3); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s ease; backdrop-filter: blur(2px); }
.music-card:hover .play-overlay { opacity: 1; }
.play-icon { font-size: 48px; color: rgba(255, 255, 255, 0.9); }
.info-wrapper { padding: 16px 4px 8px 4px; }
.title { font-size: 16px; font-weight: 700; margin: 0 0 6px 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: pointer; transition: color 0.2s;}
.title:hover { color: #8b5cf6; }
.artist { font-size: 13px; color: var(--text-muted); margin: 0; }
</style>