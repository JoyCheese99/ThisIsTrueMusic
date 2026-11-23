import request from './request'

// 获取音色列表
export const listVoicesApi = () => request.get('/ai-cover/voices')

// 提交翻唱任务 (支持上传进度回调)
export const createCoverApi = (formData, onUploadProgress) => {
  return request.post('/ai-cover', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress
  })
}

// 查询任务进度
export const getTaskApi = (taskId) => request.get(`/ai-cover/${taskId}`)

// 获取历史记录 (可选)
export const getHistoryApi = (page = 1, size = 20) => request.get('/ai-cover/history', { params: { page, size } })