/**
 * 用户管理接口
 * @author zyr
 */
import service from './request'

/** 分页查询用户列表 */
export const pageUsers = (params) => service.get('/admin/user/list', { params })

/**
 * 新增用户
 *
 * <p>注意：后端使用 @RequestParam 接收，前端必须用 query 参数，
 * 不能放 JSON body（否则后端报 "Required request parameter 'username' not present"）</p>
 */
export const createUser = (data) => {
  const q = new URLSearchParams()
  q.append('username', data.username || '')
  if (data.nickname) q.append('nickname', data.nickname)
  if (data.role) q.append('role', data.role)
  if (data.phone) q.append('phone', data.phone)
  if (data.address) q.append('address', data.address)
  return service.post(`/admin/user?${q.toString()}`)
}

/**
 * 更新用户
 *
 * <p>注意：后端使用 @RequestParam 接收，前端必须用 query 参数</p>
 */
export const updateUser = (id, data) => {
  const q = new URLSearchParams()
  if (data.nickname !== undefined) q.append('nickname', data.nickname ?? '')
  if (data.role !== undefined) q.append('role', data.role)
  if (data.phone !== undefined) q.append('phone', data.phone ?? '')
  if (data.address !== undefined) q.append('address', data.address ?? '')
  if (data.status !== undefined) q.append('status', data.status)
  return service.put(`/admin/user/${id}?${q.toString()}`)
}

/**
 * 重置密码为 123456
 *
 * <p>后端固定重置为 123456，无需前端传 password 参数</p>
 */
export const resetPassword = (id) =>
  service.put(`/admin/user/${id}/password/reset`)

/** 删除用户 */
export const deleteUser = (id) => service.delete(`/admin/user/${id}`)
