import { defineStore } from 'pinia'

export const usePlayerStore = defineStore('player', {
  state: () => ({
    currentSong: null, // 当前播放的歌曲对象
    playlist: [],      // 播放列表
    playing: false,    // 是否正在播放
    currentTime: 0,    // 当前进度
    duration: 0,     // 总时长
    volume: 0.8,      // 新增：音量 (0~1)
    showLyric: false
  }),
  actions: {
    playSong(song, list = []) {
      this.currentSong = song
      if (list.length > 0) this.playlist = list
      this.playing = true
    },
    next() {
      if (this.playlist.length === 0) return
      const idx = this.playlist.findIndex(s => s.id === this.currentSong.id)
      this.currentSong = this.playlist[(idx + 1) % this.playlist.length]
      this.playing = true
    },
    prev() {
      if (this.playlist.length === 0) return
      const idx = this.playlist.findIndex(s => s.id === this.currentSong.id)
      this.currentSong = this.playlist[(idx - 1 + this.playlist.length) % this.playlist.length]
      this.playing = true
    }
  }
})