<template>
  <div class="detail-page" v-loading="loading" v-if="song">
    <!-- 顶部歌曲信息区 -->
    <div class="hero-section">
      <div class="cover-wrapper">
        <img :src="formatUrl(song.coverUrl)" class="cover-img" />
      </div>
      <div class="info-wrapper">
        <div class="tag">单曲</div>
        <h1>{{ song.title }}</h1>
        <p class="artist">
          歌手：
          <!-- 点击歌手名字，带着 keyword 参数跳转到发现页 -->
          <span 
            class="artist-link"
            @click="goToArtist(song.artist)"
          >
            {{ song.artist }}
          </span>
        </p>
        
        <div class="actions">
          <el-button type="primary" size="large" round :icon="VideoPlay" @click="playSong">播放歌曲</el-button>
          
          <el-button size="large" round :icon="Star" :type="isFavorited ? 'warning' : 'default'" @click="toggleFavorite">
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          
          <el-button size="large" round :icon="Pointer" :type="isLiked ? 'danger' : 'default'" @click="toggleLike">
            {{ isLiked ? '已点赞' : '点赞' }} ({{ likeCount }})
          </el-button>

          <!-- 添加到歌单下拉菜单 -->
          <el-dropdown v-if="userStore.isLogin" @command="handleAddToPlaylist" style="margin-left: 12px;">
            <el-button size="large" round :icon="FolderAdd">添加到歌单</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="myPlaylists.length === 0" disabled>
                  暂无歌单，快去创建吧
                </el-dropdown-item>
                <el-dropdown-item v-for="pl in myPlaylists" :key="pl.id" :command="pl.id">
                  {{ pl.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="comments-section">
      <h3>评论区 ({{ comments.length }})</h3>
      
      <!-- 发评论 -->
      <div class="input-area">
        <el-avatar :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
        <div class="input-box">
          <el-input 
            v-model="newComment" 
            type="textarea" 
            :rows="2" 
            placeholder="说点什么吧，让哈基米听见..." 
          />
          <div class="submit-btn">
            <el-button type="primary" @click="submitComment" :disabled="!newComment.trim()">发送评论</el-button>
          </div>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <el-avatar :size="40" :src="formatUrl(comment.avatarUrl)" />
          <div class="comment-content">
            <div class="comment-header">
              <span class="username">{{ comment.nickname }}</span>
              <span class="time">{{ new Date(comment.createdAt).toLocaleString() }}</span>
            </div>
            <p class="text">{{ comment.content }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VideoPlay, Star, Pointer, FolderAdd } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { usePlayerStore } from '@/stores/player'
import { useUserStore } from '@/stores/user'
import { 
  getSongDetailApi, likeSongApi, favoriteSongApi, 
  getCommentsApi, postCommentApi 
} from '@/api/song'
import { getMyPlaylistsApi, addSongToPlaylistApi } from '@/api/playlist'

const route = useRoute()
const router = useRouter()
const player = usePlayerStore()
const userStore = useUserStore()

const songId = route.params.id
const song = ref(null)
const loading = ref(true)

// 互动状态
const isFavorited = ref(false)
const isLiked = ref(false)
const likeCount = ref(0)

// 评论相关
const newComment = ref('')
const comments = ref([])

// 歌单相关
const myPlaylists = ref([])

const formatUrl = (url) => url ? url.replace('127.0.0.1', '26.89.197.25') : ''

// 点击歌手跳转
const goToArtist = (artistName) => {
  if (!artistName) return
  router.push({ path: '/songs', query: { keyword: artistName } })
}

const fetchDetail = async () => {
  try {
    const res = await getSongDetailApi(songId)
    song.value = {
      ...res,
      audioUrl: formatUrl(res.audioUrl),
      coverUrl: formatUrl(res.coverUrl)
    }
    isLiked.value = res.liked || false
    isFavorited.value = res.favorited || false
    likeCount.value = res.likeCount || 0
  } catch (error) {
    console.error(error)
  }
}

const fetchComments = async () => {
  try {
    const res = await getCommentsApi(songId, { page: 1, size: 50 })
    comments.value = res.records
  } catch (error) {
    console.error(error)
  }
}

const fetchPlaylists = async () => {
  if (!userStore.isLogin) return
  try {
    const res = await getMyPlaylistsApi()
    myPlaylists.value = Array.isArray(res) ? res : (res.records || [])
  } catch (error) {
    console.error('获取歌单失败', error)
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([
    fetchDetail(), 
    fetchComments(),
    fetchPlaylists()
  ])
  loading.value = false
})

const playSong = () => {
  player.playSong(song.value, [song.value])
}

const toggleFavorite = async () => {
  if (!userStore.isLogin) return ElMessage.warning('请先登录')
  try {
    const res = await favoriteSongApi(songId)
    isFavorited.value = res.favorited
    ElMessage.success(res.favorited ? '已加入收藏' : '已取消收藏')
  } catch (error) { console.error(error) }
}

const toggleLike = async () => {
  if (!userStore.isLogin) return ElMessage.warning('请先登录')
  try {
    const res = await likeSongApi(songId)
    isLiked.value = res.liked
    likeCount.value += res.liked ? 1 : -1
    ElMessage.success(res.liked ? '点赞成功' : '取消点赞')
  } catch (error) { console.error(error) }
}

const submitComment = async () => {
  if (!userStore.isLogin) return ElMessage.warning('请先登录')
  if (!newComment.value.trim()) return
  
  try {
    await postCommentApi(songId, { content: newComment.value })
    ElMessage.success('评论发布成功')
    newComment.value = ''
    fetchComments() 
  } catch (error) { console.error(error) }
}

const handleAddToPlaylist = async (playlistId) => {
  try {
    await addSongToPlaylistApi(playlistId, songId)
    ElMessage.success('已成功添加到歌单！')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.detail-page { max-width: 1000px; margin: 0 auto; min-height: 500px; }
.hero-section { display: flex; gap: 40px; background: white; padding: 40px; border-radius: 24px; box-shadow: 0 10px 30px rgba(0,0,0,0.03); margin-bottom: 40px; }
.cover-wrapper { width: 260px; height: 260px; border-radius: 16px; overflow: hidden; box-shadow: 0 20px 40px rgba(0,0,0,0.1); flex-shrink: 0; }
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.info-wrapper { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.tag { display: inline-block; padding: 4px 12px; background: rgba(139, 92, 246, 0.1); color: #8b5cf6; border-radius: 20px; font-size: 12px; font-weight: bold; width: fit-content; margin-bottom: 16px; }
h1 { font-size: 36px; margin: 0 0 12px 0; color: var(--text-main); }

.artist { color: var(--text-muted); font-size: 16px; margin-bottom: 32px; }
.artist-link {
  cursor: pointer;
  color: #8b5cf6;
  font-weight: 600;
  transition: color 0.2s ease;
}
.artist-link:hover {
  color: #ec4899;
}

.actions { display: flex; gap: 12px; align-items: center; }
.comments-section { background: white; padding: 40px; border-radius: 24px; box-shadow: 0 10px 30px rgba(0,0,0,0.03); }
h3 { font-size: 20px; margin-top: 0; margin-bottom: 24px; }
.input-area { display: flex; gap: 16px; margin-bottom: 40px; }
.input-box { flex: 1; }
.submit-btn { margin-top: 12px; text-align: right; }
.comment-item { display: flex; gap: 16px; padding: 20px 0; border-bottom: 1px solid #f1f5f9; }
.comment-item:last-child { border-bottom: none; }
.comment-content { flex: 1; }
.comment-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.username { font-weight: 600; font-size: 14px; color: var(--text-main); }
.time { font-size: 12px; color: #94a3b8; }
.text { margin: 0; font-size: 15px; line-height: 1.6; color: var(--text-muted); }
</style>