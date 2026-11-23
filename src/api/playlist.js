import request from './request'

export const createPlaylistApi = (formData) => request.post('/playlists', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
export const getMyPlaylistsApi = () => request.get('/playlists')
export const addSongToPlaylistApi = (playlistId, songId) => request.post(`/playlists/${playlistId}/songs/${songId}`)
export const getPlaylistSongsApi = (playlistId, params) => request.get(`/playlists/${playlistId}/songs`, { params })