package com.zyr.shop.repository;

import com.zyr.shop.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 购物车数据访问接口
 *
 * @author zyr
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 按用户ID查询购物车（每个用户一辆购物车）
     *
     * @param userId 用户ID
     * @return 购物车实体
     */
    Optional<Cart> findByUserId(Long userId);
}
