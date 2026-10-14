package com.zyr.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 普通用户前台页面控制器（Thymeleaf 模板跳转）
 *
 * <p>说明：</p>
 * <ul>
 *     <li>本控制器只负责返回视图名称，所有数据交互由前端页面通过 fetch 调 /api/** 接口完成</li>
 *     <li>因 {@link com.zyr.shop.config.WebConfig} 中 {@code JwtInterceptor} 只拦截 {@code /api/**}，
 *         所以本控制器所有路由默认免鉴权，登录态由前端 JS 通过 cookie 中的 {@code zyr_token} 判断</li>
 *     <li>需要登录的页面（购物车、订单、支付、客服）由前端 JS 在加载时校验并跳转 {@code /login}</li>
 * </ul>
 *
 * @author zyr
 */
@Controller
public class PageController {

    /**
     * 用户登录页
     *
     * @return 模板路径 login
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 商城首页（商品列表 + 分类筛选）
     *
     * @return 模板路径 index
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 商品详情页
     *
     * @param id 商品 ID
     * @return 模板路径 product
     */
    @GetMapping("/product/{id}")
    public String product(@PathVariable Long id) {
        return "product";
    }

    /**
     * 购物车页（需登录）
     *
     * @return 模板路径 cart
     */
    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    /**
     * 结算页（确认订单信息，需登录）
     *
     * @return 模板路径 checkout
     */
    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

    /**
     * 我的订单页（需登录）
     *
     * @return 模板路径 order
     */
    @GetMapping("/order")
    public String order() {
        return "order";
    }

    /**
     * 支付页（调用支付宝沙箱网关生成支付表单）
     *
     * @return 模板路径 pay
     */
    @GetMapping("/pay")
    public String pay() {
        return "pay";
    }

    /**
     * 智能客服聊天窗（需登录）
     *
     * @return 模板路径 chat
     */
    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }
}
