/**
 * 知识库管理接口
 * @author zyr
 */
import service from './request'

/** 分页查询知识库文档列表 */
export const pageDocuments = (params) =>
  service.get('/knowledge/page', { params })

/** 知识库文档详情 */
export const getDocument = (id) => service.get(`/knowledge/${id}`)

/** 文档分块列表 */
export const listChunks = (documentId) =>
  service.get(`/knowledge/${documentId}/chunks`)

/** 上传知识库文档（multipart/form-data） */
export const uploadDocument = (formData, onUploadProgress) =>
  service.post('/knowledge/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress
  })

/** 删除知识库文档 */
export const deleteDocument = (id) => service.delete(`/knowledge/${id}`)
