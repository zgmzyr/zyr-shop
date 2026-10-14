package com.zyr.shop.service;

import com.zyr.shop.common.BusinessException;
import com.zyr.shop.dto.CartItemVO;
import com.zyr.shop.entity.Cart;
import com.zyr.shop.entity.CartItem;
import com.zyr.shop.entity.Product;
import com.zyr.shop.repository.CartItemRepository;
import com.zyr.shop.repository.CartRepository;
import com.zyr.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 购物车服务
 *
 * <p>职责：</p>
 * <ul>
 *     <li>加购：商品已存在则累加数量，单价以最新商品价格刷新</li>
 *     <li>查询：返回带商品快照信息的购物车项</li>
 *     <li>修改数量、删除单项、清空</li>
 * </ul>
 *
 * <p>购物车按用户唯一（每个用户一辆），首次加购时自动创建</p>
 *
 * @author zyr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    /** 购物车数据访问 */
    private final CartRepository cartRepository;

    /** 购物车项数据访问 */
    private final CartItemRepository cartItemRepository;

    /** 商品数据访问（用于加购时校验与取价） */
    private final ProductRepository productRepository;

    /**
     * 加入购物车
     *
     * <p>若购物车中已有同款商品，累加数量；否则新建购物车项</p>
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  数量
     * @return 当前购物车列表
     */
    @Transactional
    public List<CartItemVO> add(Long userId, Long productId, Integer quantity) {
        // 1. 校验商品存在且上架
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(400, "商品已下架，无法加入购物车");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException(400, "库存不足，当前剩余：" + product.getStock());
        }

        // 2. 获取或创建购物车
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUserId(userId);
            return cartRepository.save(c);
        });

        // 3. 已有则累加，无则新增
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setCartId(cart.getId());
                    ci.setProductId(productId);
                    ci.setPrice(product.getPrice());
                    ci.setQuantity(0);
                    return ci;
                });
        // 价格按商品最新价刷新（保证下单时金额正确）
        item.setPrice(product.getPrice());
        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
        log.info("【购物车】用户 {} 加入商品 {} 数量 {}", userId, productId, quantity);

        return list(userId);
    }

    /**
     * 查询当前用户购物车
     *
     * @param userId 用户ID
     * @return 购物车项视图列表（含商品快照信息）
     */
    public List<CartItemVO> list(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return List.of();
        }
        List<CartItem> items = cartItemRepository.findByCartIdOrderByCreateTimeDesc(cart.getId());
        if (items.isEmpty()) {
            return List.of();
        }
        // 批量取商品快照
        Set<Long> productIds = items.stream().map(CartItem::getProductId).collect(Collectors.toSet());
        Map<Long, Product> productMap = new HashMap<>();
        for (Product p : productRepository.findAllById(productIds)) {
            productMap.put(p.getId(), p);
        }
        return items.stream().map(item -> {
            CartItemVO vo = CartItemVO.from(item);
            Product p = productMap.get(item.getProductId());
            if (p != null) {
                vo.setProductName(p.getName());
                vo.setCoverImage(p.getCoverImage());
                vo.setCurrentStock(p.getStock());
                vo.setProductStatus(p.getStatus());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 修改购物车项数量
     *
     * @param userId       用户ID
     * @param cartItemId   购物车项ID
     * @param newQuantity  新数量
     * @return 当前购物车列表
     */
    @Transactional
    public List<CartItemVO> updateQuantity(Long userId, Long cartItemId, Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new BusinessException(400, "数量至少为1");
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(404, "购物车为空"));
        CartItem item = cartItemRepository.findById(cartItemId)
                .filter(i -> i.getCartId().equals(cart.getId()))
                .orElseThrow(() -> new BusinessException(404, "购物车项不存在"));
        Product p = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        if (p.getStock() < newQuantity) {
            throw new BusinessException(400, "库存不足，当前剩余：" + p.getStock());
        }
        item.setQuantity(newQuantity);
        item.setPrice(p.getPrice());
        cartItemRepository.save(item);
        return list(userId);
    }

    /**
     * 删除购物车单项
     *
     * @param userId     用户ID
     * @param cartItemId  购物车项ID
     * @return 当前购物车列表
     */
    @Transactional
    public List<CartItemVO> remove(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cartItemRepository.findById(cartItemId)
                    .filter(i -> i.getCartId().equals(cart.getId()))
                    .ifPresent(cartItemRepository::delete);
        }
        return list(userId);
    }

    /**
     * 清空购物车（下单成功后调用）
     *
     * @param userId 用户ID
     */
    @Transactional
    public void clear(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(c -> {
            cartItemRepository.deleteByCartId(c.getId());
            log.info("【购物车】用户 {} 清空购物车", userId);
        });
    }
}
