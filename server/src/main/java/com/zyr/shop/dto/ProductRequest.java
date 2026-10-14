package com.zyr.shop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品新增/修改请求参数（管理员后台使用）
 *
 * @author zyr
 */
@Data
public class ProductRequest {

    /** 分类ID */
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /** 商品描述 */
    private String description;

    /** 商品价格（必须大于0） */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    /** 库存（必须≥0） */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

    /** 封面图访问路径 */
    private String coverImage;

    /** 状态：1-上架，0-下架 */
    private Integer status;
}
