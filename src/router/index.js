import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') }, // 新增注册
  {
    path: '/',
    component: () => import('@/components/AppHeader.vue'),
    children: [
      { path: '', component: () => import('@/views/Home.vue') },
      { path: 'songs', component: () => import('@/views/SongList.vue') },
      { path: 'song/:id', component: () => import('@/views/SongDetail.vue') },
      { path: 'upload', component: () => import('@/views/Upload.vue'), meta: { auth: true } },
      { path: 'my-favorites', component: () => import('@/views/MyFavorites.vue'), meta: { auth: true } },
      { path: 'my-uploads', component: () => import('@/views/MyUploads.vue'), meta: { auth: true } },
      { path: 'my-playlists', component: () => import('@/views/MyPlaylists.vue'), meta: { auth: true } },
      { path: 'playlist/:id', component: () => import('@/views/PlaylistDetail.vue'), meta: { auth: true } },
      { path: 'creators', component: () => import('@/views/CreatorList.vue') },
      { path: 'creator/:id', component: () => import('@/views/CreatorDetail.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  if (to.meta.auth && !token) return '/login'
})

export default router