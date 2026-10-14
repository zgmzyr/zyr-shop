/**
 * 订单管理接口
 * @author zyr
 */
import service from './request'

/** 分页查询订单列表（管理员） */
export const pageOrders = (params) => service.get('/admin/order/list', { params })

/** 订单详情 */
export const getOrder = (orderNo) => service.get(`/admin/order/${orderNo}`)

/**
 * 修改订单状态
 *
 * <p>注意：后端使用 @RequestParam 接收 status，必须用 query 参数或 form-urlencoded，
 * 不能放 JSON body（否则后端报 "Required request parameter 'status' not present"）</p>
 */
export const updateOrderStatus = (orderNo, status) =>
  service.put(`/admin/order/${orderNo}/status?status=${encodeURIComponent(status)}`)

/**
 * 管理员手动确认订单已支付
 *
 * <p>无内网穿透时的学习用兜底：沙箱付款成功后管理员点此按钮把订单标记为 PAID 并写支付流水</p>
 */
export const markOrderPaid = (orderNo) =>
  service.put(`/admin/order/${orderNo}/mark-paid`)
