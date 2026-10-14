package com.zyr.shop.controller.admin;

import com.zyr.shop.common.Result;
import com.zyr.shop.security.RequireAdmin;
import com.zyr.shop.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理员首页数据统计接口（需 ADMIN 角色）
 *
 * <p>路径前缀 {@code /api/stats}</p>
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    /** 统计服务 */
    private final StatsService statsService;

    /**
     * 核心指标卡片数据
     *
     * @return 指标 Map
     */
    @RequireAdmin
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(statsService.overview());
    }

    /**
     * 近 N 天订单趋势（折线图）
     *
     * @param days 天数，默认 7
     * @return 日期-数量列表
     */
    @RequireAdmin
    @GetMapping("/order-trend")
    public Result<List<Map<String, Object>>> orderTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.orderTrend(days));
    }

    /**
     * 近 N 天销售额趋势（柱状图）
     *
     * @param days 天数，默认 7
     * @return 日期-销售额列表
     */
    @RequireAdmin
    @GetMapping("/sales-trend")
    public Result<List<Map<String, Object>>> salesTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.salesTrend(days));
    }

    /**
     * 商品分类分布（饼图）
     *
     * @return 分类-数量列表
     */
    @RequireAdmin
    @GetMapping("/product-category")
    public Result<List<Map<String, Object>>> productCategory() {
        return Result.success(statsService.productCategoryDistribution());
    }

    /**
     * 用户角色分布（环形图）
     *
     * @return 角色-数量列表
     */
    @RequireAdmin
    @GetMapping("/user-role")
    public Result<List<Map<String, Object>>> userRole() {
        return Result.success(statsService.userRoleDistribution());
    }

    /**
     * 客服问答趋势（柱状图）
     *
     * @param days 天数
     * @return 日期-数量列表
     */
    @RequireAdmin
    @GetMapping("/chat-trend")
    public Result<List<Map<String, Object>>> chatTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statsService.chatTrend(days));
    }

    /**
     * 知识库文档类型分布（饼图）
     *
     * @return 类型-数量列表
     */
    @RequireAdmin
    @GetMapping("/document-type")
    public Result<List<Map<String, Object>>> documentType() {
        return Result.success(statsService.documentTypeDistribution());
    }
}
