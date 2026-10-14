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
 * 订单项实体（订单中一个商品快照行，包含下单时的商品名与单价，避免商品改价/下架影响历史订单）
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "order_item")
public class OrderItem {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属订单ID */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** 商品ID */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 商品名快照 */
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    /** 下单单价快照 */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 购买数量 */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** 创建时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 插入前填充时间 */
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}
