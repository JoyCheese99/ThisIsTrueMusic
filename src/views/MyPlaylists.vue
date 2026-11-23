<template>
  <div class="playlist-page">
    <div class="page-header">
      <h1>我的歌单</h1>
      <el-button type="primary" round @click="dialogVisible = true">创建新歌单</el-button>
    </div>

    <div class="grid-container">
      <div v-for="pl in playlists" :key="pl.id" class="playlist-card" @click="$router.push(`/playlist/${pl.id}`)">
        <img :src="formatUrl(pl.coverUrl) || 'https://via.placeholder.com/300'" class="cover" />
        <div class="info">
          <h3>{{ pl.name }}</h3>
          <p>{{ pl.description || '暂无简介' }}</p>
        </div>
      </div>
    </div>

    <!-- 创建歌单弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建歌单" width="400px">
      <el-form label-width="80px">
        <el-form-item label="歌单名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="封面">
          <el-upload action="#" :auto-upload="false" :on-change="handleCoverChange" :limit="1">
            <el-button type="primary">选择图片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createPlaylist">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyPlaylistsApi, createPlaylistApi } from '@/api/playlist'

const playlists = ref([])
const dialogVisible = ref(false)
const form = reactive({ name: '', description: '' })
const coverFile = ref(null)

const formatUrl = (url) => url ? url.replace('127.0.0.1', '26.89.197.25') : ''

const fetchPlaylists = async () => {
  const res = await getMyPlaylistsApi()
  playlists.value = res // 文档说返回 data 是数组
}

const handleCoverChange = (file) => coverFile.value = file.raw

const createPlaylist = async () => {
  if (!form.name) return ElMessage.warning('请输入歌单名')
  const formData = new FormData()
  formData.append('name', form.name)
  if (form.description) formData.append('description', form.description)
  if (coverFile.value) formData.append('cover', coverFile.value)

  await createPlaylistApi(formData)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  fetchPlaylists()
}

onMounted(fetchPlaylists)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.page-header h1 { font-size: 32px; margin: 0; }
.grid-container { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
.playlist-card { background: #fff; border-radius: 16px; padding: 12px; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 20px rgba(0,0,0,0.04); }
.playlist-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(139, 92, 246, 0.15); }
.cover { width: 100%; aspect-ratio: 1; border-radius: 12px; object-fit: cover; }
.info h3 { margin: 12px 0 6px; font-size: 16px; }
.info p { margin: 0; font-size: 12px; color: #888; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>