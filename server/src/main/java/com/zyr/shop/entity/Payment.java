package com.zyr.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体
 *
 * <p>记录每笔订单的支付信息（支付宝交易号、金额、异步通知原文等）</p>
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "payment")
public class Payment {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 业务订单号 */
    @Column(name = "order_no", nullable = false, length = 32)
    private String orderNo;

    /** 支付用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 支付金额（元） */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** 支付方式：ALIPAY-支付宝 */
    @Column(name = "pay_type", nullable = false, length = 20)
    private String payType;

    /** 支付宝交易号（异步通知返回的 trade_no） */
    @Column(name = "trade_no", length = 64)
    private String tradeNo;

    /** 买家支付宝用户ID */
    @Column(name = "buyer_id", length = 32)
    private String buyerId;

    /** 支付状态：PENDING-待支付，SUCCESS-支付成功，FAILED-支付失败 */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** 支付完成时间 */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** 异步通知原始报文（便于排查问题） */
    @Column(name = "raw_notify", columnDefinition = "text")
    private String rawNotify;

    /** 创建时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 插入前填充时间与默认值 */
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
        if (this.payType == null) {
            this.payType = "ALIPAY";
        }
    }
}
