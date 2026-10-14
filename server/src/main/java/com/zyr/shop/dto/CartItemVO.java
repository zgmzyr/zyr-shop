package com.zyr.shop.dto;

import com.zyr.shop.entity.CartItem;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项视图对象（携带商品快照信息，便于前台展示）
 *
 * @author zyr
 */
@Data
public class CartItemVO {

    /** 购物车项ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 商品名称（快照，列表展示用） */
    private String productName;

    /** 商品封面图 */
    private String coverImage;

    /** 单价（加入购物车时的快照） */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 单项小计金额（price × quantity） */
    private BigDecimal subtotal;

    /** 当前商品库存（前台判断是否超卖） */
    private Integer currentStock;

    /** 商品状态：1-上架 0-下架 */
    private Integer productStatus;

    /**
     * 由购物车项构造（不含商品冗余字段，需在外部填充）
     *
     * @param item 购物车项
     * @return 视图对象
     */
    public static CartItemVO from(CartItem item) {
        if (item == null) {
            return null;
        }
        CartItemVO vo = new CartItemVO();
        vo.id = item.getId();
        vo.productId = item.getProductId();
        vo.price = item.getPrice();
        vo.quantity = item.getQuantity();
        vo.subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return vo;
    }
}
