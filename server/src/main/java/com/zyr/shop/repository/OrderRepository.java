package com.zyr.shop.repository;

import com.zyr.shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问接口
 *
 * @author zyr
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 按订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 用户查询自己的订单（按状态可选过滤），按创建时间倒序
     *
     * @param userId 用户ID
     * @param status 订单状态（可空）
     * @param pageable 分页参数
     * @return 订单分页结果
     */
    @Query("select o from Order o where o.userId = :userId " +
            "and (:status is null or :status = '' or o.status = :status) " +
            "order by o.id desc")
    Page<Order> searchByUser(@Param("userId") Long userId,
                             @Param("status") String status,
                             Pageable pageable);

    /**
     * 管理员订单分页查询（支持按订单号、状态过滤）
     *
     * @param orderNo 订单号关键词（可空）
     * @param status  订单状态（可空）
     * @param pageable 分页参数
     * @return 订单分页结果
     */
    @Query("select o from Order o where (:orderNo is null or :orderNo = '' " +
            "or o.orderNo like concat('%', :orderNo, '%')) " +
            "and (:status is null or :status = '' or o.status = :status) " +
            "order by o.id desc")
    Page<Order> searchAdmin(@Param("orderNo") String orderNo,
                            @Param("status") String status,
                            Pageable pageable);

    /**
     * 统计指定时间后的下单数量（首页指标）
     *
     * @param time 起始时间
     * @return 数量
     */
    long countByCreateTimeAfter(LocalDateTime time);

    /**
     * 按天统计订单数（首页折线图）
     *
     * @param start 起始时间
     * @return 每行：日期字符串(yyyy-MM-dd)、数量
     */
    @Query(value = "select date_format(create_time, '%Y-%m-%d') as d, count(*) as c " +
            "from shop_order where create_time >= :start group by d order by d",
            nativeQuery = true)
    List<Object[]> countDailyAfter(@Param("start") LocalDateTime start);

    /**
     * 按天统计销售额（首页柱状图，仅统计已支付订单）
     *
     * @param start 起始时间
     * @return 每行：日期字符串(yyyy-MM-dd)、销售额
     */
    @Query(value = "select date_format(create_time, '%Y-%m-%d') as d, ifnull(sum(total_amount),0) as s " +
            "from shop_order where status in ('PAID','SHIPPED','COMPLETED') and create_time >= :start group by d order by d",
            nativeQuery = true)
    List<Object[]> sumDailySalesAfter(@Param("start") LocalDateTime start);

    /**
     * 累计销售额（首页指标，仅已支付订单）
     *
     * @return 销售额合计
     */
    @Query("select ifnull(sum(o.totalAmount),0) from Order o where o.status in ('PAID','SHIPPED','COMPLETED')")
    Double sumTotalSales();
}
