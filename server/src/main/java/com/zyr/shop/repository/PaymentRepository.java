package com.zyr.shop.repository;

import com.zyr.shop.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 支付记录数据访问接口
 *
 * @author zyr
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 按订单号查询支付记录
     *
     * @param orderNo 订单号
     * @return 支付记录
     */
    Optional<Payment> findByOrderNo(String orderNo);
}
