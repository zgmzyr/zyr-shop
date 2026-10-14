package com.zyr.shop.service;

import com.zyr.shop.common.BusinessException;
import com.zyr.shop.common.PageResult;
import com.zyr.shop.dto.ProductRequest;
import com.zyr.shop.dto.ProductVO;
import com.zyr.shop.entity.Category;
import com.zyr.shop.entity.Product;
import com.zyr.shop.repository.CategoryRepository;
import com.zyr.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品服务
 *
 * <p>职责：</p>
 * <ul>
 *     <li>前台商品分页/详情查询（仅上架商品）</li>
 *     <li>管理员商品 CRUD</li>
 *     <li>商品分类管理</li>
 *     <li>扣减库存（被订单服务调用）</li>
 * </ul>
 *
 * @author zyr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    /** 商品数据访问 */
    private final ProductRepository productRepository;

    /** 分类数据访问 */
    private final CategoryRepository categoryRepository;

    // ===================== 前台接口 =====================

    /**
     * 前台商品分页查询（仅上架）
     *
     * @param categoryId 分类ID（可空）
     * @param keyword    关键词（可空）
     * @param page       页码（从1开始）
     * @param size       每页条数
     * @return 商品分页结果
     */
    public PageResult<ProductVO> searchOnSale(Long categoryId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Product> p = productRepository.searchOnSale(categoryId, keyword, pageable);
        // 批量填充分类名
        Map<Long, String> categoryNameMap = getCategoryNameMap(p.getContent());
        List<ProductVO> list = p.getContent().stream().map(e -> {
            ProductVO vo = ProductVO.from(e);
            vo.setCategoryName(categoryNameMap.get(e.getCategoryId()));
            return vo;
        }).collect(Collectors.toList());
        PageResult<ProductVO> result = new PageResult<>();
        result.setPage((long) (p.getNumber() + 1));
        result.setSize((long) p.getSize());
        result.setTotal(p.getTotalElements());
        result.setRecords(list);
        return result;
    }

    /**
     * 前台商品详情
     *
     * @param id 商品ID
     * @return 商品视图
     */
    public ProductVO detail(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        ProductVO vo = ProductVO.from(p);
        categoryRepository.findById(p.getCategoryId())
                .ifPresent(c -> vo.setCategoryName(c.getName()));
        return vo;
    }

    /**
     * 查询所有启用的商品分类（前台导航/筛选用）
     *
     * @return 分类列表
     */
    public List<Category> listEnabledCategories() {
        return categoryRepository.findByStatusOrderBySortAsc(1);
    }

    // ===================== 管理员接口 =====================

    /**
     * 管理员商品分页查询（不限状态）
     *
     * @param keyword 关键词（可空）
     * @param page    页码
     * @param size    每页条数
     * @return 商品分页结果
     */
    public PageResult<ProductVO> searchAdmin(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Product> p = productRepository.searchAdmin(keyword, pageable);
        Map<Long, String> categoryNameMap = getCategoryNameMap(p.getContent());
        List<ProductVO> list = p.getContent().stream().map(e -> {
            ProductVO vo = ProductVO.from(e);
            vo.setCategoryName(categoryNameMap.get(e.getCategoryId()));
            return vo;
        }).collect(Collectors.toList());
        PageResult<ProductVO> result = new PageResult<>();
        result.setPage((long) (p.getNumber() + 1));
        result.setSize((long) p.getSize());
        result.setTotal(p.getTotalElements());
        result.setRecords(list);
        return result;
    }

    /**
     * 管理员新增商品
     *
     * @param request 商品请求参数
     * @return 新增后的商品视图
     */
    public ProductVO create(ProductRequest request) {
        // 校验分类存在
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(400, "分类不存在"));
        Product p = new Product();
        p.setCategoryId(category.getId());
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setPrice(request.getPrice());
        p.setStock(request.getStock());
        p.setSales(0);
        p.setCoverImage(request.getCoverImage());
        p.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        Product saved = productRepository.save(p);
        log.info("【商品管理】新增商品 id={} name={}", saved.getId(), saved.getName());
        ProductVO vo = ProductVO.from(saved);
        vo.setCategoryName(category.getName());
        return vo;
    }

    /**
     * 管理员修改商品
     *
     * @param id      商品ID
     * @param request 商品请求参数
     * @return 修改后的商品视图
     */
    public ProductVO update(Long id, ProductRequest request) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(400, "分类不存在"));
        p.setCategoryId(category.getId());
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        p.setPrice(request.getPrice());
        p.setStock(request.getStock());
        if (request.getCoverImage() != null) {
            p.setCoverImage(request.getCoverImage());
        }
        if (request.getStatus() != null) {
            p.setStatus(request.getStatus());
        }
        Product saved = productRepository.save(p);
        log.info("【商品管理】修改商品 id={}", id);
        ProductVO vo = ProductVO.from(saved);
        vo.setCategoryName(category.getName());
        return vo;
    }

    /**
     * 管理员上架/下架商品
     *
     * @param id     商品ID
     * @param status 状态：1-上架，0-下架
     */
    public void changeStatus(Long id, Integer status) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        p.setStatus(status == null ? 1 : status);
        productRepository.save(p);
        log.info("【商品管理】商品 {} 状态变更为 {}", id, p.getStatus());
    }

    /**
     * 管理员删除商品
     *
     * @param id 商品ID
     */
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(404, "商品不存在");
        }
        productRepository.deleteById(id);
        log.info("【商品管理】删除商品 id={}", id);
    }

    // ===================== 分类管理 =====================

    /**
     * 全部分类（管理员，含禁用）
     *
     * @return 分类列表
     */
    public List<Category> listAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * 新增分类
     *
     * @param name 名称
     * @param sort 排序
     */
    public Category createCategory(String name, Integer sort) {
        Category c = new Category();
        c.setName(name);
        c.setSort(sort == null ? 0 : sort);
        c.setStatus(1);
        return categoryRepository.save(c);
    }

    /**
     * 修改分类
     *
     * @param id     分类ID
     * @param name   名称
     * @param sort   排序
     * @param status 状态
     */
    public Category updateCategory(Long id, String name, Integer sort, Integer status) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        if (name != null && !name.isBlank()) {
            c.setName(name);
        }
        if (sort != null) {
            c.setSort(sort);
        }
        if (status != null) {
            c.setStatus(status);
        }
        return categoryRepository.save(c);
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException(404, "分类不存在");
        }
        categoryRepository.deleteById(id);
        log.info("【商品管理】删除分类 id={}", id);
    }

    // ===================== 库存扣减（订单服务调用） =====================

    /**
     * 扣减商品库存并增加销量
     *
     * <p>使用乐观更新语句，避免并发超卖；返回0时由调用方抛业务异常</p>
     *
     * @param productId 商品ID
     * @param quantity  数量
     * @return 受影响行数，0 表示库存不足
     */
    @Transactional
    public int reduceStock(Long productId, Integer quantity) {
        return productRepository.reduceStock(productId, quantity);
    }

    // ===================== 私有工具 =====================

    /**
     * 批量取分类名映射（避免 N+1 查询）
     *
     * @param products 商品列表
     * @return categoryId -> categoryName
     */
    private Map<Long, String> getCategoryNameMap(List<Product> products) {
        Set<Long> ids = products.stream().map(Product::getCategoryId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        List<Category> categories = categoryRepository.findAllById(ids);
        return categories.stream().collect(Collectors.toMap(Category::getId, Category::getName));
    }
}
