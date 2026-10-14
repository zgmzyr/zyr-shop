package com.zyr.shop.service;

import com.zyr.shop.common.BusinessException;
import com.zyr.shop.common.PageResult;
import com.zyr.shop.dto.CartItemVO;
import com.zyr.shop.dto.OrderRequest;
import com.zyr.shop.dto.OrderVO;
import com.zyr.shop.entity.CartItem;
import com.zyr.shop.entity.Order;
import com.zyr.shop.entity.OrderItem;
import com.zyr.shop.entity.Payment;
import com.zyr.shop.entity.SysUser;
import com.zyr.shop.repository.CartItemRepository;
import com.zyr.shop.repository.CartRepository;
import com.zyr.shop.repository.OrderItemRepository;
import com.zyr.shop.repository.OrderRepository;
import com.zyr.shop.repository.PaymentRepository;
import com.zyr.shop.repository.ProductRepository;
import com.zyr.shop.repository.SysUserRepository;
import com.zyr.shop.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务
 *
 * <p>核心交易链路：</p>
 * <ol>
 *     <li>下单：取购物车→校验库存→扣库存(reduceStock乐观更新)→写订单与订单项→清空购物车</li>
 *     <li>用户查单：分页查询本人订单</li>
 *     <li>管理员改状态：发货、完成、取消</li>
 *     <li>支付回调标记已支付：由 AlipayService 调用 markPaid</li>
 * </ol>
 *
 * <p>订单状态机：PENDING(待支付) → PAID(已支付) → SHIPPED(已发货) → COMPLETED(已完成)；任意状态可 CANCELLED</p>
 *
 * @author zyr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    /** 订单数据访问 */
    private final OrderRepository orderRepository;

    /** 订单项数据访问 */
    private final OrderItemRepository orderItemRepository;

    /** 购物车数据访问 */
    private final CartRepository cartRepository;

    /** 购物车项数据访问 */
    private final CartItemRepository cartItemRepository;

    /** 商品数据访问 */
    private final ProductRepository productRepository;

    /** 支付流水数据访问（手动确认支付时写流水） */
    private final PaymentRepository paymentRepository;

    /** 系统用户数据访问（管理员订单列表展示用户名） */
    private final SysUserRepository userRepository;

    /** 商品服务（用于扣减库存） */
    private final ProductService productService;

    /**
     * 用户下单（基于购物车）
     *
     * @param userId  用户ID
     * @param request 下单参数
     * @return 订单视图
     */
    @Transactional
    public OrderVO createOrder(Long userId, OrderRequest request) {
        // 1. 取购物车，校验非空
        var cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(400, "购物车为空，无法下单"));
        List<CartItem> items = cartItemRepository.findByCartIdOrderByCreateTimeDesc(cart.getId());
        if (items.isEmpty()) {
            throw new BusinessException(400, "购物车为空，无法下单");
        }

        // 2. 扣减库存（任何一项失败抛异常回滚事务）
        for (CartItem item : items) {
            int affected = productService.reduceStock(item.getProductId(), item.getQuantity());
            if (affected == 0) {
                throw new BusinessException(400, "商品库存不足（ID=" + item.getProductId() + "）");
            }
        }

        // 3. 生成订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo(userId));
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setReceiver(request.getReceiver());
        order.setPhone(request.getPhone());
        order.setAddress(request.getAddress());
        order.setRemark(request.getRemark());
        // 总金额：购物车项小计之和
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        orderRepository.save(order);
        log.info("【订单】用户 {} 创建订单 {} 金额 {}", userId, order.getOrderNo(), total);

        // 4. 写订单项（带商品名快照）
        for (CartItem item : items) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(item.getProductId());
            var product = productRepository.findById(item.getProductId()).orElse(null);
            oi.setProductName(product != null ? product.getName() : "已删除商品");
            oi.setPrice(item.getPrice());
            oi.setQuantity(item.getQuantity());
            orderItemRepository.save(oi);
        }

        // 5. 清空购物车
        cartItemRepository.deleteByCartId(cart.getId());

        OrderVO vo = OrderVO.from(order);
        vo.setItems(orderItemRepository.findByOrderId(order.getId()));
        return vo;
    }

    /**
     * 用户分页查询订单
     *
     * @param userId 用户ID
     * @param status 订单状态（可空）
     * @param page   页码
     * @param size   每页条数
     * @return 订单分页结果
     */
    public PageResult<OrderVO> searchByUser(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Order> p = orderRepository.searchByUser(userId, status, pageable);
        List<OrderVO> list = p.getContent().stream().map(o -> {
            OrderVO vo = OrderVO.from(o);
            vo.setItems(orderItemRepository.findByOrderId(o.getId()));
            return vo;
        }).toList();
        PageResult<OrderVO> result = new PageResult<>();
        result.setPage((long) (p.getNumber() + 1));
        result.setSize((long) p.getSize());
        result.setTotal(p.getTotalElements());
        result.setRecords(list);
        return result;
    }

    /**
     * 订单详情
     *
     * @param userId 用户ID（用于校验订单归属）
     * @param orderNo 订单号
     * @return 订单视图
     */
    public OrderVO detail(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!order.getUserId().equals(userId) && !UserContext.isAdmin()) {
            // 普通用户只能查自己的订单
            throw new BusinessException(403, "无权查看此订单");
        }
        OrderVO vo = OrderVO.from(order);
        // 填充用户名（管理员查看订单详情时展示）
        userRepository.findById(order.getUserId())
                .ifPresent(u -> vo.setUsername(u.getUsername()));
        vo.setItems(orderItemRepository.findByOrderId(order.getId()));
        return vo;
    }

    /**
     * 用户取消订单（仅待支付状态可取消，并回补库存）
     *
     * @param userId  用户ID
     * @param orderNo 订单号
     */
    @Transactional
    public void cancelByUser(Long userId, String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
            throw new BusinessException(400, "当前订单状态不可取消");
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        // 回补库存（仅当原状态不是 PENDING 时才需要回补？此处简化：取消即回补库存）
        rollbackStock(order.getId());
        log.info("【订单】用户 {} 取消订单 {}", userId, orderNo);
    }

    /**
     * 管理员分页查询订单
     *
     * @param orderNo 订单号关键词
     * @param status  状态
     * @param page    页码
     * @param size    每页条数
     * @return 订单分页结果
     */
    public PageResult<OrderVO> searchAdmin(String orderNo, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Order> p = orderRepository.searchAdmin(orderNo, status, pageable);
        // 批量查询用户名，避免 N+1 查询问题
        List<Long> userIds = p.getContent().stream().map(Order::getUserId).distinct().toList();
        Map<Long, String> usernameMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername, (a, b) -> a));
        List<OrderVO> list = p.getContent().stream().map(o -> {
            OrderVO vo = OrderVO.from(o);
            vo.setUsername(usernameMap.get(o.getUserId()));
            vo.setItems(orderItemRepository.findByOrderId(o.getId()));
            return vo;
        }).toList();
        PageResult<OrderVO> result = new PageResult<>();
        result.setPage((long) (p.getNumber() + 1));
        result.setSize((long) p.getSize());
        result.setTotal(p.getTotalElements());
        result.setRecords(list);
        return result;
    }

    /**
     * 管理员更新订单状态（发货、完成等）
     *
     * @param orderNo 订单号
     * @param status  新状态
     */
    @Transactional
    public void updateStatus(String orderNo, String status) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        order.setStatus(status);
        orderRepository.save(order);
        log.info("【订单】管理员修改订单 {} 状态为 {}", orderNo, status);
    }

    /**
     * 标记订单已支付（支付宝异步回调调用）
     *
     * <p>幂等：若订单已不是 PENDING 则直接返回，避免重复回调</p>
     *
     * @param orderNo 订单号
     */
    @Transactional
    public void markPaid(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!"PENDING".equals(order.getStatus())) {
            log.info("【订单】订单 {} 当前状态 {}，跳过支付回调", orderNo, order.getStatus());
            return;
        }
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("【订单】订单 {} 标记为已支付", orderNo);
    }

    /**
     * 管理员手动确认订单已支付（无内网穿透/异步回调时的学习用兜底方案）
     *
     * <p>由管理员后台触发，等价于支付宝异步回调 {@link #markPaid}，但额外：</p>
     * <ol>
     *     <li>校验订单状态必须为 PENDING（防止重复确认）</li>
     *     <li>更新订单状态为 PAID，写入 paidAt</li>
     *     <li>写一条 payment 流水：payType=MANUAL，tradeNo=MANUAL_yyyyMMddHHmmss，
     *         status=SUCCESS，rawNotify 注明"管理员手动确认"</li>
     * </ol>
     *
     * @param orderNo     订单号
     * @param operatorId  操作人ID（管理员）— 用于日志追溯
     */
    @Transactional
    public void markPaidManually(String orderNo, Long operatorId) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        // 幂等性：非 PENDING 状态不允许手动确认
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(400,
                    "订单当前状态为 " + order.getStatus() + "，仅待支付订单可手动确认");
        }
        // 1. 更新订单状态
        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        // 2. 写一条 MANUAL 支付流水
        String tradeNo = "MANUAL_" + LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Payment payment = new Payment();
        payment.setOrderNo(orderNo);
        payment.setUserId(order.getUserId());
        payment.setAmount(order.getTotalAmount());
        payment.setPayType("MANUAL");
        payment.setTradeNo(tradeNo);
        payment.setStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now());
        payment.setRawNotify("{\"source\":\"manual\",\"operatorId\":" + operatorId + "}");
        paymentRepository.save(payment);
        log.info("【订单】管理员 {} 手动确认订单 {} 已支付", operatorId, orderNo);
    }

    // ===================== 私有工具 =====================

    /**
     * 生成订单号：时间戳(14位) + 用户ID后4位 + 随机3位
     *
     * @param userId 用户ID
     * @return 订单号
     */
    private String generateOrderNo(Long userId) {
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String time = LocalDateTime.now().format(fmt);
        long tail = userId == null ? 0 : userId % 10000;
        int rand = ThreadLocalRandom.current().nextInt(100, 999);
        return time + String.format("%04d", tail) + rand;
    }

    /**
     * 回补库存（订单取消时）
     *
     * @param orderId 订单ID
     */
    private void rollbackStock(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            productRepository.findById(item.getProductId()).ifPresent(p -> {
                p.setStock(p.getStock() + item.getQuantity());
                p.setSales(Math.max(0, p.getSales() - item.getQuantity()));
                productRepository.save(p);
            });
        }
    }
}
