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
 * 系统用户实体（管理员 ADMIN / 普通用户 USER）
 *
 * @author zyr
 */
@Data
@Entity
@Table(name = "sys_user")
public class SysUser {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录用户名（唯一） */
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    /** 登录密码（MD5 加密存储） */
    @Column(name = "password", nullable = false, length = 64)
    private String password;

    /** 用户昵称 */
    @Column(name = "nickname", length = 50)
    private String nickname;

    /** 头像访问路径 */
    @Column(name = "avatar", length = 255)
    private String avatar;

    /** 角色：ADMIN-管理员，USER-普通用户 */
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    /** 手机号 */
    @Column(name = "phone", length = 20)
    private String phone;

    /** 收货地址（用户下单时默认填充） */
    @Column(name = "address", length = 255)
    private String address;

    /** 状态：1-正常，0-禁用 */
    @Column(name = "status", nullable = false)
    private Integer status;

    /** 创建时间 */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 插入前自动填充创建时间与更新时间，默认状态正常、默认普通用户角色
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        if (this.status == null) {
            this.status = 1;
        }
        if (this.role == null) {
            this.role = "USER";
        }
    }

    /**
     * 更新前自动刷新更新时间
     */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
