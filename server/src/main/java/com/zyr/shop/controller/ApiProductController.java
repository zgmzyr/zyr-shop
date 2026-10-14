package com.zyr.shop.controller;

import com.zyr.shop.common.PageResult;
import com.zyr.shop.common.Result;
import com.zyr.shop.dto.ProductVO;
import com.zyr.shop.entity.Category;
import com.zyr.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台商品接口（普通用户访问，免鉴权）
 *
 * <p>路径前缀 {@code /api/product}，列表与详情在 WebConfig 中已免鉴权</p>
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ApiProductController {

    /** 商品服务 */
    private final ProductService productService;

    /**
     * 商品分页查询（仅上架）
     *
     * @param categoryId 分类ID（可空）
     * @param keyword    关键词（可空）
     * @param page       页码（默认1）
     * @param size       每页条数（默认12）
     * @return 商品分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<ProductVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return Result.success(productService.searchOnSale(categoryId, keyword, page, size));
    }

    /**
     * 商品详情
     *
     * @param id 商品ID
     * @return 商品视图
     */
    @GetMapping("/detail/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }

    /**
     * 启用中的商品分类列表（前台导航/筛选用）
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.success(productService.listEnabledCategories());
    }
}
