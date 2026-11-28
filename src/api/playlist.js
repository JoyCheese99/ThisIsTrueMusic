import request from './request'

export const createPlaylistApi = (formData) => request.post('/playlists', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
export const getMyPlaylistsApi = () => request.get('/playlists')
export const addSongToPlaylistApi = (playlistId, songId) => request.post(`/playlists/${playlistId}/songs/${songId}`)
export const getPlaylistSongsApi = (playlistId, params) => request.get(`/playlists/${playlistId}/songs`, { params })
// 5. 修改歌单
export const updatePlaylistApi = (id, formData) =>
  request.put(`/playlists/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

// 6. 删除歌单
export const deletePlaylistApi = (id) =>
  request.delete(`/playlists/${id}`)

// 7. 从歌单中移除歌曲
export const removeSongFromPlaylistApi = (playlistId, songId) =>
  request.delete(`/playlists/${playlistId}/songs/${songId}`)
