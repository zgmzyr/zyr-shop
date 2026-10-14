/**
 * 认证相关接口
 * @author zyr
 */
import service from './request'

/** 登录 */
export const login = (data) => service.post('/auth/login', data)

/** 获取当前登录用户信息 */
export const getUserInfo = () => service.get('/auth/info')

/** 修改个人密码 */
export const changePassword = (data) =>
  service.put('/auth/password', data)

/** 更新个人基本资料（昵称、手机号、收货地址） */
export const updateProfile = (params) =>
  service.put('/auth/profile', null, { params })
