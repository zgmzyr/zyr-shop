/**
 * 用户状态管理（Pinia）
 * 登录令牌持久化在 localStorage，刷新页面不丢失
 * @author zyr
 */
import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 登录令牌
    token: localStorage.getItem('zyr_shop_token') || '',
    // 当前用户信息
    userInfo: JSON.parse(localStorage.getItem('zyr_shop_user') || 'null')
  }),

  getters: {
    // 是否已登录
    isLogin: (state) => !!state.token,
    // 当前角色
    role: (state) => state.userInfo?.role || '',
    // 是否管理员
    isAdmin: (state) => state.userInfo?.role === 'ADMIN',
    // 头像
    avatar: (state) => state.userInfo?.avatar || '',
    // 昵称（没有昵称时用用户名）
    nickname: (state) => state.userInfo?.nickname || state.userInfo?.username || '',
    // 用户名
    username: (state) => state.userInfo?.username || ''
  },

  actions: {
    /**
     * 登录
     * @param {{username:string, password:string}} form 登录表单
     */
    async login(form) {
      const { data } = await loginApi(form)
      this.token = data.token
      this.userInfo = data.user
      localStorage.setItem('zyr_shop_token', data.token)
      localStorage.setItem('zyr_shop_user', JSON.stringify(data.user))
    },

    /**
     * 从后端重新拉取用户信息（刷新页面后同步状态）
     */
    async fetchUserInfo() {
      const { data } = await getUserInfo()
      this.userInfo = data
      localStorage.setItem('zyr_shop_user', JSON.stringify(data))
    },

    /**
     * 更新本地用户信息（修改资料后）
     */
    setUserInfo(user) {
      this.userInfo = user
      localStorage.setItem('zyr_shop_user', JSON.stringify(user))
    },

    /**
     * 退出登录
     */
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('zyr_shop_token')
      localStorage.removeItem('zyr_shop_user')
    }
  }
})
