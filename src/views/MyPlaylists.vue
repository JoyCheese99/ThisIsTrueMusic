<template>
  <div class="playlist-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>我的歌单</h1>
      <el-button type="primary" round @click="openCreateDialog">
        创建新歌单
      </el-button>
    </div>

    <!-- 歌单列表 -->
    <div class="grid-container">
      <div
        v-for="pl in playlists"
        :key="pl.id"
        class="playlist-card"
        @click="$router.push(`/playlist/${pl.id}`)"
      >
        <img
          :src="formatUrl(pl.coverUrl) || 'https://via.placeholder.com/300'"
          class="cover"
        />

        <div class="info">
          <h3>{{ pl.name }}</h3>
          <p>{{ pl.description || '暂无简介' }}</p>
        </div>

        <!-- 歌单操作按钮 -->
        <div class="card-actions">
          <el-button
            type="primary"
            size="small"
            plain
            @click.stop="openEditDialog(pl)"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>

          <el-button
            type="danger"
            size="small"
            plain
            @click.stop="deletePlaylist(pl.id)"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 创建 / 编辑歌单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑歌单' : '创建歌单'"
      width="400px"
      @closed="resetForm"
    >
      <el-form label-width="80px">
        <el-form-item label="歌单名">
          <el-input
            v-model="form.name"
            placeholder="请输入歌单名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="简介">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入歌单简介"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面">
          <el-upload
            :key="uploadKey"
            action="#"
            :auto-upload="false"
            :on-change="handleCoverChange"
            :limit="1"
            accept="image/*"
          >
            <el-button type="primary">
              选择图片
            </el-button>
          </el-upload>

          <div v-if="isEdit && !coverFile" class="cover-tip">
            不选择新图片则保留原封面
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="submitLoading"
          @click="submitPlaylist"
        >
          {{ isEdit ? '保存修改' : '确定创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'

import {
  getMyPlaylistsApi,
  createPlaylistApi,
  updatePlaylistApi,
  deletePlaylistApi
} from '@/api/playlist'

// 歌单列表
const playlists = ref([])

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const submitLoading = ref(false)

// 表单
const form = reactive({
  name: '',
  description: ''
})

// 封面文件
const coverFile = ref(null)
const uploadKey = ref(0)

// 图片地址处理
const formatUrl = (url) => {
  return url ? url.replace('127.0.0.1', '26.89.197.25') : ''
}

// 获取我的歌单
const fetchPlaylists = async () => {
  try {
    const res = await getMyPlaylistsApi()
    playlists.value = res
  } catch (error) {
    console.error('获取歌单失败：', error)
    ElMessage.error('获取歌单失败')
  }
}

// 选择封面
const handleCoverChange = (file) => {
  coverFile.value = file.raw
}

// 重置表单
const resetForm = () => {
  form.name = ''
  form.description = ''
  coverFile.value = null
  editingId.value = null
  isEdit.value = false
  uploadKey.value++
}

// 打开创建弹窗
const openCreateDialog = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (playlist) => {
  resetForm()

  isEdit.value = true
  editingId.value = playlist.id

  form.name = playlist.name || ''
  form.description = playlist.description || ''

  dialogVisible.value = true
}

// 提交创建 / 修改
const submitPlaylist = async () => {
  if (!form.name.trim()) {
    return ElMessage.warning('请输入歌单名称')
  }

  if (submitLoading.value) return

  submitLoading.value = true

  try {
    const formData = new FormData()

    formData.append('name', form.name.trim())
    formData.append('description', form.description || '')

    if (coverFile.value) {
      formData.append('cover', coverFile.value)
    }

    if (isEdit.value) {
      // 修改歌单
      await updatePlaylistApi(editingId.value, formData)
      ElMessage.success('歌单修改成功')
    } else {
      // 创建歌单
      await createPlaylistApi(formData)
      ElMessage.success('歌单创建成功')
    }

    dialogVisible.value = false
    await fetchPlaylists()

  } catch (error) {
    console.error('歌单操作失败：', error)
    ElMessage.error(isEdit.value ? '修改歌单失败' : '创建歌单失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除歌单
const deletePlaylist = async (id) => {
  try {
    await ElMessageBox.confirm(
      '删除歌单后，其中的歌曲不会被删除，确定继续吗？',
      '删除歌单',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deletePlaylistApi(id)

    ElMessage.success('歌单删除成功')

    await fetchPlaylists()

  } catch (error) {
    if (
      error !== 'cancel' &&
      error !== 'close'
    ) {
      console.error('删除歌单失败：', error)
      ElMessage.error('删除歌单失败')
    }
  }
}

// 页面初始化
onMounted(() => {
  fetchPlaylists()
})
</script>

<style scoped>
.playlist-page {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  margin: 0;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.playlist-card {
  background: #fff;
  border-radius: 16px;
  padding: 12px;
  cursor: pointer;
  transition: 0.3s;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
}

.playlist-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(139, 92, 246, 0.15);
}

.cover {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  object-fit: cover;
}

.info h3 {
  margin: 12px 0 6px;
  font-size: 16px;
}

.info p {
  margin: 0;
  font-size: 12px;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 14px;
}

.card-actions .el-button {
  flex: 1;
  margin-left: 0;
}

.cover-tip {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}

@media (max-width: 600px) {
  .page-header h1 {
    font-size: 24px;
  }

  .grid-container {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }
}
</style>