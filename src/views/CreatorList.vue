<template>
  <div class="creator-page">
    <div class="page-header"><h1>发现神人</h1></div>
    <div class="grid-container">
      <div v-for="c in creators" :key="c.id" class="creator-card" @click="$router.push(`/creator/${c.id}`)">
        <el-avatar :size="80" :src="formatUrl(c.avatarUrl)">{{ c.nickname.charAt(0) }}</el-avatar>
        <h3>{{ c.nickname }}</h3>
        <p>共上传 {{ c.songCount }} 首歌曲</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCreatorsApi } from '@/api/creator'

const creators = ref([])
const formatUrl = (url) => url ? url.replace('127.0.0.1', '26.89.197.25') : ''

onMounted(async () => {
  const res = await getCreatorsApi({ page: 1, size: 20 })
  creators.value = res.records
})
</script>

<style scoped>
.grid-container { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
.creator-card { background: #fff; border-radius: 16px; padding: 30px 20px; text-align: center; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 20px rgba(0,0,0,0.04); }
.creator-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(139, 92, 246, 0.15); }
.creator-card h3 { margin: 16px 0 8px; font-size: 18px; }
.creator-card p { color: #888; font-size: 13px; margin: 0; }
</style>