/**
 * 统计接口（数据看板）
 * @author zyr
 */
import service from './request'

/** 总览指标（用户数、商品数、订单数、销售额） */
export const overview = () => service.get('/stats/overview')

/** 近 N 天订单趋势 */
export const orderTrend = (days = 7) =>
  service.get('/stats/order-trend', { params: { days } })

/** 近 N 天销售额趋势 */
export const salesTrend = (days = 7) =>
  service.get('/stats/sales-trend', { params: { days } })

/** 商品分类分布 */
export const productCategoryDistribution = () =>
  service.get('/stats/product-category')

/** 用户角色分布 */
export const userRoleDistribution = () => service.get('/stats/user-role')

/** 近 N 天智能客服会话数 */
export const chatTrend = (days = 7) =>
  service.get('/stats/chat-trend', { params: { days } })

/** 知识库文档类型分布 */
export const documentTypeDistribution = () =>
  service.get('/stats/document-type')
