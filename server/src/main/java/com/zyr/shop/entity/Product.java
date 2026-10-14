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
 * 商品实体
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "product")
public class Product {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属分类ID */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** 商品名称 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 商品描述（简介） */
    @Column(name = "description", length = 1000)
    private String description;

    /** 商品价格（单位：元，保留两位小数） */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** 库存数量 */
    @Column(name = "stock", nullable = false)
    private Integer stock;

    /** 销量（已售件数，用于排序） */
    @Column(name = "sales")
    private Integer sales;

    /** 封面图片访问路径 */
    @Column(name = "cover_image", length = 500)
    private String coverImage;

    /** 状态：1-上架，0-下架 */
    @Column(name = "status", nullable = false)
    private Integer status;

    /** 创建时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /** 插入前填充时间与默认值 */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        if (this.status == null) {
            this.status = 1;
        }
        if (this.sales == null) {
            this.sales = 0;
        }
        if (this.stock == null) {
            this.stock = 0;
        }
    }

    /** 更新前刷新更新时间 */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
