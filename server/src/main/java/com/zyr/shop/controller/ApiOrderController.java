package com.zyr.shop.controller;

import com.zyr.shop.common.PageResult;
import com.zyr.shop.common.Result;
import com.zyr.shop.dto.OrderRequest;
import com.zyr.shop.dto.OrderVO;
import com.zyr.shop.security.UserContext;
import com.zyr.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台订单接口（需登录，路径前缀 /api/order）
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class ApiOrderController {

    /** 订单服务 */
    private final OrderService orderService;

    /**
     * 创建订单（基于购物车）
     *
     * @param request 下单参数
     * @return 订单视图
     */
    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderRequest request) {
        return Result.success(orderService.createOrder(UserContext.getUserId(), request));
    }

    /**
     * 分页查询我的订单
     *
     * @param status 订单状态（可空）
     * @param page   页码
     * @param size   每页条数
     * @return 订单分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(@RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.searchByUser(UserContext.getUserId(), status, page, size));
    }

    /**
     * 订单详情
     *
     * @param orderNo 订单号
     * @return 订单视图
     */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.success(orderService.detail(UserContext.getUserId(), orderNo));
    }

    /**
     * 用户取消订单
     *
     * @param orderNo 订单号
     * @return 操作结果
     */
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancelByUser(UserContext.getUserId(), orderNo);
        return Result.success();
    }
}
