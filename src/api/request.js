import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000 // 延长一点超时时间，防止服务器慢报错
})

// 请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  res => {
    const data = res.data
    // 后端统一返回 { code: 200, msg: '...', data: {...} }
    if (data.code === 200) {
      return data.data // 这里直接解包，所以页面里拿到的就是真正的 data
    }
    
    // 如果不是 200，提示错误
    ElMessage.error(data.msg || '请求失败')
    if (data.code === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    return Promise.reject(new Error(data.msg || 'Error'))
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(err.response?.data?.msg || '网络错误')
    }
    return Promise.reject(err)
  }
)

// ⚠️ 就是这一行缺失了！必须要有
export default request