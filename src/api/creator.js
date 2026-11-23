import request from './request'

export const getCreatorsApi = (params) => request.get('/creators', { params })
export const getCreatorDetailApi = (id) => request.get(`/creators/${id}`)
export const getCreatorSongsApi = (id, params) => request.get(`/creators/${id}/songs`, { params })