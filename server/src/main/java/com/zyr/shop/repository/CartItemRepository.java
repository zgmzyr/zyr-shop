package com.zyr.shop.repository;

import com.zyr.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 购物车项数据访问接口
 *
 * @author zyr
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 查询某购物车的全部商品项，按加入时间倒序
     *
     * @param cartId 购物车ID
     * @return 购物车项列表
     */
    List<CartItem> findByCartIdOrderByCreateTimeDesc(Long cartId);

    /**
     * 查询购物车中是否已有该商品（用于"加购时合并数量"）
     *
     * @param cartId    购物车ID
     * @param productId 商品ID
     * @return 购物车项
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * 删除某购物车的全部商品项（下单后清空购物车使用）
     *
     * @param cartId 购物车ID
     */
    void deleteByCartId(Long cartId);
}
