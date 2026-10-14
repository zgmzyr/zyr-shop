package com.zyr.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入购物车请求参数
 *
 * @author zyr
 */
@Data
public class CartRequest {

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 数量（≥1） */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}
