import request from './request'

// 获取音乐列表
export const getSongsApi = (params) => request.get('/songs', { params })

// 获取歌曲详情 (带 token 会返回是否点赞/收藏)
export const getSongDetailApi = (id) => request.get(`/songs/${id}`)

// 获取我的收藏
export const getMyFavoritesApi = (params) => request.get('/user/favorites', { params })

// 互动接口
export const likeSongApi = (id) => request.post(`/songs/${id}/like`)
export const favoriteSongApi = (id) => request.post(`/songs/${id}/favorite`)
export const getCommentsApi = (id, params) => request.get(`/songs/${id}/comments`, { params })
export const postCommentApi = (id, data) => request.post(`/songs/${id}/comments`, data)

// 上传接口 (form-data)
export const uploadSongApi = (formData) => request.post('/songs', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
// 删除自己上传的歌曲
export const deleteSongApi = (id) =>
  request.delete(`/songs/${id}`)

// 上报歌曲播放记录
export const reportSongPlayApi = (id) =>
  request.post(`/songs/${id}/play`)