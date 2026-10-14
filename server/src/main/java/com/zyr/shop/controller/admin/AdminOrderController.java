package com.zyr.shop.controller.admin;

import com.zyr.shop.common.PageResult;
import com.zyr.shop.common.Result;
import com.zyr.shop.dto.OrderVO;
import com.zyr.shop.security.RequireAdmin;
import com.zyr.shop.security.UserContext;
import com.zyr.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员订单管理接口（需 ADMIN 角色）
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    /** 订单服务 */
    private final OrderService orderService;

    /**
     * 订单分页查询
     *
     * @param orderNo 订单号关键词
     * @param status 状态
     * @param page   页码
     * @param size   每页条数
     * @return 分页结果
     */
    @RequireAdmin
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(@RequestParam(required = false) String orderNo,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.searchAdmin(orderNo, status, page, size));
    }

    /**
     * 查询订单详情（管理员可查看任意订单）
     *
     * @param orderNo 订单号
     * @return 订单详情（含订单项与用户名）
     */
    @RequireAdmin
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.success(orderService.detail(UserContext.getUserId(), orderNo));
    }

    /**
     * 修改订单状态（发货、完成等）
     *
     * @param orderNo 订单号
     * @param status  新状态
     * @return 操作结果
     */
    @RequireAdmin
    @PutMapping("/{orderNo}/status")
    public Result<Void> updateStatus(@PathVariable String orderNo, @RequestParam String status) {
        orderService.updateStatus(orderNo, status);
        return Result.success();
    }

    /**
     * 管理员手动确认订单已支付
     *
     * <p>使用场景：本地学习无内网穿透/支付宝异步回调不可达时，
     * 管理员在沙箱付款成功后通过此接口把订单标记为已支付并写支付流水</p>
     *
     * @param orderNo 订单号
     * @return 操作结果
     */
    @RequireAdmin
    @PutMapping("/{orderNo}/mark-paid")
    public Result<Void> markPaid(@PathVariable String orderNo) {
        orderService.markPaidManually(orderNo, UserContext.getUserId());
        return Result.success();
    }
}
