/**
 * Axios 请求统一封装
 * - 自动携带 JWT 令牌
 * - 统一解包后端 Result 结构
 * - 401 自动跳转登录
 * @author zyr
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 120000
})

// 请求拦截器：附带登录令牌
service.interceptors.request.use((config) => {
  const token = localStorage.getItem('zyr_shop_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理业务状态码
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 文件流等非标准结构直接返回
    if (res instanceof Blob) return response
    if (res.code === 200) {
      return res
    }
    // 业务失败提示
    ElMessage.error(res.msg || res.message || '请求失败')
    return Promise.reject(new Error(res.msg || res.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.msg || error.response?.data?.message
    if (status === 401) {
      ElMessage.error(msg || '登录已过期，请重新登录')
      localStorage.removeItem('zyr_shop_token')
      localStorage.removeItem('zyr_shop_user')
      router.push('/login')
    } else {
      ElMessage.error(msg || '网络异常，请稍后再试')
    }
    return Promise.reject(error)
  }
)

export default service
