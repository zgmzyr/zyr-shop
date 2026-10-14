/**
 * 商品 / 分类管理接口
 * @author zyr
 */
import service from './request'

// ================= 商品 =================

/** 分页查询商品列表（管理员） */
export const pageProducts = (params) => service.get('/admin/product/list', { params })

/** 商品详情 */
export const getProduct = (id) => service.get(`/admin/product/${id}`)

/** 新增商品 */
export const createProduct = (data) => service.post('/admin/product', data)

/** 更新商品 */
export const updateProduct = (id, data) => service.put(`/admin/product/${id}`, data)

/**
 * 修改商品上下架状态
 *
 * <p>注意：后端使用 @RequestParam 接收 status，必须用 query 参数，
 * 不能放 JSON body（否则后端报 "Required request parameter 'status' not present"）</p>
 */
export const changeProductStatus = (id, status) =>
  service.put(`/admin/product/${id}/status?status=${encodeURIComponent(status)}`)

/** 删除商品 */
export const deleteProduct = (id) => service.delete(`/admin/product/${id}`)

// ================= 分类 =================

/** 分类列表 */
export const listCategories = () => service.get('/admin/product/categories')

/**
 * 新增分类
 *
 * <p>注意：后端使用 @RequestParam 接收，前端必须用 query 参数</p>
 */
export const createCategory = (data) => {
  const q = new URLSearchParams()
  q.append('name', data.name || '')
  if (data.sort !== undefined && data.sort !== null) q.append('sort', data.sort)
  return service.post(`/admin/product/category?${q.toString()}`)
}

/**
 * 更新分类
 *
 * <p>注意：后端使用 @RequestParam 接收，前端必须用 query 参数</p>
 */
export const updateCategory = (id, data) => {
  const q = new URLSearchParams()
  if (data.name !== undefined) q.append('name', data.name ?? '')
  if (data.sort !== undefined && data.sort !== null) q.append('sort', data.sort)
  if (data.status !== undefined) q.append('status', data.status)
  return service.put(`/admin/product/category/${id}?${q.toString()}`)
}

/** 删除分类 */
export const deleteCategory = (id) =>
  service.delete(`/admin/product/category/${id}`)
