package com.zyr.shop.dto;

import com.zyr.shop.entity.Order;
import com.zyr.shop.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象（含订单项列表）
 *
 * @author zyr
 */
@Data
public class OrderVO {

    /** 订单ID */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 用户名（管理员订单列表展示用，由 Service 层填充） */
    private String username;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 订单状态 */
    private String status;

    /** 收货地址 */
    private String address;

    /** 收货人手机号 */
    private String phone;

    /** 收货人 */
    private String receiver;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 支付时间 */
    private LocalDateTime paidAt;

    /** 订单项列表 */
    private List<OrderItem> items;

    /**
     * 由实体构造（不含订单项，需外部填充）
     *
     * @param o 订单实体
     * @return 视图对象
     */
    public static OrderVO from(Order o) {
        if (o == null) {
            return null;
        }
        OrderVO vo = new OrderVO();
        vo.id = o.getId();
        vo.orderNo = o.getOrderNo();
        vo.userId = o.getUserId();
        vo.totalAmount = o.getTotalAmount();
        vo.status = o.getStatus();
        vo.address = o.getAddress();
        vo.phone = o.getPhone();
        vo.receiver = o.getReceiver();
        vo.remark = o.getRemark();
        vo.createTime = o.getCreateTime();
        vo.paidAt = o.getPaidAt();
        return vo;
    }
}
