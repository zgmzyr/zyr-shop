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

import java.time.LocalDateTime;

/**
 * 商品分类实体
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "category")
public class Category {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分类名称 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 排序值（越小越靠前） */
    @Column(name = "sort")
    private Integer sort;

    /** 状态：1-启用，0-禁用 */
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
        if (this.sort == null) {
            this.sort = 0;
        }
    }

    /** 更新前刷新更新时间 */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
