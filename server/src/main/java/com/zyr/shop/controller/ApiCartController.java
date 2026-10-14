package com.zyr.shop.controller;

import com.zyr.shop.common.Result;
import com.zyr.shop.dto.CartItemVO;
import com.zyr.shop.dto.CartRequest;
import com.zyr.shop.security.UserContext;
import com.zyr.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台购物车接口（需登录，路径前缀 /api/cart）
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class ApiCartController {

    /** 购物车服务 */
    private final CartService cartService;

    /**
     * 加入购物车
     *
     * @param request 加购请求
     * @return 当前购物车列表
     */
    @PostMapping
    public Result<List<CartItemVO>> add(@Valid @RequestBody CartRequest request) {
        return Result.success(cartService.add(UserContext.getUserId(), request.getProductId(), request.getQuantity()));
    }

    /**
     * 查询购物车
     *
     * @return 购物车项列表
     */
    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.list(UserContext.getUserId()));
    }

    /**
     * 修改购物车项数量
     *
     * @param cartItemId 购物车项ID
     * @param quantity   新数量
     * @return 当前购物车列表
     */
    @PutMapping("/{cartItemId}")
    public Result<List<CartItemVO>> update(@PathVariable Long cartItemId, @RequestParam Integer quantity) {
        return Result.success(cartService.updateQuantity(UserContext.getUserId(), cartItemId, quantity));
    }

    /**
     * 删除购物车单项
     *
     * @param cartItemId 购物车项ID
     * @return 当前购物车列表
     */
    @DeleteMapping("/{cartItemId}")
    public Result<List<CartItemVO>> remove(@PathVariable Long cartItemId) {
        return Result.success(cartService.remove(UserContext.getUserId(), cartItemId));
    }
}
