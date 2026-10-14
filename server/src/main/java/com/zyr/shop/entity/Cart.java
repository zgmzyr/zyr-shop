package com.zyr.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车实体（每个用户一辆购物车，按用户ID唯一）
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "cart")
public class Cart {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 创建时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 插入前填充创建时间 */
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}
