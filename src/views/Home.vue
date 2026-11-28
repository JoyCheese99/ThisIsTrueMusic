<template>
  <div class="home-hero">
    <div class="hero-content">
      <h1>让每一首歌<br>都变成 <span class="gradient-text">哈基米</span> 的声音</h1>
      <p>极致的音乐分享社区，结合 RVC 声音转换技术<br>上传你的音乐，一键生成ai翻唱</p>
      
      <div class="actions">
        <el-button type="primary" size="large" round class="cta-btn" @click="$router.push('/songs')">
          开始听歌
        </el-button>
        <el-button size="large" round class="cta-btn-outline" @click="$router.push('/upload')">
          去试试 AI 翻唱
        </el-button>
      </div>
    </div>
    
    <div class="hero-image">
      <img src="https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800&q=80" alt="Hakimi" class="floating-img" />
    </div>
  </div>
</template>

<style scoped>
.home-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: calc(100vh - 200px);
  padding: 0 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-content {
  flex: 1;
  max-width: 600px;
}

.badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(236, 72, 153, 0.1);
  color: #ec4899;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 24px;
}

h1 {
  font-size: 56px;
  line-height: 1.2;
  font-weight: 800;
  color: var(--text-main);
  margin: 0 0 24px 0;
}

.gradient-text {
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

p {
  font-size: 18px;
  color: var(--text-muted);
  line-height: 1.6;
  margin-bottom: 40px;
}

.actions {
  display: flex;
  gap: 16px;
}

.cta-btn {
  padding: 24px 40px;
  font-size: 16px;
  font-weight: 600;
}

.cta-btn-outline {
  padding: 24px 40px;
  font-size: 16px;
  font-weight: 600;
  background: white;
  border: 1px solid #e2e8f0;
}

.cta-btn-outline:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.hero-image {
  flex: 1;
  display: flex;
  justify-content: center;
  position: relative;
}

.hero-image::before {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  background: var(--primary-gradient);
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.2;
  z-index: -1;
}

.floating-img {
  width: 400px;
  height: 400px;
  object-fit: cover;
  border-radius: 30px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
  100% { transform: translateY(0px); }
}
</style>