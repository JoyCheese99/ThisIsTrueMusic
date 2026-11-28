<template>
  <div class="my-covers-page">
    <div class="page-header">
      <h1>我的 AI 翻唱记录</h1>
      <p>你创造的所有哈基米声音都在这里，链接长期有效</p>
    </div>

    <div v-loading="loading" class="list-container">
      <!-- 如果报错了，展示错误状态，不让它白屏 -->
      <el-result v-if="isError" icon="error" title="加载失败" sub-title="请检查网络或稍后重试">
        <template #extra>
          <el-button type="primary" @click="fetchHistory">重新加载</el-button>
        </template>
      </el-result>

      <el-empty v-else-if="covers.length === 0 && !loading" description="你还没有生成过 AI 翻唱哦，快去试试吧！"></el-empty>
      
      <div v-else class="cover-list">
        <!-- 过滤掉没有 taskId 的脏数据 -->
        <template v-for="(item, index) in covers" :key="item.taskId || index">
          <el-card v-if="item" class="cover-card" shadow="hover">
            <div class="card-header">
              <div class="voice-tag">音色: {{ item.voice ? item.voice : '未知' }}</div>
              <div class="time">{{ formatTime(item.createdAt) }}</div>
            </div>
            
            <div v-if="item.status === 'DONE' && item.resultUrl">
              <audio :src="item.resultUrl" controls class="audio-player"></audio>
            </div>
            <div v-else class="status-error">
              任务状态: {{ item.status ? item.status : '未知' }} (音频未生成或正在处理)
            </div>

            <div class="card-footer">
              <span class="duration">
                耗时: {{ item.elapsed ? Math.round(item.elapsed) : '-' }} 秒
              </span>
              <div class="actions" v-if="item.status === 'DONE' && item.resultUrl">
                <a :href="item.resultUrl" target="_blank" download>
                  <el-button type="primary" size="small" round>下载成品</el-button>
                </a>
                <a v-if="item.taskId" :href="'/api/ai-cover/' + item.taskId + '/file'" target="_blank">
                  <el-button size="small" round>分享链接</el-button>
                </a>
              </div>
            </div>
          </el-card>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getHistoryApi } from '@/api/aiCover';

const covers = ref([]);
const loading = ref(false);
const isError = ref(false);

const formatTime = (timeStr) => {
  if (!timeStr) return '时间未知';
  try {
    const d = new Date(timeStr);
    return isNaN(d.getTime()) ? '时间异常' : d.toLocaleString();
  } catch (e) {
    return '时间异常';
  }
};

const fetchHistory = async () => {
  loading.value = true;
  isError.value = false;
  covers.value = []; // 先清空，防止脏数据残留

  try {
    const res = await getHistoryApi(1, 20);
    
    // 兜底逻辑：确保无论如何 covers 拿到的一定是数组
    let list = [];
    if (Array.isArray(res)) {
      list = res;
    } else if (res && Array.isArray(res.records)) {
      list = res.records;
    } else if (res && res.data && Array.isArray(res.data)) {
      list = res.data;
    }
    
    // 严格过滤 null 或 undefined
    covers.value = list.filter(item => item !== null && item !== undefined);
  } catch (error) {
    console.error('获取历史记录失败', error);
    isError.value = true; // 展示错误提示界面
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchHistory();
});
</script>

<style scoped>
/* 样式保持不变 */
.my-covers-page { max-width: 1000px; margin: 0 auto; }
.page-header { margin-bottom: 40px; }
.page-header h1 { font-size: 32px; font-weight: 800; margin: 0 0 8px 0; color: var(--text-main); }
.page-header p { color: var(--text-muted); font-size: 16px; margin: 0; }
.list-container { min-height: 300px; }
.cover-list { display: flex; flex-direction: column; gap: 20px; }
.cover-card { border-radius: 16px; border: none; box-shadow: 0 4px 20px rgba(0,0,0,0.04); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.voice-tag { background: rgba(139, 92, 246, 0.1); color: #8b5cf6; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600; }
.time { font-size: 13px; color: #94a3b8; }
.audio-player { width: 100%; height: 40px; outline: none; margin-bottom: 16px; }
.audio-player::-webkit-media-controls-panel { background-color: #f1f5f9; }
.status-error { margin-bottom: 16px; padding: 10px; background: #fef2f2; color: #ef4444; border-radius: 8px; font-size: 14px; text-align: center; }
.card-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed #e2e8f0; padding-top: 16px; }
.duration { font-size: 13px; color: var(--text-muted); }
.actions { display: flex; gap: 10px; }
.actions a { text-decoration: none; }
</style>