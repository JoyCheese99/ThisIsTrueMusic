import request from './request'

// 获取我的上传 (文档 2.2)
export const getMyUploadsApi = (params) => request.get('/user/uploads', { params })

// 修改个人信息 (文档 2.4)
export const updateProfileApi = (formData) => request.post('/user/profile', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
