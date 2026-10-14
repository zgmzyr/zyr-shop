package com.zyr.shop.repository;

import com.zyr.shop.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 系统用户数据访问接口
 *
 * @author zyr
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 按用户名查询用户（登录使用）
     *
     * @param username 用户名
     * @return 用户实体
     */
    Optional<SysUser> findByUsername(String username);

    /**
     * 统计指定角色的用户数（首页环形图使用）
     *
     * @param role 角色
     * @return 数量
     */
    long countByRole(String role);
}
