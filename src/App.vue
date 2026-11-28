<template>
  <router-view />
  <GlobalPlayer />
  <FullScreenLyric />
</template>

<script setup>
import { onMounted } from 'vue'
import GlobalPlayer from '@/components/GlobalPlayer.vue'
import FullScreenLyric from '@/components/FullScreenLyric.vue' // 引入
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  if (userStore.isLogin) {
    userStore.fetchMe()
  }
})
</script>