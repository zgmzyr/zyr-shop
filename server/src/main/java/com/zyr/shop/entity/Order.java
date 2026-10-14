package com.zyr.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 *
 * <p>订单状态机：PENDING(待支付) → PAID(已支付) → SHIPPED(已发货) → COMPLETED(已完成)，
 * 任意状态均可 CANCELLED(已取消)</p>
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "shop_order")
public class Order {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单号（业务唯一，生成规则：时间戳+用户ID后4位+随机数） */
    @Column(name = "order_no", nullable = false, length = 32, unique = true)
    private String orderNo;

    /** 下单用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 订单总金额（元） */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** 订单状态：PENDING/PAID/SHIPPED/COMPLETED/CANCELLED */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** 收货地址 */
    @Column(name = "address", length = 500)
    private String address;

    /** 收货人手机号 */
    @Column(name = "phone", length = 20)
    private String phone;

    /** 收货人姓名 */
    @Column(name = "receiver", length = 50)
    private String receiver;

    /** 备注（用户留言） */
    @Column(name = "remark", length = 500)
    private String remark;

    /** 创建时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 支付时间 */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** 更新时间 */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /** 插入前填充时间与默认状态 */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    /** 更新前刷新更新时间 */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
