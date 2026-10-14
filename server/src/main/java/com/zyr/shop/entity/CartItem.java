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
 * 购物车项实体（一条购物车记录对应一个商品及数量）
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "cart_item")
public class CartItem {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属购物车ID */
    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    /** 商品ID */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 加入购物车时的商品单价（快照，避免商品改价后老购物车被影响） */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 购买数量 */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** 加入时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 插入前填充时间与默认数量 */
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        if (this.quantity == null) {
            this.quantity = 1;
        }
    }
}
