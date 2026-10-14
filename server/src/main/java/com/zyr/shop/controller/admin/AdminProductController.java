package com.zyr.shop.controller.admin;

import com.zyr.shop.common.PageResult;
import com.zyr.shop.common.Result;
import com.zyr.shop.dto.ProductRequest;
import com.zyr.shop.dto.ProductVO;
import com.zyr.shop.entity.Category;
import com.zyr.shop.security.RequireAdmin;
import com.zyr.shop.service.ProductService;
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
 * 管理员商品/分类管理接口（需 ADMIN 角色）
 *
 * <p>路径前缀 {@code /api/admin}，类内方法均标注 {@link RequireAdmin} 强制鉴权</p>
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductController {

    /** 商品服务 */
    private final ProductService productService;

    // ===================== 商品 CRUD =====================

    /**
     * 管理员商品分页查询
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页条数
     * @return 分页结果
     */
    @RequireAdmin
    @GetMapping("/product/list")
    public Result<PageResult<ProductVO>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(productService.searchAdmin(keyword, page, size));
    }

    /**
     * 新增商品
     *
     * @param request 商品参数
     * @return 新增后的商品视图
     */
    @RequireAdmin
    @PostMapping("/product")
    public Result<ProductVO> create(@Valid @RequestBody ProductRequest request) {
        return Result.success(productService.create(request));
    }

    /**
     * 修改商品
     *
     * @param id      商品ID
     * @param request 商品参数
     * @return 修改后的商品视图
     */
    @RequireAdmin
    @PutMapping("/product/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return Result.success(productService.update(id, request));
    }

    /**
     * 上架/下架商品
     *
     * @param id     商品ID
     * @param status 状态
     * @return 操作结果
     */
    @RequireAdmin
    @PutMapping("/product/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.changeStatus(id, status);
        return Result.success();
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 操作结果
     */
    @RequireAdmin
    @DeleteMapping("/product/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }

    // ===================== 分类管理 =====================

    /**
     * 全部分类（含禁用，供管理员使用）
     *
     * @return 分类列表
     */
    @RequireAdmin
    @GetMapping("/category/list")
    public Result<List<Category>> categoryList() {
        return Result.success(productService.listAllCategories());
    }

    /**
     * 新增分类
     *
     * @param name 名称
     * @param sort 排序
     * @return 新增后的分类
     */
    @RequireAdmin
    @PostMapping("/category")
    public Result<Category> createCategory(@RequestParam String name,
                                           @RequestParam(required = false) Integer sort) {
        return Result.success(productService.createCategory(name, sort));
    }

    /**
     * 修改分类
     *
     * @param id     分类ID
     * @param name   名称
     * @param sort   排序
     * @param status 状态
     * @return 修改后的分类
     */
    @RequireAdmin
    @PutMapping("/category/{id}")
    public Result<Category> updateCategory(@PathVariable Long id,
                                           @RequestParam(required = false) String name,
                                           @RequestParam(required = false) Integer sort,
                                           @RequestParam(required = false) Integer status) {
        return Result.success(productService.updateCategory(id, name, sort, status));
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @RequireAdmin
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id);
        return Result.success();
    }
}
