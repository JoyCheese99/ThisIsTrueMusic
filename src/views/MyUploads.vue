<template>
  <div class="song-list-page">
    <div class="page-header">
      <h1>我的上传</h1>
      <p>你为哈基米宇宙贡献的音乐</p>
    </div>

    <div v-loading="loading" class="grid-container">
      <el-empty v-if="songs.length === 0 && !loading" description="你还没有上传过歌曲哦~" />
      
      <div v-for="song in songs" :key="song.id" class="music-card">
        <div class="cover-wrapper" @click="play(song)">
          <img :src="formatUrl(song.coverUrl) || 'https://via.placeholder.com/300'" class="cover-img" />
          <div class="play-overlay"><el-icon class="play-icon"><VideoPlay /></el-icon></div>
        </div>
        <div class="info-wrapper">
          <h3 class="title" @click="$router.push(`/song/${song.id}`)">{{ song.title }}</h3>
          <p class="artist">{{ song.artist }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePlayerStore } from '@/stores/player'
import { VideoPlay } from '@element-plus/icons-vue'
import { getMyUploadsApi } from '@/api/user'

const player = usePlayerStore()
const songs = ref([])
const loading = ref(false)

const formatUrl = (url) => url ? url.replace('127.0.0.1', '26.89.197.25') : ''

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyUploadsApi({ page: 1, size: 50 })
    songs.value = res.records.map(s => ({
      ...s, audioUrl: formatUrl(s.audioUrl), coverUrl: formatUrl(s.coverUrl)
    }))
  } catch (error) { console.error(error) } 
  finally { loading.value = false }
}

onMounted(() => fetchData())

const play = (song) => player.playSong(song, songs.value)
</script>

<style scoped>
.page-header { margin-bottom: 40px; }
.page-header h1 { font-size: 32px; font-weight: 800; margin: 0 0 8px 0; color: var(--text-main); }
.page-header p { color: var(--text-muted); font-size: 16px; margin: 0; }
.grid-container { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 24px; min-height: 200px; }
.music-card { background: #ffffff; border-radius: 16px; padding: 12px; transition: all 0.4s; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04); }
.music-card:hover { transform: translateY(-8px); box-shadow: 0 12px 30px rgba(139, 92, 246, 0.15); }
.cover-wrapper { position: relative; width: 100%; aspect-ratio: 1; border-radius: 12px; overflow: hidden; cursor: pointer; }
.cover-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
.music-card:hover .cover-img { transform: scale(1.08); }
.play-overlay { position: absolute; inset: 0; background: rgba(0, 0, 0, 0.3); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s ease; }
.music-card:hover .play-overlay { opacity: 1; }
.play-icon { font-size: 48px; color: #fff; }
.info-wrapper { padding: 16px 4px 8px 4px; }
.title { font-size: 16px; font-weight: 700; margin: 0 0 6px 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: pointer; }
.title:hover { color: #8b5cf6; }
.artist { font-size: 13px; color: var(--text-muted); margin: 0; }
</style>