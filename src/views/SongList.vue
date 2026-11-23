<template>
  <div class="song-list-page">
    <div class="page-header">
      <div>
        <h1>发现哈基米</h1>
        <p>探索全网最魔性的猫咪翻唱与纯音乐</p>
      </div>
      
      <!-- 顶部搜索框 -->
      <div class="search-box">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索歌曲或歌手..." 
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>
    </div>

    <div v-loading="loading" element-loading-text="哈基米正在赶来..." class="grid-container">
      <el-empty v-if="songs.length === 0 && !loading" description="没有找到相关歌曲哦~" style="grid-column: 1 / -1;" />
      
      <div v-for="song in songs" :key="song.id" class="music-card">
        <div class="cover-wrapper" @click="play(song)">
          <img :src="formatUrl(song.coverUrl)" class="cover-img" loading="lazy" />
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
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VideoPlay, Search } from '@element-plus/icons-vue'
import { usePlayerStore } from '@/stores/player'
import { getSongsApi } from '@/api/song'

const route = useRoute()
const router = useRouter()
const player = usePlayerStore()

const songs = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const searchKeyword = ref('')

const formatUrl = (url) => {
  if (!url) return 'https://via.placeholder.com/300'
  return url.replace('127.0.0.1', '26.89.197.25')
}

const fetchData = async () => {
  loading.value = true
  try {
    // 获取 URL 上的 keyword
    const keyword = route.query.keyword || ''
    searchKeyword.value = keyword // 同步到搜索框
    
    const res = await getSongsApi({ 
      page: currentPage.value, 
      size: pageSize.value,
      keyword: keyword
    })
    
    songs.value = res.records.map(song => ({
      ...song,
      audioUrl: formatUrl(song.audioUrl),
      coverUrl: formatUrl(song.coverUrl)
    }))
    total.value = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 点击搜索按钮
const handleSearch = () => {
  currentPage.value = 1
  router.push({ path: '/songs', query: { keyword: searchKeyword.value } })
}

onMounted(() => {
  fetchData()
})

// 监听 URL 变化（从详情页点击歌手跳过来时触发）
watch(
  () => route.query.keyword,
  () => {
    currentPage.value = 1
    fetchData()
  }
)

const play = (song) => {
  player.playSong(song, songs.value)
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40px;
}
.page-header h1 {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 8px 0;
  color: var(--text-main);
}
.page-header p {
  color: var(--text-muted);
  font-size: 16px;
  margin: 0;
}
.search-box {
  width: 300px;
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