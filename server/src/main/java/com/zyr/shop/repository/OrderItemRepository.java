package com.zyr.shop.repository;

import com.zyr.shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 订单项数据访问接口
 *
 * @author zyr
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 查询某订单的全部订单项
     *
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItem> findByOrderId(Long orderId);
}
