package com.zyr.shop.controller;

import com.zyr.shop.common.BusinessException;
import com.zyr.shop.common.Result;
import com.zyr.shop.entity.Order;
import com.zyr.shop.repository.OrderRepository;
import com.zyr.shop.security.UserContext;
import com.zyr.shop.service.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付接口
 *
 * <p>路径前缀 {@code /api/payment}</p>
 *
 * <p>接口：</p>
 * <ul>
 *     <li>POST /api/payment/pay  创建支付订单，返回支付页 form HTML</li>
 *     <li>POST /api/payment/notify 支付宝异步通知（免鉴权，靠 SDK 验签）</li>
 *     <li>POST /api/payment/query  查询订单支付状态</li>
 * </ul>
 *
 * @author zyr
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class ApiPaymentController {

    /** 支付宝服务 */
    private final AlipayService alipayService;

    /** 订单数据访问 */
    private final OrderRepository orderRepository;

    /**
     * 创建支付订单，返回支付宝支付页表单 HTML
     *
     * @param orderNo 订单号
     * @return form HTML（前端直接 innerHTML 即可自动跳转到支付宝）
     */
    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        // 仅本人订单可发起支付
        if (!order.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权操作此订单");
        }
        // 仅 PENDING 状态可发起支付
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(400, "订单状态非待支付，当前：" + order.getStatus());
        }
        String subject = "zyr_shop商城订单-" + orderNo;
        String form = alipayService.createPagePay(orderNo, order.getTotalAmount(), subject, order.getUserId());
        return Result.success(form);
    }

    /**
     * 支付宝异步通知（公网回调，无 JWT 拦截）
     *
     * <p>支付宝完成付款后会 POST 到此接口，由 SDK 验签后更新订单状态</p>
     *
     * @param request HTTP 请求
     * @return 字符串 success / fail
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        return alipayService.handleNotify(request);
    }

    /**
     * 查询订单支付状态（前台支付完成后轮询）
     *
     * @param orderNo 订单号
     * @return 含 status 字段
     */
    @PostMapping("/query")
    public Result<Map<String, Object>> query(@RequestParam String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("status", order.getStatus());
        data.put("paid", "PAID".equals(order.getStatus()) || "SHIPPED".equals(order.getStatus())
                || "COMPLETED".equals(order.getStatus()));
        return Result.success(data);
    }
}
