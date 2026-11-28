<template>
  <div class="layout">
    <header class="glass-header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🐱</span> 哈基米音乐
        </div>
        <nav class="nav-links">
          <router-link to="/">首页</router-link>
          <router-link to="/songs">发现</router-link>
          <router-link to="/upload">AI 翻唱</router-link>
          <router-link to="/creators">发现神人</router-link>
          <router-link to="/my-playlists">我的歌单</router-link>
        </nav>
        
        <!-- 右上角的用户信息区 -->
        <div class="user-actions">
          <template v-if="userStore.isLogin">
            <el-dropdown>
              <span class="user-profile">
                <el-avatar :size="32" :src="formatAvatar(userStore.userInfo?.avatarUrl)" />
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="openSettings">个人设置</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/my-favorites')">我的收藏</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/my-uploads')">我的上传</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/my-covers')">我的 AI 翻唱</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <el-button v-else round class="login-btn" @click="$router.push('/login')">登录 / 注册</el-button>
        </div>
      </div>
    </header>

        <!-- 个人设置弹窗 -->
    <el-dialog v-model="dialogVisible" title="修改个人信息" width="400px">
      <el-form label-width="80px">
        <el-form-item label="新昵称">
          <el-input v-model="editForm.nickname" placeholder="不填则不修改" />
        </el-form-item>
        <el-form-item label="新头像">
          <el-upload action="#" :auto-upload="false" :on-change="handleAvatarChange" :limit="1" list-type="picture-card">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 页面主体内容 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ref, reactive } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { updateProfileApi } from '@/api/user'

const dialogVisible = ref(false)
const editForm = reactive({ nickname: '' })
const avatarFile = ref(null)

const userStore = useUserStore()
const router = useRouter()

const formatAvatar = (url) => {
  if (!url) return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png' // 默认头像
  return url.replace('127.0.0.1', '26.89.197.25')
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}

const openSettings = () => {
  editForm.nickname = userStore.userInfo?.nickname || ''
  avatarFile.value = null
  dialogVisible.value = true
}

const handleAvatarChange = (file) => {
  avatarFile.value = file.raw
}

const submitProfile = async () => {
  const formData = new FormData()
  if (editForm.nickname) formData.append('nickname', editForm.nickname)
  if (avatarFile.value) formData.append('avatar', avatarFile.value)

  try {
    await updateProfileApi(formData)
    ElMessage.success('修改成功！')
    dialogVisible.value = false
    await userStore.fetchMe() // 重新拉取用户信息刷新头像
  } catch (error) { console.error(error) }
}

</script>

<style scoped>
.layout {
  min-height: 100vh;
  padding-bottom: 100px;
}

.glass-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.03);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.logo {
  font-size: 22px;
  font-weight: 800;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-links a {
  text-decoration: none;
  color: var(--text-muted);
  font-weight: 500;
  font-size: 15px;
  transition: all 0.3s ease;
  position: relative;
}

.nav-links a:hover, .nav-links a.router-link-active {
  color: #8b5cf6;
}

.nav-links a.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -24px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: var(--primary-gradient);
  border-radius: 2px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  outline: none;
}

.username {
  font-size: 14px;
  font-weight: 600;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(15px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}
</style>