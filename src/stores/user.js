import { defineStore } from 'pinia'
import request from '@/api/request' // 引入我们封装的 axios

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  getters: {
    isLogin: state => !!state.token,
    // 如果后端没有写 role 字段，这个先保留但不会生效，或者直接删掉
    isAdmin: state => state.userInfo?.role === 'ADMIN' 
  },
  actions: {
    setLogin(data) {
      // data 结构参考文档：{ token, user: {...} }
      this.token = data.token
      this.userInfo = data.user
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(data.user))
    },
    
    // 新增：页面刷新时，向后端获取最新的个人信息 (接口 2.1)
    async fetchMe() {
      if (!this.token) return
      try {
        const res = await request.get('/user/me')
        this.userInfo = res
        localStorage.setItem('userInfo', JSON.stringify(res))
      } catch (error) {
        console.error('获取用户信息失败', error)
      }
    },

    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})