<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <div class="card-header">
        <h2>注册哈基米账号</h2>
        <p>加入全网最魔性的音乐社区</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" :prefix-icon="Postcard" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%; font-weight: bold;" :loading="loading" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="footer-links">
        <span>已有账号？</span>
        <el-link type="primary" :underline="false" @click="$router.push('/login')">去登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Postcard } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { registerApi } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', nickname: '', password: '' })
const rules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleRegister = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await registerApi(form)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh; display: flex; justify-content: center; align-items: center;
  background-color: var(--bg-color);
  background-image: radial-gradient(at 0% 0%, hsla(253,16%,7%,0) 0, transparent 50%), radial-gradient(at 50% 0%, hsla(225,39%,30%,0) 0, transparent 50%), radial-gradient(at 100% 0%, hsla(339,49%,30%,0) 0, transparent 50%);
}
.login-card {
  width: 420px; border-radius: 20px; background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.5); box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
}
.card-header { text-align: center; margin-bottom: 30px; margin-top: 10px; }
.card-header h2 { margin: 0 0 8px 0; font-size: 28px; font-weight: 800; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.card-header p { color: var(--text-muted); font-size: 14px; margin: 0; }
.footer-links { text-align: center; font-size: 14px; color: #666; margin-top: 10px; display: flex; justify-content: center; gap: 8px; }
</style>