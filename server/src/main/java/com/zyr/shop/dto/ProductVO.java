package com.zyr.shop.dto;

import com.zyr.shop.entity.Product;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品视图对象（前台展示用，附带分类名）
 *
 * @author zyr
 */
@Data
public class ProductVO {

    /** 商品ID */
    private Long id;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称（冗余字段，列表展示用） */
    private String categoryName;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;

    /** 商品价格 */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 封面图访问路径 */
    private String coverImage;

    /** 状态：1-上架，0-下架 */
    private Integer status;

    /**
     * 由实体直接转换（不包含分类名）
     *
     * @param p 商品实体
     * @return 视图对象
     */
    public static ProductVO from(Product p) {
        if (p == null) {
            return null;
        }
        ProductVO vo = new ProductVO();
        vo.id = p.getId();
        vo.categoryId = p.getCategoryId();
        vo.name = p.getName();
        vo.description = p.getDescription();
        vo.price = p.getPrice();
        vo.stock = p.getStock();
        vo.sales = p.getSales();
        vo.coverImage = p.getCoverImage();
        vo.status = p.getStatus();
        return vo;
    }
}
